package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.configs.*;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.*;
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

    public interface MeasurableValueProvidersProvider<MI> {
        Map<Measurable, MeasurableValueProvider<MI>> valueProvidersFor(Set<? extends Measurable> measurables);
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
            Set<? extends Measurable> measurables);
    }

    public interface InstanceMaker<MI> {
        AbstractMeterInstance<MI> makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<MI>> measurableValueProviders,
            MI meterImpl);

        AbstractExpirableMeterInstance<MI> makeExpirableInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
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

    private final MetricDimension[] dimensions;
    private final int dimensionCount;
    private final boolean dimensionalInstanceExpirationEnabled;
    private final boolean dimensionalInstanceAutoRemovalEnabled;
    private final long minDimensionalInstanceExpirationTimeMs;
    private final MetricDimensionValuesPredicate exclusionPredicate;

    private final Slice<MI, IC, SC, C> allSlice;
    private final Slice<MI, IC, SC, C>[] slices;

    private final TimeMsProvider timeMsProvider;
    private final ScheduledExecutorService executor;
    private static final Logger logger = getLogger(AbstractMeter.class);

    @SuppressWarnings("unchecked")
    protected AbstractMeter(
        MetricName name,
        C config,
        MeasurableValueProvidersProvider<MI> measurableValueProvidersProvider,
        MeterImplMaker<MI, IC, SC, C> meterImplMaker,
        MeterImplUpdater<MI> meterImplUpdater,
        InstanceMaker<MI> instanceMaker,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor) {

        super(config.isEnabled(), name, config.description());
        this.timeMsProvider = timeMsProvider;

        if (!config.isEnabled()) {
            this.listeners = null;
            this.dimensions = null;
            this.dimensionCount = 0;
            this.dimensionalInstanceExpirationEnabled = false;
            this.dimensionalInstanceAutoRemovalEnabled = false;
            this.minDimensionalInstanceExpirationTimeMs = 0L;
            this.exclusionPredicate = null;
            this.allSlice = null;
            this.slices = null;
            this.executor = null;

            return;
        }

        this.listeners = new ArrayList<>();

        if (config.hasDimensions()) {
            this.dimensions = config.dimensions().toArray(new MetricDimension[0]);
            this.dimensionCount = this.dimensions.length;
            this.exclusionPredicate = config.exclusionPredicate();
            SC allSliceConfig = config.allSliceConfig();

            Set<SC> enabledSliceConfigs =
                config.hasSliceConfigs() ?
                config.sliceConfigs().stream().filter(MeterSliceConfig::isEnabled).collect(toCollection(LinkedHashSet::new)) :
                emptySet();

            this.dimensionalInstanceExpirationEnabled =
                (allSliceConfig.isEnabled() && allSliceConfig.isDimensionalInstanceExpirationEnabled())
                || enabledSliceConfigs.stream().anyMatch(MeterSliceConfig::isDimensionalInstanceExpirationEnabled);

            this.dimensionalInstanceAutoRemovalEnabled =
                this.dimensionalInstanceExpirationEnabled
                || (allSliceConfig.isEnabled() && allSliceConfig.hasEffectiveMaxDimensionalInstances())
                || enabledSliceConfigs.stream().anyMatch(MeterSliceConfig::hasEffectiveMaxDimensionalInstances);

            this.minDimensionalInstanceExpirationTimeMs =
                this.dimensionalInstanceExpirationEnabled ?
                minDimensionalInstanceExpirationTimeMs(allSliceConfig, enabledSliceConfigs) :
                0L;

            SliceContext<MI, IC, SC, C> sliceContext = new SliceContext<>(
                name,
                config,
                removed,
                listeners,
                dimensions,
                dimensionCount,
                measurableValueProvidersProvider,
                meterImplMaker,
                instanceMaker,
                meterImplUpdater,
                timeMsProvider,
                executor);

            this.allSlice =
                allSliceConfig.isEnabled() ?
                new Slice<>(sliceContext, allSliceConfig) :
                null;

            this.slices =
                !enabledSliceConfigs.isEmpty() ?
                enabledSliceConfigs.stream().map(sc -> new Slice<>(sliceContext, sc)).toArray(Slice[]::new) :
                null;
        } else {
            this.dimensions = null;
            this.dimensionCount = 0;
            this.dimensionalInstanceExpirationEnabled = false;
            this.dimensionalInstanceAutoRemovalEnabled = false;
            this.minDimensionalInstanceExpirationTimeMs = 0L;
            this.exclusionPredicate = null;
            SC allSliceConfig = config.allSliceConfig();

            if (allSliceConfig.isEnabled() && allSliceConfig.isTotalEnabled()) {
                SliceContext<MI, IC, SC, C> sliceContext = new SliceContext<>(
                    name,
                    config,
                    removed,
                    listeners,
                    null,
                    dimensionCount,
                    measurableValueProvidersProvider,
                    meterImplMaker,
                    instanceMaker,
                    meterImplUpdater,
                    timeMsProvider,
                    executor);

                this.allSlice = new Slice<>(sliceContext, config.allSliceConfig());
            } else {
                allSlice = null;
            }

            this.slices = null;
        }

        this.executor = executor;
    }

    private static long minDimensionalInstanceExpirationTimeMs(
        MeterSliceConfig<?> allSliceConfig,
        Set<? extends MeterSliceConfig<?>> enabledSliceConfigs) {

        Optional<Long> slicesResult = enabledSliceConfigs.stream()
            .filter(MeterSliceConfig::isDimensionalInstanceExpirationEnabled)
            .map(sc -> sc.dimensionalInstanceExpirationTime().toMillis())
            .min(Long::compareTo);

        if (allSliceConfig.isEnabled() && allSliceConfig.isDimensionalInstanceExpirationEnabled()) {
            long allSliceResult = allSliceConfig.dimensionalInstanceExpirationTime().toMillis();
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
            forEach(instance -> notifyListener(listener, l -> l.metricInstanceAdded(instance)));
        });
    }

    protected boolean isRemoved() {
        return removed.get();
    }

    @Override
    public void metricAdded() {
        if (dimensionalInstanceExpirationEnabled) {
            executor.schedule(
                this::removeExpiredInstancesAndSchedule,
                minDimensionalInstanceExpirationTimeMs + EXPIRED_INSTANCES_REMOVAL_ADDITIONAL_DELAY_MS, MILLISECONDS);
        }
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private void removeExpiredInstancesAndSchedule() {
        if (isRemoved()) {
            return;
        }

        removeExpiredInstances(true);
        long baseDelayMs = minDimensionalInstanceExpirationTimeMs;
        long nowMs = timeMsProvider.timeMs();

        if (allSlice != null
            && allSlice.dimensionalInstanceExpirationEnabled
            && allSlice.instanceExpirationManager.hasInstanceExpirations()) {

            baseDelayMs = min(baseDelayMs, max(allSlice.instanceExpirationManager.minInstanceExpirationTimeMs - nowMs, 0L));
        }

        if (slices != null) {
            for (int i = 0; i < slices.length; ++i) {
                Slice<MI, IC, SC, C> slice = slices[i];

                if (slice.dimensionalInstanceExpirationEnabled && slice.instanceExpirationManager.hasInstanceExpirations()) {
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
            listeners.forEach(listener -> forEach(instance -> notifyListener(listener, l -> l.metricInstanceRemoved(instance))));
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
        if (!dimensionalInstanceExpirationEnabled || isRemoved()) {
            return;
        }

        long nowMs = timeMsProvider.timeMs();

        if (allSlice != null && allSlice.dimensionalInstanceExpirationEnabled) {
            allSlice.instanceExpirationManager.wake(false, nowMs, inExecutorThread);
        }

        if (slices != null) {
            for (int i = 0; i < slices.length; ++i) {
                Slice<MI, IC, SC, C> slice = slices[i];

                if (slice.dimensionalInstanceExpirationEnabled) {
                    slice.instanceExpirationManager.wake(false, nowMs, inExecutorThread);
                }
            }
        }
    }

    protected void update(long value, MetricDimensionValues dimensionValues) {
        if (!isEnabled() || isRemoved()) {
            return;
        }

        List<MetricDimensionValue> valueList = dimensionValues.list();
        checkDimensionValues(valueList);

        if (areExcluded(dimensionValues)) {
            return;
        }

        long updateTimeMs = dimensionalInstanceAutoRemovalEnabled ? timeMsProvider.timeMs() : 0L;

        if (allSlice != null) {
            allSlice.update(value, valueList, updateTimeMs);
        }

        if (slices != null) {
            for (Slice<MI, IC, SC, C> slice : slices) {
                if (slice.matches(dimensionValues)) {
                    slice.update(value, valueList, updateTimeMs);
                }
            }
        }
    }

    private void checkDimensionValues(List<MetricDimensionValue> dimensionValues) {
        if (dimensions != null) {
            if (dimensionValues == null || dimensionValues.size() != dimensions.length) {
                unexpected(dimensionValues);
            }

            for (int i = 0; i < dimensionCount; ++i) {
                if (!dimensions[i].equals(dimensionValues.get(i).dimension())) {
                    unexpected(dimensionValues);
                }
            }
        } else if (dimensionValues != null && !dimensionValues.isEmpty()) {
            unexpected(dimensionValues);
        }
    }

    private void unexpected(List<MetricDimensionValue> dimensionValues) {
        throw new IllegalArgumentException(
            "dimensionValues = " + dimensionValues +
            " do not match dimensions = " + Arrays.toString(dimensions));
    }

    private boolean areExcluded(MetricDimensionValues dimensionValues) {
        return exclusionPredicate != null && exclusionPredicate.matches(dimensionValues);
    }

    @Override
    public void removeInstancesFor(MetricDimensionValues dimensionValues) {
        if (!isEnabled() || isRemoved()) {
            return;
        }

        List<MetricDimensionValue> valueList = dimensionValues.list();
        checkDimensionValues(valueList);

        if (areExcluded(dimensionValues)) {
            return;
        }

        if (allSlice != null) {
            allSlice.removeInstancesFor(dimensionValues);
        }

        if (slices != null) {
            for (Slice<MI, IC, SC, C> slice : slices) {
                if (slice.matches(dimensionValues)) {
                    slice.removeInstancesFor(dimensionValues);
                }
            }
        }
    }

    public static abstract class AbstractMeterInstance<MI> implements MeterInstance {

        private final MetricName name;
        private final List<MetricDimensionValue> dimensionValues;
        private final Map<MetricDimension, MetricDimensionValue> dimensionToValue;
        private final boolean totalInstance;
        private final boolean dimensionalTotalInstance;
        private final boolean levelInstance;
        private final MeasurableValuesProvider measurableValuesProvider;
        private final Map<Measurable, MeasurableValueProvider<MI>> measurableValueProviders;
        private final Set<Measurable> measurables;
        private final MI meterImpl;

        protected AbstractMeterInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            MeasurableValuesProvider measurableValuesProvider,
            Map<Measurable, MeasurableValueProvider<MI>> measurableValueProviders,
            MI meterImpl) {

            this.name = name;
            this.dimensionValues = dimensionValues;

            this.dimensionToValue =
                !dimensionValues.isEmpty() ?
                dimensionValues.stream().collect(toMap(MetricDimensionValue::dimension, dv -> dv)) :
                emptyMap();

            this.totalInstance = totalInstance;
            this.dimensionalTotalInstance = dimensionalTotalInstance;
            this.levelInstance = levelInstance;
            this.measurableValuesProvider = measurableValuesProvider;
            this.measurableValueProviders = measurableValueProviders;
            this.measurables = measurableValueProviders.keySet();
            this.meterImpl = meterImpl;
        }

        @Override
        public MetricName name() {
            return name;
        }

        @Override
        public List<MetricDimensionValue> dimensionValues() {
            return dimensionValues;
        }

        @Override
        public Map<MetricDimension, MetricDimensionValue> dimensionToValue() {
            return dimensionToValue;
        }

        @Override
        public boolean isTotalInstance() {
            return totalInstance;
        }

        @Override
        public boolean isDimensionalTotalInstance() {
            return dimensionalTotalInstance;
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
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            MeasurableValuesProvider measurableValuesProvider,
            Map<Measurable, MeasurableValueProvider<MI>> measurableValueProviders,
            MI meterImpl,
            long creationTimeMs) {

            super(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
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

        final List<MetricDimensionValue> dimensionValues;
        final MetricDimension[] dimensionsMask;
        final int hashCode;

        InstanceKey(
            List<MetricDimensionValue> dimensionValues,
            MetricDimension[] dimensionsMask) {

            this.dimensionValues = dimensionValues;
            this.dimensionsMask = dimensionsMask;
            this.hashCode = hashCodeFor(dimensionValues, dimensionsMask);
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

            if (dimensionsMask != that.dimensionsMask) {
                return false;
            }

            for (int i = 0; i < dimensionsMask.length; ++i) {
                if (dimensionsMask[i] != null
                    && !dimensionValues.get(i).value().equals(that.dimensionValues.get(i).value())) {

                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        static int hashCodeFor(List<MetricDimensionValue> dimensionValues, MetricDimension[] dimensionsMask) {
            HashCodeBuilder builder = new HashCodeBuilder(17, 37);

            for (int i = 0; i < dimensionsMask.length; ++i) {
                if (dimensionsMask[i] != null) {
                    builder.append(dimensionValues.get(i));
                }
            }

            return builder.toHashCode();
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
        final MetricDimension[] parentDimensions;
        final int parentDimensionCount;
        final MeasurableValueProvidersProvider<MI> measurableValueProvidersProvider;
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
            MetricDimension[] parentDimensions,
            int parentDimensionCount,
            MeasurableValueProvidersProvider<MI> measurableValueProvidersProvider,
            MeterImplMaker<MI, IC, SC, C> meterImplMaker,
            InstanceMaker<MI> instanceMaker,
            MeterImplUpdater<MI> meterImplUpdater,
            TimeMsProvider timeMsProvider,
            ScheduledExecutorService executor) {

            this.parentName = parentName;
            this.parentConfig = parentConfig;
            this.parentRemoved = parentRemoved;
            this.listeners = listeners;
            this.parentDimensions = parentDimensions;
            this.parentDimensionCount = parentDimensionCount;
            this.measurableValueProvidersProvider = measurableValueProvidersProvider;
            this.meterImplMaker = meterImplMaker;
            this.instanceMaker = instanceMaker;
            this.meterImplUpdater = meterImplUpdater;
            this.timeMsProvider = timeMsProvider;
            this.executor = executor;
        }

        boolean hasPrefixDimensionValues() {
            return parentConfig.hasPrefixDimensionValues();
        }

        boolean isParentRemoved() {
            return parentRemoved.get();
        }

        void forEachListener(Consumer<MetricListener> action) {
            listeners.forEach(l -> notifyListener(l, action));
        }

        MetricDimension parentDimension(int i) {
            return parentDimensions[i];
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

        static final long INFINITE_DIMENSIONAL_INSTANCE_EXPIRATION_TIME_MS = DAYS.toMillis(10000L);

        final SliceContext<MI, IC, SC, C> context;
        final int parentDimensionCount;
        final SC config;
        final MetricName name;

        final MetricDimensionValuesPredicate predicate;
        final List<MetricDimension> dimensions;
        final boolean dimensionalInstanceAutoRemovalEnabled;
        final boolean dimensionalInstanceExpirationEnabled;
        final long dimensionalInstanceExpirationTimeMs;
        final InstanceExpirationManager instanceExpirationManager;
        final MetricDimension[] dimensionsMask;
        final Map<Measurable, MeasurableValueProvider<MI>> measurableValueProviders;

        final AbstractMeterInstance<MI> totalInstance;
        final ConcurrentHashMap<InstanceKey, AbstractMeterInstance<MI>> levelsInstances;
        final List<MetricDimension[]> levelsDimensionsMasks;
        final List<Map<Measurable, MeasurableValueProvider<MI>>> levelsMeasurableValueProviders;

        final ConcurrentHashMap<InstanceKey, AbstractMeterInstance<MI>> instances;

        Slice(SliceContext<MI, IC, SC, C> context, SC config) {
            this.context = context;
            this.parentDimensionCount = context.parentDimensionCount;
            this.config = config;
            this.name = MetricName.of(context.parentName, config.name());
            this.predicate = config.predicate();
            this.dimensions = config.hasDimensions() ? config.dimensions() : emptyList();
            this.dimensionalInstanceAutoRemovalEnabled = config.hasEffectiveMaxDimensionalInstances() || config.isDimensionalInstanceExpirationEnabled();
            this.dimensionalInstanceExpirationEnabled = config.isDimensionalInstanceExpirationEnabled();

            if (this.dimensionalInstanceAutoRemovalEnabled) {
                this.dimensionalInstanceExpirationTimeMs =
                    this.dimensionalInstanceExpirationEnabled ?
                    config.dimensionalInstanceExpirationTime().toMillis() :
                    INFINITE_DIMENSIONAL_INSTANCE_EXPIRATION_TIME_MS;
            } else {
                this.dimensionalInstanceExpirationTimeMs = 0L;
            }

            this.instanceExpirationManager =
                this.dimensionalInstanceAutoRemovalEnabled ?
                new InstanceExpirationManager(
                    config.hasMaxDimensionalInstances() ? config.maxDimensionalInstances() : Integer.MAX_VALUE,
                    context.executor) :
                null;

            if (!this.dimensions.isEmpty()) {
                this.dimensionsMask = new MetricDimension[this.parentDimensionCount];

                for (int i = 0; i < this.parentDimensionCount; ++i) {
                    MetricDimension dimension = context.parentDimension(i);
                    this.dimensionsMask[i] = this.dimensions.contains(dimension) ? dimension : null;
                }
            } else {
                this.dimensionsMask = null;
            }

            this.measurableValueProviders = context.measurableValueProvidersProvider.valueProvidersFor(config.measurables());

            if (config.isTotalEnabled()) {
                IC totalInstanceConfig = config.totalInstanceConfig();

                List<MetricDimensionValue> dimensionValues =
                    context.hasPrefixDimensionValues() ?
                    context.parentConfig.prefixDimensionValues().list() :
                    emptyList();

                if (totalInstanceConfig != null) {
                    Map<Measurable, MeasurableValueProvider<MI>> mvps =
                        totalInstanceConfig.hasMeasurables() ?
                        context.measurableValueProvidersProvider.valueProvidersFor(totalInstanceConfig.measurables()) :
                        this.measurableValueProviders;

                    MI meterImpl = context.meterImplMaker.makeMeterImpl(
                        totalInstanceConfig,
                        config,
                        context.parentConfig,
                        mvps.keySet());

                    this.totalInstance = context.instanceMaker.makeInstance(
                        totalInstanceConfig.hasName() ? MetricName.of(this.name, totalInstanceConfig.name()) : this.name,
                        dimensionValues,
                        true,
                        !this.dimensions.isEmpty(),
                        false,
                        mvps,
                        meterImpl);
                } else {
                    MI meterImpl = context.meterImplMaker.makeMeterImpl(
                        null,
                        config,
                        context.parentConfig,
                        this.measurableValueProviders.keySet());

                    this.totalInstance = context.instanceMaker.makeInstance(
                        this.name,
                        dimensionValues,
                        true,
                        !this.dimensions.isEmpty(),
                        false,
                        this.measurableValueProviders,
                        meterImpl);
                }
            } else {
                this.totalInstance = null;
            }

            this.instances = this.dimensions.isEmpty() ? null : new ConcurrentHashMap<>();
            int dimensionCount = this.dimensions.size();

            if (config.areLevelsEnabled() && dimensionCount > 1) {
                this.levelsInstances = new ConcurrentHashMap<>();
                this.levelsDimensionsMasks = new ArrayList<>(dimensionCount - 1);
                this.levelsMeasurableValueProviders = new ArrayList<>(dimensionCount - 1);

                for (int i = 0; i < dimensionCount - 1; ++i) {
                    MetricDimension dimension = this.dimensions.get(i);

                    if (config.areOnlyConfiguredLevelsEnabled() && !config.hasLevelInstanceConfigFor(dimension)) {
                        this.levelsDimensionsMasks.add(null);
                        continue;
                    }

                    MetricDimension[] levelDimensionsMask = new MetricDimension[this.parentDimensionCount];
                    int k = i;

                    for (int j = 0; j < this.parentDimensionCount; ++j) {
                        MetricDimension parentDimension = context.parentDimension(j);

                        if (this.dimensions.contains(parentDimension) && k-- >= 0) {
                            levelDimensionsMask[j] = parentDimension;
                        } else {
                            levelDimensionsMask[j] = null;
                        }
                    }

                    this.levelsDimensionsMasks.add(levelDimensionsMask);
                    Map<Measurable, MeasurableValueProvider<MI>> levelMeasurableValueProviders;
                    IC levelInstanceConfig = config.levelInstanceConfigs().get(dimension);

                    if (levelInstanceConfig != null && levelInstanceConfig.hasMeasurables()) {
                        levelMeasurableValueProviders = context.measurableValueProvidersProvider.valueProvidersFor(levelInstanceConfig.measurables());
                    } else if (config.hasDefaultLevelInstanceConfig() && config.defaultLevelInstanceConfig().hasMeasurables()) {
                        levelMeasurableValueProviders = context.measurableValueProvidersProvider.valueProvidersFor(config.defaultLevelInstanceConfig().measurables());
                    } else {
                        levelMeasurableValueProviders = this.measurableValueProviders;
                    }

                    this.levelsMeasurableValueProviders.add(levelMeasurableValueProviders);
                }
            } else {
                this.levelsInstances = null;
                this.levelsDimensionsMasks = null;
                this.levelsMeasurableValueProviders = null;
            }
        }

        boolean matches(MetricDimensionValues dimensionValues) {
            return predicate == null || predicate.matches(dimensionValues);
        }

        void update(long value, List<MetricDimensionValue> dimensionValues, long updateTimeMs) {
            if (totalInstance != null) {
                totalInstance.update(value, context.meterImplUpdater);
            }

            if (levelsInstances != null) {
                for (int i = 0; i < levelsDimensionsMasks.size(); ++i) {
                    MetricDimension[] levelDimensionsMask = levelsDimensionsMasks.get(i);

                    if (levelDimensionsMask == null) {
                        continue;
                    }

                    InstanceKey instanceKey = new InstanceKey(dimensionValues, levelDimensionsMask);
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
                                List<MetricDimensionValue> instanceDimensionValues;

                                if (context.hasPrefixDimensionValues()) {
                                    MetricDimensionValues prefixDimensionValues = context.parentConfig.prefixDimensionValues();
                                    instanceDimensionValues = new ArrayList<>(prefixDimensionValues.size() + i2 + 1);
                                    instanceDimensionValues.addAll(prefixDimensionValues.list());
                                } else {
                                    instanceDimensionValues = new ArrayList<>(i2 + 1);
                                }

                                for (int j = 0; j < levelDimensionsMask.length; ++j) {
                                    if (levelDimensionsMask[j] != null) {
                                        instanceDimensionValues.add(dimensionValues.get(j));
                                    }
                                }

                                MetricName nameSuffix = null;
                                IC instanceConfig = config.levelInstanceConfigs().getOrDefault(dimensions.get(i2), config.defaultLevelInstanceConfig());

                                if (instanceConfig != null && instanceConfig.hasName()) {
                                    nameSuffix = instanceConfig.name();
                                } else if (config.hasLevelInstanceNameProvider()) {
                                    nameSuffix = config.levelInstanceNameProvider().nameForLevelInstance(instanceDimensionValues);

                                    if (nameSuffix == null && config.hasDefaultLevelInstanceConfig() && config.defaultLevelInstanceConfig().hasName()) {
                                        nameSuffix = config.defaultLevelInstanceConfig().name();
                                    }
                                }

                                AbstractMeterInstance<MI> newInstance;
                                MetricName instanceName = nameSuffix != null ? MetricName.of(name, nameSuffix) : name;
                                Map<Measurable, MeasurableValueProvider<MI>> mvps = levelsMeasurableValueProviders.get(i2);
                                MI meterImpl = context.meterImplMaker.makeMeterImpl(instanceConfig, config, context.parentConfig, mvps.keySet());

                                if (dimensionalInstanceAutoRemovalEnabled) {
                                    newInstance = context.instanceMaker.makeExpirableInstance(
                                        instanceName,
                                        instanceDimensionValues,
                                        false,
                                        false,
                                        true,
                                        mvps,
                                        meterImpl,
                                        updateTimeMs);
                                } else {
                                    newInstance = context.instanceMaker.makeInstance(
                                        instanceName,
                                        instanceDimensionValues,
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
                InstanceKey instanceKey = new InstanceKey(dimensionValues, dimensionsMask);
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
                            List<MetricDimensionValue> instanceDimensionValues;

                            if (context.hasPrefixDimensionValues()) {
                                MetricDimensionValues prefixDimensionValues = context.parentConfig.prefixDimensionValues();
                                instanceDimensionValues = new ArrayList<>(prefixDimensionValues.size() + dimensions.size());
                                instanceDimensionValues.addAll(prefixDimensionValues.list());
                            } else {
                                instanceDimensionValues = new ArrayList<>(dimensions.size());
                            }

                            for (int i = 0; i < dimensionValues.size(); ++i) {
                                if (dimensionsMask[i] != null) {
                                    instanceDimensionValues.add(dimensionValues.get(i));
                                }
                            }

                            AbstractMeterInstance<MI> newInstance;

                            MI meterImpl = context.meterImplMaker.makeMeterImpl(
                                null,
                                config,
                                context.parentConfig,
                                measurableValueProviders.keySet());

                            if (dimensionalInstanceAutoRemovalEnabled) {
                                newInstance = context.instanceMaker.makeExpirableInstance(
                                    name,
                                    instanceDimensionValues,
                                    false,
                                    false,
                                    false,
                                    measurableValueProviders,
                                    meterImpl,
                                    updateTimeMs);
                            } else {
                                newInstance = context.instanceMaker.makeInstance(
                                    name,
                                    instanceDimensionValues,
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

            if (dimensionalInstanceExpirationEnabled) {
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

            if (dimensionalInstanceAutoRemovalEnabled) {
                AbstractExpirableMeterInstance<MI> expirableInstance = (AbstractExpirableMeterInstance<MI>)instance;
                long instanceUpdateTimeMs = expirableInstance.updateTimeMs();
                long instanceExpirationTimeMs = instanceUpdateTimeMs + dimensionalInstanceExpirationTimeMs;

                instanceExpirationManager.addInstanceExpiration(new InstanceExpiration(
                    this,
                    instanceKey,
                    expirableInstance,
                    instanceExpirationTimeMs,
                    instanceUpdateTimeMs));
            }

            context.forEachListener(l -> l.metricInstanceAdded(instance));
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
            }
        }

        public void removeInstancesFor(MetricDimensionValues dimensionValues) {
            if (dimensionsMask != null) {
                context.execute(() -> {
                    InstanceKey key = new InstanceKey(dimensionValues.list(), dimensionsMask);
                    removeInstance(key);
                });
            }
        }

        @Override
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

            return Integer.compare(right.instance.dimensionValues().size(), instance.dimensionValues().size());
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
                        instanceUpdateTimeMs + ie.slice.dimensionalInstanceExpirationTimeMs,
                        instanceUpdateTimeMs));
                }

                ie = queue.peek();
                minInstanceExpirationTimeMs = ie != null ? ie.expirationTimeMs : 0L;
            }

            expiredInstancesRemovalSubmitted.set(false);
        }
    }
}
