package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.configs.*;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.measurables.MeasurableValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Collections.*;
import static java.util.concurrent.TimeUnit.*;
import static java.util.stream.Collectors.*;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractMeter<
    MI, // (Instance) Meter Impl
    IC extends MeterInstanceConfig,
    SC extends MeterSliceConfig<IC>,
    C extends MeterConfig<IC, SC>> extends AbstractMetric implements Meter {

    public interface MeasurableValuesProvider {
        MeasurableValues measurableValues();
    }

    public interface MeasurableValueProvider<MI> {
        Object valueFor(MI meterImpl);
    }

    public interface MeasurableValueProvidersProvider<
        MI,
        IC extends MeterInstanceConfig,
        SC extends MeterSliceConfig<IC>,
        C extends MeterConfig<IC, SC>> {

        Map<Measurable, MeasurableValueProvider<MI>> valueProvidersFor(
            IC instanceConfig,
            SC sliceConfig,
            C config,
            Set<? extends Measurable> measurables);
    }

    public interface MeterImplMaker<
        MI,
        IC extends MeterInstanceConfig,
        SC extends MeterSliceConfig<IC>,
        C extends MeterConfig<IC, SC>> {

        MI makeMeterImpl(
            IC instanceConfig,
            SC sliceConfig,
            C config,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor,
            MetricRegistry registry);
    }

    public interface InstanceMaker<MI> {
        AbstractMeterInstance<MI> makeInstance(
            MetricName name,
            List<LabelValue> labelValues,
            boolean totalInstance,
            boolean labeledMetricTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<MI>> measurableValueProviders,
            MI meterImpl);

        AbstractExpirableMeterInstance<MI> makeExpirableInstance(
            MetricName name,
            List<LabelValue> labelValues,
            boolean totalInstance,
            boolean labeledMetricTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<MI>> measurableValueProviders,
            MI meterImpl,
            long creationTimeMs);
    }

    public interface MeterImplUpdater<MI> {
        void update(MI meterImpl, long value);
    }

    static final long EXPIRED_INSTANCES_REMOVAL_ADDITIONAL_DELAY_MS = 10000L;

    private final AtomicBoolean removed = new AtomicBoolean();
    private final List<MetricListener> listeners;

    private final Label[] labels;
    private final int labelCount;
    private final boolean labeledInstanceExpirationEnabled;
    private final boolean labeledInstanceAutoRemovalEnabled;
    private final long minLabeledInstanceExpirationTimeMs;
    private final LabelValuesPredicate exclusionPredicate;

    private final Slice<MI, IC, SC, C> allSlice;
    private final Slice<MI, IC, SC, C>[] slices;

    private final TimeMsProvider timeMsProvider;
    private final ScheduledExecutorService executor;

    private static final Logger logger = getLogger(AbstractMeter.class);

    @SuppressWarnings("unchecked")
    protected AbstractMeter(
        MetricName name,
        C config,
        MeasurableValueProvidersProvider<MI, IC, SC, C> measurableValueProvidersProvider,
        MeterImplMaker<MI, IC, SC, C> meterImplMaker,
        MeterImplUpdater<MI> meterImplUpdater,
        InstanceMaker<MI> instanceMaker,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor,
        MetricRegistry registry) {

        super(config.isEnabled(), name, config.description());
        this.timeMsProvider = timeMsProvider;

        if (!config.isEnabled()) {
            this.listeners = null;
            this.labels = null;
            this.labelCount = 0;
            this.labeledInstanceExpirationEnabled = false;
            this.labeledInstanceAutoRemovalEnabled = false;
            this.minLabeledInstanceExpirationTimeMs = 0L;
            this.exclusionPredicate = null;
            this.allSlice = null;
            this.slices = null;
            this.executor = null;

            return;
        }

        this.listeners = new ArrayList<>();

        if (config.hasLabels()) {
            this.labels = config.labels().toArray(new Label[0]);
            this.labelCount = this.labels.length;
            this.exclusionPredicate = config.exclusionPredicate();
            SC allSliceConfig = config.allSliceConfig();

            Set<SC> enabledSliceConfigs =
                config.hasSliceConfigs() ?
                config.sliceConfigs().stream().filter(MeterSliceConfig::isEnabled).collect(toCollection(LinkedHashSet::new)) :
                emptySet();

            this.labeledInstanceExpirationEnabled =
                (allSliceConfig.isEnabled() && allSliceConfig.isLabeledInstanceExpirationEnabled())
                || enabledSliceConfigs.stream().anyMatch(MeterSliceConfig::isLabeledInstanceExpirationEnabled);

            this.labeledInstanceAutoRemovalEnabled =
                this.labeledInstanceExpirationEnabled
                || (allSliceConfig.isEnabled() && allSliceConfig.hasEffectiveMaxLabeledInstances())
                || enabledSliceConfigs.stream().anyMatch(MeterSliceConfig::hasEffectiveMaxLabeledInstances);

            this.minLabeledInstanceExpirationTimeMs =
                this.labeledInstanceExpirationEnabled ?
                minLabeledInstanceExpirationTimeMs(allSliceConfig, enabledSliceConfigs) :
                0L;

            SliceContext<MI, IC, SC, C> sliceContext = new SliceContext<>(
                name,
                config,
                removed,
                listeners,
                labels,
                labelCount,
                measurableValueProvidersProvider,
                meterImplMaker,
                instanceMaker,
                meterImplUpdater,
                timeMsProvider,
                executor);

            this.allSlice =
                allSliceConfig.isEnabled() ?
                new Slice<>(sliceContext, allSliceConfig, registry) :
                null;

            this.slices =
                !enabledSliceConfigs.isEmpty() ?
                enabledSliceConfigs.stream().map(sc -> new Slice<>(sliceContext, sc, registry)).toArray(Slice[]::new) :
                null;
        } else {
            this.labels = null;
            this.labelCount = 0;
            this.labeledInstanceExpirationEnabled = false;
            this.labeledInstanceAutoRemovalEnabled = false;
            this.minLabeledInstanceExpirationTimeMs = 0L;
            this.exclusionPredicate = null;
            SC allSliceConfig = config.allSliceConfig();

            if (allSliceConfig.isEnabled() && allSliceConfig.isTotalEnabled()) {
                SliceContext<MI, IC, SC, C> sliceContext = new SliceContext<>(
                    name,
                    config,
                    removed,
                    listeners,
                    null,
                    labelCount,
                    measurableValueProvidersProvider,
                    meterImplMaker,
                    instanceMaker,
                    meterImplUpdater,
                    timeMsProvider,
                    executor);

                this.allSlice = new Slice<>(sliceContext, config.allSliceConfig(), registry);
            } else {
                allSlice = null;
            }

            this.slices = null;
        }

        this.executor = executor;
    }

    private static long minLabeledInstanceExpirationTimeMs(
        MeterSliceConfig<?> allSliceConfig,
        Set<? extends MeterSliceConfig<?>> enabledSliceConfigs) {

        Optional<Long> slicesResult = enabledSliceConfigs.stream()
            .filter(MeterSliceConfig::isLabeledInstanceExpirationEnabled)
            .map(sc -> sc.labeledInstanceExpirationTime().toMillis())
            .min(Long::compareTo);

        if (allSliceConfig.isEnabled() && allSliceConfig.isLabeledInstanceExpirationEnabled()) {
            long allSliceResult = allSliceConfig.labeledInstanceExpirationTime().toMillis();
            return min(allSliceResult, slicesResult.orElse(allSliceResult));
        } else {
            return slicesResult.orElseThrow();
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void addListener(MetricListener listener) {
        if (!isEnabled() || isRemoved()) {
            return;
        }

        executor.execute(() -> {
            if (isRemoved()) {
                return;
            }

            listeners.add(listener);

            forEach(instance -> {
                notifyListener(listener, l -> l.metricInstanceAdded(instance));
                instance.metricInstanceAdded();
            });
        });
    }

    protected boolean isRemoved() {
        return removed.get();
    }

    @Override
    public void metricAdded() {
        if (labeledInstanceExpirationEnabled) {
            executor.schedule(
                this::removeExpiredInstancesAndSchedule,
                minLabeledInstanceExpirationTimeMs + EXPIRED_INSTANCES_REMOVAL_ADDITIONAL_DELAY_MS, MILLISECONDS);
        }
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void removeExpiredInstancesAndSchedule() {
        if (isRemoved()) {
            return;
        }

        removeExpiredInstances(true);
        long baseDelayMs = minLabeledInstanceExpirationTimeMs;
        long nowMs = timeMsProvider.timeMs();

        if (allSlice != null
            && allSlice.labeledInstanceExpirationEnabled
            && allSlice.instanceExpirationManager.hasInstanceExpirations()) {

            baseDelayMs = min(baseDelayMs, max(allSlice.instanceExpirationManager.minInstanceExpirationTimeMs - nowMs, 0L));
        }

        if (slices != null) {
            for (int i = 0; i < slices.length; ++i) {
                Slice<MI, IC, SC, C> slice = slices[i];

                if (slice.labeledInstanceExpirationEnabled && slice.instanceExpirationManager.hasInstanceExpirations()) {
                    baseDelayMs = min(baseDelayMs, max(slice.instanceExpirationManager.minInstanceExpirationTimeMs - nowMs, 0L));
                }
            }
        }

        executor.schedule(
            this::removeExpiredInstancesAndSchedule,
            baseDelayMs + EXPIRED_INSTANCES_REMOVAL_ADDITIONAL_DELAY_MS, MILLISECONDS);
    }

    public void metricRemoved() {
        if (isRemoved()) {
            return;
        }

        if (!isEnabled()) {
            removed.set(true);
            return;
        }

        executor.execute(() -> {
            if (isRemoved()) {
                return;
            }

            removed.set(true);

            forEach(instance -> {
                listeners.forEach(listener -> listener.metricInstanceRemoved(instance));
                instance.metricInstanceRemoved();
            });

            listeners.clear();
        });
    }

    @Override
    public Iterator<MetricInstance> iterator() {
        removeExpiredInstances(false);
        return isEnabled() ? new InstancesIterator(allSlice, slices) : emptyIterator();
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void removeExpiredInstances(boolean inExecutorThread) {
        if (!labeledInstanceExpirationEnabled || isRemoved()) {
            return;
        }

        long nowMs = timeMsProvider.timeMs();

        if (allSlice != null && allSlice.labeledInstanceExpirationEnabled) {
            allSlice.instanceExpirationManager.wake(false, nowMs, inExecutorThread);
        }

        if (slices != null) {
            for (int i = 0; i < slices.length; ++i) {
                Slice<MI, IC, SC, C> slice = slices[i];

                if (slice.labeledInstanceExpirationEnabled) {
                    slice.instanceExpirationManager.wake(false, nowMs, inExecutorThread);
                }
            }
        }
    }

    protected void update(long value, LabelValues labelValues) {
        if (!isEnabled() || isRemoved()) {
            return;
        }

        List<LabelValue> valueList = labelValues.list();
        checkLabelValues(valueList);

        if (areExcluded(labelValues)) {
            return;
        }

        long updateTimeMs = labeledInstanceAutoRemovalEnabled ? timeMsProvider.timeMs() : 0L;

        if (allSlice != null) {
            allSlice.update(value, valueList, updateTimeMs);
        }

        if (slices != null) {
            for (Slice<MI, IC, SC, C> slice : slices) {
                if (slice.matches(labelValues)) {
                    slice.update(value, valueList, updateTimeMs);
                }
            }
        }
    }

    private void checkLabelValues(List<LabelValue> labelValues) {
        if (labels != null) {
            if (labelValues == null || labelValues.size() != labels.length) {
                unexpected(labelValues);
            }

            for (int i = 0; i < labelCount; ++i) {
                if (!labels[i].equals(labelValues.get(i).label())) {
                    unexpected(labelValues);
                }
            }
        } else if (labelValues != null && !labelValues.isEmpty()) {
            unexpected(labelValues);
        }
    }

    private void unexpected(List<LabelValue> labelValues) {
        throw new IllegalArgumentException(
            "labelValues = " + labelValues +
            " do not match labels = " + Arrays.toString(labels));
    }

    private boolean areExcluded(LabelValues labelValues) {
        return exclusionPredicate != null && exclusionPredicate.matches(labelValues);
    }

    @Override
    public void removeInstancesFor(LabelValues labelValues) {
        if (!isEnabled() || isRemoved()) {
            return;
        }

        List<LabelValue> valueList = labelValues.list();
        checkLabelValues(valueList);

        if (areExcluded(labelValues)) {
            return;
        }

        if (allSlice != null) {
            allSlice.removeInstancesFor(labelValues);
        }

        if (slices != null) {
            for (Slice<MI, IC, SC, C> slice : slices) {
                if (slice.matches(labelValues)) {
                    slice.removeInstancesFor(labelValues);
                }
            }
        }
    }

    public static abstract class AbstractMeterInstance<MI> implements MeterInstance {

        private final MetricName name;
        private final List<LabelValue> labelValues;
        private final Map<Label, LabelValue> labelToValue;
        private final boolean totalInstance;
        private final boolean labeledMetricTotalInstance;
        private final boolean levelInstance;
        private final MeasurableValuesProvider measurableValuesProvider;
        private final Map<Measurable, MeasurableValueProvider<MI>> measurableValueProviders;
        private final Set<Measurable> measurables;
        private final boolean withPercentiles;
        private final boolean withBuckets;
        private final MI meterImpl;

        protected AbstractMeterInstance(
            MetricName name,
            List<LabelValue> labelValues,
            boolean totalInstance,
            boolean labeledMetricTotalInstance,
            boolean levelInstance,
            MeasurableValuesProvider measurableValuesProvider,
            Map<Measurable, MeasurableValueProvider<MI>> measurableValueProviders,
            MI meterImpl) {

            this.name = name;
            this.labelValues = labelValues;

            this.labelToValue =
                !labelValues.isEmpty() ?
                labelValues.stream().collect(toMap(LabelValue::label, lv -> lv)) :
                emptyMap();

            this.totalInstance = totalInstance;
            this.labeledMetricTotalInstance = labeledMetricTotalInstance;
            this.levelInstance = levelInstance;
            this.measurableValuesProvider = measurableValuesProvider;
            this.measurableValueProviders = measurableValueProviders;
            this.measurables = measurableValueProviders.keySet();
            this.withPercentiles = measurables().stream().anyMatch(m -> m instanceof Histogram.Percentile);
            this.withBuckets = measurables().stream().anyMatch(m -> m instanceof Histogram.Bucket);
            this.meterImpl = meterImpl;
        }

        @Override
        public MetricName name() {
            return name;
        }

        @Override
        public List<LabelValue> labelValues() {
            return labelValues;
        }

        @Override
        public Map<Label, LabelValue> labelToValue() {
            return labelToValue;
        }

        @Override
        public boolean isTotalInstance() {
            return totalInstance;
        }

        @Override
        public boolean isLabeledMetricTotalInstance() {
            return labeledMetricTotalInstance;
        }

        @Override
        public boolean isLevelInstance() {
            return levelInstance;
        }

        @Override
        public Set<Measurable> measurables() {
            return measurables;
        }

        @Override
        public boolean isWithPercentiles() {
            return withPercentiles;
        }

        @Override
        public boolean isWithBuckets() {
            return withBuckets;
        }

        protected MI meterImpl() {
            return meterImpl;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> V valueOf(Measurable measurable) throws NotMeasuredException {
            if (measurables.contains(measurable)) {
                return (V)measurableValueProviders.get(measurable).valueFor(meterImpl);
            } else {
                throw NotMeasuredException.forMeasurable(measurable);
            }
        }

        @Override
        public MeasurableValues measurableValues() {
            return measurableValuesProvider.measurableValues();
        }

        protected void update(long value, MeterImplUpdater<MI> meterImplUpdater) {
            meterImplUpdater.update(meterImpl, value);
        }
    }

    public static abstract class AbstractExpirableMeterInstance<MI> extends AbstractMeterInstance<MI> {

        private volatile long updateTimeMs;

        protected AbstractExpirableMeterInstance(
            MetricName name,
            List<LabelValue> labelValues,
            boolean totalInstance,
            boolean labeledMetricTotalInstance,
            boolean levelInstance,
            MeasurableValuesProvider measurableValuesProvider,
            Map<Measurable, MeasurableValueProvider<MI>> measurableValueProviders,
            MI meterImpl,
            long creationTimeMs) {

            super(
                name,
                labelValues,
                totalInstance,
                labeledMetricTotalInstance,
                levelInstance,
                measurableValuesProvider,
                measurableValueProviders,
                meterImpl);

            this.updateTimeMs = creationTimeMs;
        }

        protected void update(
            long value,
            MeterImplUpdater<MI> meterImplUpdater,
            long newUpdateTimeMs) {

            update(value, meterImplUpdater);
            long oldUpdateTimeMs = updateTimeMs;
            updateTimeMs = max(newUpdateTimeMs, oldUpdateTimeMs);
        }

        private long updateTimeMs() {
            return updateTimeMs;
        }
    }

    private static class InstanceKey {

        final List<LabelValue> labelValues;
        final Label[] labelsMask;
        final int hashCode;

        InstanceKey(List<LabelValue> labelValues, Label[] labelsMask) {
            this.labelValues = labelValues;
            this.labelsMask = labelsMask;
            this.hashCode = hashCodeFor(labelValues, labelsMask);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            InstanceKey that = (InstanceKey)other;

            if (hashCode != that.hashCode) {
                return false;
            }

            if (labelsMask != that.labelsMask) {
                return false;
            }

            for (int i = 0; i < labelsMask.length; ++i) {
                if (labelsMask[i] != null
                    && !labelValues.get(i).value().equals(that.labelValues.get(i).value())) {

                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        static int hashCodeFor(List<LabelValue> labelValues, Label[] labelsMask) {
            LabelValue[] items = new LabelValue[labelsMask.length];

            for (int i = 0; i < labelsMask.length; ++i) {
                items[i] = labelsMask[i] != null ? labelValues.get(i) : null;
            }

            return Arrays.hashCode(items);
        }
    }

    private static class SliceContext<
        MI,
        IC extends MeterInstanceConfig,
        SC extends MeterSliceConfig<IC>,
        C extends MeterConfig<IC, SC>> {

        final MetricName parentName;
        final C parentConfig;
        final AtomicBoolean parentRemoved;
        final List<MetricListener> listeners;
        final Label[] parentLabels;
        final int parentLabelCount;
        final MeasurableValueProvidersProvider<MI, IC, SC, C> measurableValueProvidersProvider;
        final MeterImplMaker<MI, IC, SC, C> meterImplMaker;
        final InstanceMaker<MI> instanceMaker;
        final MeterImplUpdater<MI> meterImplUpdater;
        final TimeMsProvider timeMsProvider;
        final ScheduledExecutorService executor;

        SliceContext(
            MetricName parentName,
            C parentConfig,
            AtomicBoolean parentRemoved,
            List<MetricListener> listeners,
            Label[] parentLabels,
            int parentLabelCount,
            MeasurableValueProvidersProvider<MI, IC, SC, C> measurableValueProvidersProvider,
            MeterImplMaker<MI, IC, SC, C> meterImplMaker,
            InstanceMaker<MI> instanceMaker,
            MeterImplUpdater<MI> meterImplUpdater,
            TimeMsProvider timeMsProvider,
            ScheduledExecutorService executor) {

            this.parentName = parentName;
            this.parentConfig = parentConfig;
            this.parentRemoved = parentRemoved;
            this.listeners = listeners;
            this.parentLabels = parentLabels;
            this.parentLabelCount = parentLabelCount;
            this.measurableValueProvidersProvider = measurableValueProvidersProvider;
            this.meterImplMaker = meterImplMaker;
            this.instanceMaker = instanceMaker;
            this.meterImplUpdater = meterImplUpdater;
            this.timeMsProvider = timeMsProvider;
            this.executor = executor;
        }

        boolean hasPrefixLabelValues() {
            return parentConfig.hasPrefixLabelValues();
        }

        boolean isParentRemoved() {
            return parentRemoved.get();
        }

        void forEachListener(Consumer<MetricListener> action) {
            listeners.forEach(l -> notifyListener(l, action));
        }

        Label parentLabel(int i) {
            return parentLabels[i];
        }

        void execute(Runnable task) {
            try {
                executor.execute(task);
            } catch (Exception e) {
                logger.error("Failed to execute task", e);
                throw e;
            }
        }
    }

    private static class Slice<
        MI,
        IC extends MeterInstanceConfig,
        SC extends MeterSliceConfig<IC>,
        C extends MeterConfig<IC, SC>> implements Iterable<MetricInstance> {

        static final long INFINITE_LABELED_INSTANCE_EXPIRATION_TIME_MS = DAYS.toMillis(10000L);

        final MetricRegistry registry;
        final SliceContext<MI, IC, SC, C> context;
        final int parentLabelCount;
        final SC config;
        final MetricName name;

        final LabelValuesPredicate predicate;
        final List<Label> labels;
        final boolean labeledInstanceAutoRemovalEnabled;
        final boolean labeledInstanceExpirationEnabled;
        final long labeledInstanceExpirationTimeMs;
        final InstanceExpirationManager instanceExpirationManager;
        final Label[] labelsMask;
        final Map<Measurable, MeasurableValueProvider<MI>> measurableValueProviders;

        final AbstractMeterInstance<MI> totalInstance;
        final ConcurrentHashMap<InstanceKey, AbstractMeterInstance<MI>> levelsInstances;
        final List<Label[]> levelsLabelsMasks;
        final List<Map<Measurable, MeasurableValueProvider<MI>>> levelsMeasurableValueProviders;

        final ConcurrentHashMap<InstanceKey, AbstractMeterInstance<MI>> instances;

        Slice(SliceContext<MI, IC, SC, C> context, SC config, MetricRegistry registry) {
            this.registry = registry;
            this.context = context;
            this.parentLabelCount = context.parentLabelCount;
            this.config = config;
            this.name = MetricName.of(context.parentName, config.name());
            this.predicate = config.predicate();
            this.labels = config.hasLabels() ? config.labels() : emptyList();
            this.labeledInstanceAutoRemovalEnabled = config.hasEffectiveMaxLabeledInstances() || config.isLabeledInstanceExpirationEnabled();
            this.labeledInstanceExpirationEnabled = config.isLabeledInstanceExpirationEnabled();

            if (this.labeledInstanceAutoRemovalEnabled) {
                this.labeledInstanceExpirationTimeMs =
                    this.labeledInstanceExpirationEnabled ?
                    config.labeledInstanceExpirationTime().toMillis() :
                    INFINITE_LABELED_INSTANCE_EXPIRATION_TIME_MS;
            } else {
                this.labeledInstanceExpirationTimeMs = 0L;
            }

            this.instanceExpirationManager =
                this.labeledInstanceAutoRemovalEnabled ?
                new InstanceExpirationManager(
                    config.hasMaxLabeledInstances() ? config.maxLabeledInstances() : Integer.MAX_VALUE,
                    context.executor) :
                null;

            if (!this.labels.isEmpty()) {
                this.labelsMask = new Label[this.parentLabelCount];

                for (int i = 0; i < this.parentLabelCount; ++i) {
                    Label label = context.parentLabel(i);
                    this.labelsMask[i] = this.labels.contains(label) ? label : null;
                }
            } else {
                this.labelsMask = null;
            }

            this.measurableValueProviders = context.measurableValueProvidersProvider.valueProvidersFor(
                null,
                config,
                context.parentConfig,
                config.measurables());

            if (config.isTotalEnabled()) {
                IC totalInstanceConfig = config.totalInstanceConfig();

                List<LabelValue> labelValues =
                    context.hasPrefixLabelValues() ?
                    context.parentConfig.prefixLabelValues().list() :
                    emptyList();

                if (totalInstanceConfig != null) {
                    Map<Measurable, MeasurableValueProvider<MI>> mvps =
                        totalInstanceConfig.hasMeasurables() ?
                        context.measurableValueProvidersProvider.valueProvidersFor(
                            totalInstanceConfig,
                            config,
                            context.parentConfig,
                            totalInstanceConfig.measurables()) :
                        this.measurableValueProviders;

                    MI meterImpl = context.meterImplMaker.makeMeterImpl(
                        totalInstanceConfig,
                        config,
                        context.parentConfig,
                        mvps.keySet(),
                        context.executor,
                        registry);

                    this.totalInstance = context.instanceMaker.makeInstance(
                        totalInstanceConfig.hasName() ? MetricName.of(this.name, totalInstanceConfig.name()) : this.name,
                        labelValues,
                        true,
                        !this.labels.isEmpty(),
                        false,
                        mvps,
                        meterImpl);
                } else {
                    MI meterImpl = context.meterImplMaker.makeMeterImpl(
                        null,
                        config,
                        context.parentConfig,
                        this.measurableValueProviders.keySet(),
                        context.executor,
                        registry);

                    this.totalInstance = context.instanceMaker.makeInstance(
                        this.name,
                        labelValues,
                        true,
                        !this.labels.isEmpty(),
                        false,
                        this.measurableValueProviders,
                        meterImpl);
                }
            } else {
                this.totalInstance = null;
            }

            this.instances = this.labels.isEmpty() ? null : new ConcurrentHashMap<>();
            int labelCount = this.labels.size();

            if (config.areLevelsEnabled() && labelCount > 1) {
                this.levelsInstances = new ConcurrentHashMap<>();
                this.levelsLabelsMasks = new ArrayList<>(labelCount - 1);
                this.levelsMeasurableValueProviders = new ArrayList<>(labelCount - 1);

                for (int i = 0; i < labelCount - 1; ++i) {
                    Label label = this.labels.get(i);

                    if (config.areOnlyConfiguredLevelsEnabled() && !config.hasLevelInstanceConfigFor(label)) {
                        this.levelsLabelsMasks.add(null);
                        continue;
                    }

                    Label[] levelLabelsMask = new Label[this.parentLabelCount];
                    int k = i;

                    for (int j = 0; j < this.parentLabelCount; ++j) {
                        Label parentLabel = context.parentLabel(j);

                        if (this.labels.contains(parentLabel) && k-- >= 0) {
                            levelLabelsMask[j] = parentLabel;
                        } else {
                            levelLabelsMask[j] = null;
                        }
                    }

                    this.levelsLabelsMasks.add(levelLabelsMask);
                    Map<Measurable, MeasurableValueProvider<MI>> levelMeasurableValueProviders;
                    IC levelInstanceConfig = config.levelInstanceConfigs().get(label);

                    if (levelInstanceConfig != null && levelInstanceConfig.hasMeasurables()) {
                        levelMeasurableValueProviders = context.measurableValueProvidersProvider.valueProvidersFor(
                            levelInstanceConfig,
                            config,
                            context.parentConfig,
                            levelInstanceConfig.measurables());
                    } else if (config.hasDefaultLevelInstanceConfig() && config.defaultLevelInstanceConfig().hasMeasurables()) {
                        levelMeasurableValueProviders = context.measurableValueProvidersProvider.valueProvidersFor(
                            config.defaultLevelInstanceConfig(),
                            config,
                            context.parentConfig,
                            config.defaultLevelInstanceConfig().measurables());
                    } else {
                        levelMeasurableValueProviders = this.measurableValueProviders;
                    }

                    this.levelsMeasurableValueProviders.add(levelMeasurableValueProviders);
                }
            } else {
                this.levelsInstances = null;
                this.levelsLabelsMasks = null;
                this.levelsMeasurableValueProviders = null;
            }
        }

        boolean matches(LabelValues labelValues) {
            return predicate == null || predicate.matches(labelValues);
        }

        void update(long value, List<LabelValue> labelValues, long updateTimeMs) {
            if (totalInstance != null) {
                totalInstance.update(value, context.meterImplUpdater);
            }

            if (levelsInstances != null) {
                for (int i = 0; i < levelsLabelsMasks.size(); ++i) {
                    Label[] levelLabelsMask = levelsLabelsMasks.get(i);

                    if (levelLabelsMask == null) {
                        continue;
                    }

                    InstanceKey instanceKey = new InstanceKey(labelValues, levelLabelsMask);
                    AbstractMeterInstance<MI> instance = levelsInstances.get(instanceKey);

                    if (instance != null) {
                        updateInstance(instance, value, updateTimeMs);
                    } else {
                        final int i2 = i;

                        context.execute(() -> {
                            if (context.isParentRemoved()) {
                                return;
                            }

                            AbstractMeterInstance<MI> instance2 = levelsInstances.get(instanceKey);

                            if (instance2 != null) {
                                updateInstance(instance2, value, updateTimeMs);
                            } else {
                                List<LabelValue> instanceLabelValues;

                                if (context.hasPrefixLabelValues()) {
                                    LabelValues prefixLabelValues = context.parentConfig.prefixLabelValues();
                                    instanceLabelValues = new ArrayList<>(prefixLabelValues.size() + i2 + 1);
                                    instanceLabelValues.addAll(prefixLabelValues.list());
                                } else {
                                    instanceLabelValues = new ArrayList<>(i2 + 1);
                                }

                                for (int j = 0; j < levelLabelsMask.length; ++j) {
                                    if (levelLabelsMask[j] != null) {
                                        instanceLabelValues.add(labelValues.get(j));
                                    }
                                }

                                MetricName nameSuffix = null;
                                IC instanceConfig = config.levelInstanceConfigs().getOrDefault(labels.get(i2), config.defaultLevelInstanceConfig());

                                if (instanceConfig != null && instanceConfig.hasName()) {
                                    nameSuffix = instanceConfig.name();
                                } else if (config.hasLevelInstanceNameProvider()) {
                                    nameSuffix = config.levelInstanceNameProvider().nameForLevelInstance(instanceLabelValues);

                                    if (nameSuffix == null && config.hasDefaultLevelInstanceConfig() && config.defaultLevelInstanceConfig().hasName()) {
                                        nameSuffix = config.defaultLevelInstanceConfig().name();
                                    }
                                }

                                AbstractMeterInstance<MI> newInstance;
                                MetricName instanceName = nameSuffix != null ? MetricName.of(name, nameSuffix) : name;
                                Map<Measurable, MeasurableValueProvider<MI>> mvps = levelsMeasurableValueProviders.get(i2);

                                MI meterImpl = context.meterImplMaker.makeMeterImpl(
                                    instanceConfig,
                                    config,
                                    context.parentConfig,
                                    mvps.keySet(),
                                    context.executor,
                                    registry);

                                if (labeledInstanceAutoRemovalEnabled) {
                                    newInstance = context.instanceMaker.makeExpirableInstance(
                                        instanceName,
                                        instanceLabelValues,
                                        false,
                                        false,
                                        true,
                                        mvps,
                                        meterImpl,
                                        updateTimeMs);
                                } else {
                                    newInstance = context.instanceMaker.makeInstance(
                                        instanceName,
                                        instanceLabelValues,
                                        false,
                                        false,
                                        true,
                                        mvps,
                                        meterImpl);
                                }

                                addInstance(instanceKey, newInstance, value, updateTimeMs, levelsInstances);
                            }
                        });
                    }
                }
            }

            if (instances != null) {
                InstanceKey instanceKey = new InstanceKey(labelValues, labelsMask);
                AbstractMeterInstance<MI> instance = instances.get(instanceKey);

                if (instance != null) {
                    updateInstance(instance, value, updateTimeMs);
                } else {
                    context.execute(() -> {
                        if (context.isParentRemoved()) {
                            return;
                        }

                        AbstractMeterInstance<MI> instance2 = instances.get(instanceKey);

                        if (instance2 != null) {
                            updateInstance(instance2, value, updateTimeMs);
                        } else {
                            List<LabelValue> instanceLabelValues;

                            if (context.hasPrefixLabelValues()) {
                                LabelValues prefixLabelValues = context.parentConfig.prefixLabelValues();
                                instanceLabelValues = new ArrayList<>(prefixLabelValues.size() + labels.size());
                                instanceLabelValues.addAll(prefixLabelValues.list());
                            } else {
                                instanceLabelValues = new ArrayList<>(labels.size());
                            }

                            for (int i = 0; i < labelValues.size(); ++i) {
                                if (labelsMask[i] != null) {
                                    instanceLabelValues.add(labelValues.get(i));
                                }
                            }

                            AbstractMeterInstance<MI> newInstance;

                            MI meterImpl = context.meterImplMaker.makeMeterImpl(
                                null,
                                config,
                                context.parentConfig,
                                measurableValueProviders.keySet(),
                                context.executor,
                                registry);

                            if (labeledInstanceAutoRemovalEnabled) {
                                newInstance = context.instanceMaker.makeExpirableInstance(
                                    name,
                                    instanceLabelValues,
                                    false,
                                    false,
                                    false,
                                    measurableValueProviders,
                                    meterImpl,
                                    updateTimeMs);
                            } else {
                                newInstance = context.instanceMaker.makeInstance(
                                    name,
                                    instanceLabelValues,
                                    false,
                                    false,
                                    false,
                                    measurableValueProviders,
                                    meterImpl);
                            }

                            addInstance(instanceKey, newInstance, value, updateTimeMs, instances);
                        }
                    });
                }
            }

            if (labeledInstanceExpirationEnabled) {
                instanceExpirationManager.wake(false, updateTimeMs, false);
            }
        }

        private void addInstance(
            InstanceKey instanceKey,
            AbstractMeterInstance<MI> instance,
            long value,
            long creationTimeMs,
            ConcurrentHashMap<InstanceKey, AbstractMeterInstance<MI>> instances) {

            updateInstance(instance, value, creationTimeMs);
            instances.put(instanceKey, instance);

            if (labeledInstanceAutoRemovalEnabled) {
                AbstractExpirableMeterInstance<MI> expirableInstance = (AbstractExpirableMeterInstance<MI>)instance;
                long instanceUpdateTimeMs = expirableInstance.updateTimeMs();
                long instanceExpirationTimeMs = instanceUpdateTimeMs + labeledInstanceExpirationTimeMs;

                instanceExpirationManager.addInstanceExpiration(new InstanceExpiration(
                    this,
                    instanceKey,
                    expirableInstance,
                    instanceExpirationTimeMs,
                    instanceUpdateTimeMs));
            }

            context.forEachListener(l -> l.metricInstanceAdded(instance));
            instance.metricInstanceAdded();
        }

        void updateInstance(
            AbstractMeterInstance<MI> instance,
            long value,
            long updateTimeMs) {

            if (instance instanceof AbstractExpirableMeterInstance) {
                ((AbstractExpirableMeterInstance<MI>)instance).update(value, context.meterImplUpdater, updateTimeMs);
            } else {
                instance.update(value, context.meterImplUpdater);
            }
        }

        public void removeInstance(InstanceKey key) {
            AbstractMeterInstance<MI> instance = instances.remove(key);

            if (instance == null) {
                instance = levelsInstances.remove(key);
            }

            if (instance != null) {
                AbstractMeterInstance<MI> instanceRef = instance;
                context.forEachListener(l -> l.metricInstanceRemoved(instanceRef));
                instanceRef.metricInstanceRemoved();
            }
        }

        public void removeInstancesFor(LabelValues labelValues) {
            if (labelsMask != null) {
                context.execute(() -> {
                    InstanceKey key = new InstanceKey(labelValues.list(), labelsMask);
                    removeInstance(key);
                });
            }
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public SliceInstancesIterator iterator() {
            return new SliceInstancesIterator(
                totalInstance,
                instances != null ? instances.values().iterator() : null,
                levelsInstances != null ? levelsInstances.values().iterator() : null);
        }
    }

    private static class InstancesIterator implements Iterator<MetricInstance> {

        SliceInstancesIterator allSliceIter;
        final Slice<?, ?, ?, ?>[] slices;
        int sliceIndex;
        SliceInstancesIterator sliceIter;

        private InstancesIterator(Slice<?, ?, ?, ?> allSlice, Slice<?, ?, ?, ?>[] slices) {
            this.allSliceIter = allSlice != null ? allSlice.iterator() : null;
            this.slices = slices;

            if (slices != null && slices.length > 0) {
                while (sliceIndex < slices.length && !(sliceIter = slices[sliceIndex].iterator()).hasNext()) {
                    ++sliceIndex;
                }

                if (sliceIndex == slices.length) {
                    sliceIter = null;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return (allSliceIter != null && allSliceIter.hasNext()) || sliceIter != null;
        }

        @Override
        public MetricInstance next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (allSliceIter != null) {
                 if (allSliceIter.hasNext()) {
                     return allSliceIter.next();
                 } else {
                     allSliceIter = null;
                 }
            }

            MetricInstance result = sliceIter.next();

            if (!sliceIter.hasNext()) {
                ++sliceIndex;

                while (sliceIndex < slices.length && !(sliceIter = slices[sliceIndex].iterator()).hasNext()) {
                    ++sliceIndex;
                }

                if (sliceIndex == slices.length) {
                    sliceIter = null;
                }
            }

            return result;
        }
    }

    private static class SliceInstancesIterator implements Iterator<MetricInstance> {

        AbstractMeterInstance<?> totalInstance;
        Iterator<? extends MetricInstance> instancesIter;
        Iterator<? extends MetricInstance> levelsInstancesIter;

        SliceInstancesIterator(
            AbstractMeterInstance<?> totalInstance,
            Iterator<? extends MetricInstance> instancesIter,
            Iterator<? extends MetricInstance> levelsInstancesIter) {

            this.totalInstance = totalInstance;
            this.instancesIter = instancesIter;
            this.levelsInstancesIter = levelsInstancesIter;
        }

        @Override
        public boolean hasNext() {
            return totalInstance != null
                || (instancesIter != null && instancesIter.hasNext())
                || (levelsInstancesIter != null && levelsInstancesIter.hasNext());
        }

        @Override
        public MetricInstance next() {
            if (totalInstance != null) {
                AbstractMeterInstance<?> result = totalInstance;
                totalInstance = null;
                return result;
            }

            if (instancesIter != null) {
                if (instancesIter.hasNext()) {
                    return instancesIter.next();
                } else {
                    instancesIter = null;
                }
            }

            if (levelsInstancesIter != null) {
                if (levelsInstancesIter.hasNext()) {
                    return levelsInstancesIter.next();
                } else {
                    levelsInstancesIter = null;
                }
            }

            throw new NoSuchElementException();
        }
    }

    private static class InstanceExpiration implements Comparable<InstanceExpiration> {

        final Slice<?, ?, ?, ?> slice;
        final InstanceKey instanceKey;
        final AbstractExpirableMeterInstance<?> instance;
        final long expirationTimeMs;
        final long expectedInstanceUpdateTimeMs;

        InstanceExpiration(
            Slice<?, ?, ?, ?> slice,
            InstanceKey instanceKey,
            AbstractExpirableMeterInstance<?> instance,
            long expirationTimeMs,
            long expectedInstanceUpdateTimeMs) {

            this.slice = slice;
            this.instanceKey = instanceKey;
            this.instance = instance;
            this.expirationTimeMs = expirationTimeMs;
            this.expectedInstanceUpdateTimeMs = expectedInstanceUpdateTimeMs;
        }

        @Override
        public int compareTo(InstanceExpiration right) {
            int expirationTimeResult = Long.compare(expirationTimeMs, right.expirationTimeMs);

            if (expirationTimeResult != 0) {
                return expirationTimeResult;
            }

            int levelResult = Boolean.compare(instance.isLevelInstance(), right.instance.isLevelInstance());

            if (levelResult != 0) {
                return levelResult;
            }

            return Integer.compare(right.instance.labelValues().size(), instance.labelValues().size());
        }
    }

    private static class InstanceExpirationManager {

        final PriorityQueue<InstanceExpiration> queue = new PriorityQueue<>();
        final int maxQueueSize;
        volatile long minInstanceExpirationTimeMs;
        final AtomicBoolean expiredInstancesRemovalSubmitted = new AtomicBoolean();
        final ScheduledExecutorService executor;

        InstanceExpirationManager(int maxQueueSize, ScheduledExecutorService executor) {
            this.maxQueueSize = maxQueueSize;
            this.executor = executor;
        }

        @SuppressWarnings("ConstantConditions")
        void addInstanceExpiration(InstanceExpiration instanceExpiration) {
            queue.add(instanceExpiration);
            minInstanceExpirationTimeMs = queue.peek().expirationTimeMs;
            wake(true, instanceExpiration.instance.updateTimeMs, true);
        }

        boolean hasInstanceExpirations() {
            return minInstanceExpirationTimeMs > 0L;
        }

        void wake(boolean checkQueueSize, long nowMs, boolean inExecutorThread) {
            if ((checkQueueSize && queue.size() > maxQueueSize)
                || (minInstanceExpirationTimeMs > 0L && minInstanceExpirationTimeMs <= nowMs)) {

                if (expiredInstancesRemovalSubmitted.compareAndSet(false, true)) {
                    if (inExecutorThread) {
                        wake(nowMs);
                    } else {
                        executor.execute(() -> wake(nowMs));
                    }
                }
            }
        }

        void wake(long nowMs) {
            InstanceExpiration ie = queue.peek();

            while (ie != null && (queue.size() > maxQueueSize || ie.expirationTimeMs <= nowMs)) {
                queue.poll();
                long instanceUpdateTimeMs = ie.instance.updateTimeMs();

                if (instanceUpdateTimeMs == ie.expectedInstanceUpdateTimeMs) {
                    ie.slice.removeInstance(ie.instanceKey);
                } else {
                    queue.add(new InstanceExpiration(
                        ie.slice,
                        ie.instanceKey,
                        ie.instance,
                        instanceUpdateTimeMs + ie.slice.labeledInstanceExpirationTimeMs,
                        instanceUpdateTimeMs));
                }

                ie = queue.peek();
                minInstanceExpirationTimeMs = ie != null ? ie.expirationTimeMs : 0L;
            }

            expiredInstancesRemovalSubmitted.set(false);
        }
    }
}
