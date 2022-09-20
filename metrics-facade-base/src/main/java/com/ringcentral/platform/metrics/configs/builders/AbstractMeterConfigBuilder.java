package com.ringcentral.platform.metrics.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.ModifiableMetricContext;
import com.ringcentral.platform.metrics.configs.MeterConfig;
import com.ringcentral.platform.metrics.configs.MeterInstanceConfig;
import com.ringcentral.platform.metrics.configs.MeterSliceConfig;
import com.ringcentral.platform.metrics.configs.MeterSliceConfig.LevelInstanceNameProvider;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValuesPredicate;
import com.ringcentral.platform.metrics.impl.MetricImplConfigBuilder;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.ringcentral.platform.metrics.names.MetricName.emptyMetricName;
import static com.ringcentral.platform.metrics.utils.CollectionUtils.containsAllInOrder;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkState;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings({ "unchecked", "unused", "BooleanMethodIsAlwaysInverted" })
public abstract class AbstractMeterConfigBuilder<
    M extends Measurable,
    IC extends MeterInstanceConfig,
    SC extends MeterSliceConfig<IC>,
    C extends MeterConfig<IC, SC>,
    ASCB extends AbstractMeterConfigBuilder.AllSliceConfigBuilder<M, IC, SC, C, ASCB, SCB, CB>,
    SCB extends AbstractMeterConfigBuilder.SliceConfigBuilder<M, IC, SC, C, ASCB, SCB, CB>,
    CB extends AbstractMeterConfigBuilder<M, IC, SC, C, ASCB, SCB, CB>>
        extends AbstractMetricConfigBuilder<C, CB> implements MetricConfigBuilder<C> {

    protected static abstract class AbstractSliceConfigBuilder<
        M extends Measurable,
        IC extends MeterInstanceConfig,
        SC extends MeterSliceConfig<IC>,
        C extends MeterConfig<IC, SC>,
        ASCB extends AllSliceConfigBuilder<M, IC, SC, C, ASCB, SCB, CB>,
        SCB extends SliceConfigBuilder<M, IC, SC, C, ASCB, SCB, CB>,
        CB extends AbstractMeterConfigBuilder<M, IC, SC, C, ASCB, SCB, CB>,
        Impl extends AbstractSliceConfigBuilder<M, IC, SC, C, ASCB, SCB, CB, Impl>>
            implements MetricConfigBuilderProvider<CB> {

        final CB builder;

        Boolean enabled;
        MetricName name;

        List<MetricDimension> dimensions;
        Integer maxDimensionalInstances;
        Duration dimensionalInstanceExpirationTime;

        final Class<M> measurableType;
        Set<M> measurables;

        Boolean totalEnabled;
        InstanceConfigBuilder<M, IC, ?> totalInstanceConfigBuilder;

        Boolean levelsEnabled;
        LevelInstanceNameProvider levelInstanceNameProvider;
        Map<MetricDimension, InstanceConfigBuilder<M, IC, ?>> levelInstanceConfigBuilders;
        InstanceConfigBuilder<M, IC, ?> defaultLevelInstanceConfigBuilder;
        Boolean onlyConfiguredLevelsEnabled;

        final ModifiableMetricContext context = new ModifiableMetricContext();

        protected AbstractSliceConfigBuilder(CB builder, MetricName name, Class<M> measurableType) {
            this.builder = requireNonNull(builder);
            this.name = name;
            this.measurableType = requireNonNull(measurableType);
        }

        protected void rebase(AbstractSliceConfigBuilder<?, ?, ?, ?, ?, ?, ?, ?> base) {
            if (base.enabled != null && enabled == null) {
                enabled = base.enabled;
            }

            if (base.name != null && name == null) {
                name = base.name;
            }

            if (base.dimensions != null && dimensions == null) {
                dimensions = base.dimensions;
            }

            if (base.maxDimensionalInstances != null && maxDimensionalInstances == null) {
                maxDimensionalInstances = base.maxDimensionalInstances;
            }

            if (base.dimensionalInstanceExpirationTime != null && dimensionalInstanceExpirationTime == null) {
                dimensionalInstanceExpirationTime = base.dimensionalInstanceExpirationTime;
            }

            if (base.measurableType == measurableType
                && base.measurables != null
                && measurables == null) {

                measurables = (Set<M>)base.measurables;
            }

            if (base.totalEnabled != null && totalEnabled == null) {
                totalEnabled = base.totalEnabled;
            }

            if (base.totalInstanceConfigBuilder != null) {
                totalInstanceConfigBuilder =
                    totalInstanceConfigBuilder != null ?
                    totalInstanceConfigBuilder.rebase(base.totalInstanceConfigBuilder) :
                    builder.makeInstanceConfigBuilder().rebase(base.totalInstanceConfigBuilder);
            }

            if (base.levelsEnabled != null && levelsEnabled == null) {
                levelsEnabled = base.levelsEnabled;
            }

            if (base.levelInstanceNameProvider != null && levelInstanceNameProvider == null) {
                levelInstanceNameProvider = base.levelInstanceNameProvider;
            }

            if (base.levelInstanceConfigBuilders != null) {
                if (levelInstanceConfigBuilders == null) {
                    levelInstanceConfigBuilders = base.levelInstanceConfigBuilders.entrySet().stream().collect(
                        toMap(Map.Entry::getKey, e -> builder.makeInstanceConfigBuilder().rebase(e.getValue())));
                } else {
                    base.levelInstanceConfigBuilders.forEach((d, b) -> {
                        InstanceConfigBuilder<M, IC, ?> newLicb =
                            levelInstanceConfigBuilders.containsKey(d) ?
                            levelInstanceConfigBuilders.remove(d).rebase(b) :
                            builder.makeInstanceConfigBuilder().rebase(b);

                        levelInstanceConfigBuilders.put(d, newLicb);
                    });
                }
            }

            if (base.defaultLevelInstanceConfigBuilder != null) {
                defaultLevelInstanceConfigBuilder =
                    defaultLevelInstanceConfigBuilder != null ?
                    defaultLevelInstanceConfigBuilder.rebase(base.defaultLevelInstanceConfigBuilder) :
                    builder.makeInstanceConfigBuilder().rebase(base.defaultLevelInstanceConfigBuilder);
            }

            if (base.onlyConfiguredLevelsEnabled != null && onlyConfiguredLevelsEnabled == null) {
                onlyConfiguredLevelsEnabled = base.onlyConfiguredLevelsEnabled;
            }

            context.putIfAbsent(base.context);
        }

        protected void modify(AbstractSliceConfigBuilder<?, ?, ?, ?, ?, ?, ?, ?> mod) {
            if (mod.enabled != null) {
                enabled = mod.enabled;
            }

            if (mod.name != null) {
                name = mod.name;
            }

            if (mod.dimensions != null) {
                dimensions = mod.dimensions;
            }

            if (mod.maxDimensionalInstances != null) {
                maxDimensionalInstances = mod.maxDimensionalInstances;
            }

            if (mod.dimensionalInstanceExpirationTime != null) {
                dimensionalInstanceExpirationTime = mod.dimensionalInstanceExpirationTime;
            }

            if (mod.measurableType == measurableType && mod.measurables != null) {
                measurables = (Set<M>)mod.measurables;
            }

            if (mod.totalEnabled != null) {
                totalEnabled = mod.totalEnabled;
            }

            if (mod.totalInstanceConfigBuilder != null) {
                totalInstanceConfigBuilder =
                    totalInstanceConfigBuilder != null ?
                    totalInstanceConfigBuilder.modify(mod.totalInstanceConfigBuilder) :
                    builder.makeInstanceConfigBuilder().modify(mod.totalInstanceConfigBuilder);
            }

            if (mod.levelsEnabled != null) {
                levelsEnabled = mod.levelsEnabled;
            }

            if (mod.levelInstanceNameProvider != null) {
                levelInstanceNameProvider = mod.levelInstanceNameProvider;
            }

            if (mod.levelInstanceConfigBuilders != null) {
                if (levelInstanceConfigBuilders == null) {
                    levelInstanceConfigBuilders = mod.levelInstanceConfigBuilders.entrySet().stream().collect(
                        toMap(Map.Entry::getKey, e -> builder.makeInstanceConfigBuilder().modify(e.getValue())));
                } else {
                    mod.levelInstanceConfigBuilders.forEach((d, b) -> {
                        InstanceConfigBuilder<M, IC, ?> newLicb =
                            levelInstanceConfigBuilders.containsKey(d) ?
                            levelInstanceConfigBuilders.remove(d).modify(b) :
                            builder.makeInstanceConfigBuilder().modify(b);

                        levelInstanceConfigBuilders.put(d, newLicb);
                    });
                }
            }

            if (mod.defaultLevelInstanceConfigBuilder != null) {
                defaultLevelInstanceConfigBuilder =
                    defaultLevelInstanceConfigBuilder != null ?
                    defaultLevelInstanceConfigBuilder.modify(mod.defaultLevelInstanceConfigBuilder) :
                    builder.makeInstanceConfigBuilder().modify(mod.defaultLevelInstanceConfigBuilder);
            }

            if (mod.onlyConfiguredLevelsEnabled != null) {
                onlyConfiguredLevelsEnabled = mod.onlyConfiguredLevelsEnabled;
            }

            context.put(mod.context);
        }

        public Impl enable() {
            return enabled(true);
        }

        public Impl disable() {
            return enabled(false);
        }

        public Impl enabled(boolean enabled) {
            this.enabled = enabled;
            return (Impl)this;
        }

        public Impl dimensions(MetricDimension... dimensions) {
            return dimensions(List.of(dimensions));
        }

        public Impl dimensions(List<MetricDimension> dimensions) {
            checkState(this.dimensions == null, "Slice dimensions change is not allowed");
            builder.checkSliceDimensions(dimensions);
            this.dimensions = dimensions;
            return (Impl)this;
        }

        public Impl noMaxDimensionalInstances() {
            return maxDimensionalInstances(Integer.MAX_VALUE);
        }

        public Impl maxDimensionalInstances(Integer maxDimensionalInstances) {
            checkArgument(
                maxDimensionalInstances == null || maxDimensionalInstances > 0,
                "maxDimensionalInstances <= 0");

            this.maxDimensionalInstances = maxDimensionalInstances;
            return (Impl)this;
        }

        public Impl notExpireDimensionalInstances() {
            return expireDimensionalInstanceAfter(Duration.ZERO);
        }

        public Impl expireDimensionalInstanceAfter(long time, ChronoUnit unit) {
            return expireDimensionalInstanceAfter(Duration.of(time, unit));
        }

        public Impl expireDimensionalInstanceAfter(Duration time) {
            this.dimensionalInstanceExpirationTime = time;
            return (Impl)this;
        }

        public Impl measurables(M... measurables) {
            return measurables(Set.of(measurables));
        }

        public Impl noMeasurables() {
            return measurables(emptySet());
        }

        public Impl measurables(Set<M> measurables) {
            this.measurables = measurables;
            return (Impl)this;
        }

        public Impl enableTotal() {
            return totalEnabled(true);
        }

        public Impl disableTotal() {
            return totalEnabled(false);
        }

        public Impl noTotal() {
            return totalEnabled(false);
        }

        public Impl totalEnabled(boolean totalEnabled) {
            this.totalEnabled = totalEnabled;
            return (Impl)this;
        }

        public Impl total(InstanceConfigBuilder<M, IC, ?> instanceConfigBuilder) {
            this.totalInstanceConfigBuilder = instanceConfigBuilder;
            return (Impl)this;
        }

        public Impl enableLevels() {
            return levelsEnabled(true);
        }

        public Impl disableLevels() {
            return levelsEnabled(false);
        }

        public Impl noLevels() {
            return levelsEnabled(false);
        }

        public Impl levelsEnabled(boolean levelsEnabled) {
            this.levelsEnabled = levelsEnabled;
            return (Impl)this;
        }

        public Impl levels(
            LevelInstanceNameProvider levelInstanceNameProvider,
            Map<MetricDimension, InstanceConfigBuilder<M, IC, ?>> levelInstanceConfigBuilders,
            InstanceConfigBuilder<M, IC, ?> defaultLevelInstanceConfigBuilder,
            boolean onlyConfiguredLevelsEnabled) {

            this.levelInstanceNameProvider = levelInstanceNameProvider;
            this.levelInstanceConfigBuilders = levelInstanceConfigBuilders;
            this.defaultLevelInstanceConfigBuilder = defaultLevelInstanceConfigBuilder;
            this.onlyConfiguredLevelsEnabled = onlyConfiguredLevelsEnabled;
            return (Impl)this;
        }

        public Impl put(Object key, Object value) {
            context.put(key, value);
            return (Impl)this;
        }

        public Impl withImpl(MetricImplConfigBuilder configBuilder) {
            return with(configBuilder);
        }

        public Impl with(Object value) {
            context.with(value);
            return (Impl)this;
        }

        @Override
        public CB builder() {
            return builder;
        }

        public SCB slice(String... nameParts) {
            return builder.slice(nameParts);
        }
    }

    public static abstract class AllSliceConfigBuilder<
        M extends Measurable,
        IC extends MeterInstanceConfig,
        SC extends MeterSliceConfig<IC>,
        C extends MeterConfig<IC, SC>,
        ASCB extends AllSliceConfigBuilder<M, IC, SC, C, ASCB, SCB, CB>,
        SCB extends SliceConfigBuilder<M, IC, SC, C, ASCB, SCB, CB>,
        CB extends AbstractMeterConfigBuilder<M, IC, SC, C, ASCB, SCB, CB>>
            extends AbstractSliceConfigBuilder<M, IC, SC, C, ASCB, SCB, CB, ASCB> {

        protected AllSliceConfigBuilder(CB builder, MetricName name, Class<M> measurableType) {
            super(builder, name, measurableType);
        }

        public abstract SC buildImpl(
            boolean enabled,
            MetricName name,
            List<MetricDimension> dimensions,
            Integer maxDimensionalInstances,
            Duration dimensionalInstanceExpirationTime,
            Set<M> measurables,
            boolean totalEnabled,
            IC totalInstanceConfig,
            boolean levelsEnabled,
            LevelInstanceNameProvider levelInstanceNameProvider,
            Map<MetricDimension, IC> levelInstanceConfigs,
            IC defaultLevelInstanceConfig,
            boolean onlyConfiguredLevelsEnabled,
            MetricContext context);
    }

    public static abstract class SliceConfigBuilder<
        M extends Measurable,
        IC extends MeterInstanceConfig,
        SC extends MeterSliceConfig<IC>,
        C extends MeterConfig<IC, SC>,
        ASCB extends AllSliceConfigBuilder<M, IC, SC, C, ASCB, SCB, CB>,
        SCB extends SliceConfigBuilder<M, IC, SC, C, ASCB, SCB, CB>,
        CB extends AbstractMeterConfigBuilder<M, IC, SC, C, ASCB, SCB, CB>>
            extends AbstractSliceConfigBuilder<M, IC, SC, C, ASCB, SCB, CB, SCB> {

        MetricDimensionValuesPredicate predicate;

        protected SliceConfigBuilder(CB builder, MetricName name, Class<M> measurableType) {
            super(builder, requireNonNull(name), measurableType);
        }

        protected SCB rebase(SliceConfigBuilder<?, ?, ?, ?, ?, ?, ?> base) {
            super.rebase(base);

            if (base.predicate != null && predicate == null) {
                predicate = base.predicate;
            }

            return (SCB)this;
        }

        protected SCB modify(SliceConfigBuilder<?, ?, ?, ?, ?, ?, ?> mod) {
            super.modify(mod);

            if (mod.predicate != null) {
                predicate = mod.predicate;
            }

            return (SCB)this;
        }

        public SCB noPredicates() {
            return predicate(null);
        }

        public SCB predicate(MetricDimensionValuesPredicate predicate) {
            this.predicate = predicate;
            return (SCB)this;
        }

        protected abstract SC buildImpl(
            boolean enabled,
            MetricName name,
            MetricDimensionValuesPredicate predicate,
            List<MetricDimension> dimensions,
            Integer maxDimensionalInstances,
            Duration dimensionalInstanceExpirationTime,
            Set<M> measurables,
            boolean totalEnabled,
            IC totalInstanceConfig,
            boolean levelsEnabled,
            LevelInstanceNameProvider levelInstanceNameProvider,
            Map<MetricDimension, IC> levelInstanceConfigs,
            IC defaultLevelInstanceConfig,
            boolean onlyConfiguredLevelsEnabled,
            MetricContext context);
    }

    public static abstract class InstanceConfigBuilder<
        M extends Measurable,
        IC extends MeterInstanceConfig,
        ICB extends InstanceConfigBuilder<M, IC, ICB>> {

        private MetricName name;
        private final Class<M> measurableType;
        private Set<M> measurables;
        private final ModifiableMetricContext context = new ModifiableMetricContext();

        protected InstanceConfigBuilder(MetricName name, Class<M> measurableType) {
            this.name = name;
            this.measurableType = requireNonNull(measurableType);
        }

        protected ICB rebase(InstanceConfigBuilder<?, ?, ?> base) {
            if (name == null && base.name != null) {
                name = base.name;
            }

            if (base.measurableType == measurableType
                && base.measurables != null
                && measurables == null) {

                measurables = (Set<M>)base.measurables;
            }

            context.putIfAbsent(base.context);
            return (ICB)this;
        }

        protected ICB modify(InstanceConfigBuilder<?, ?, ?> mod) {
            if (mod.name != null) {
                name = mod.name;
            }

            if (mod.measurableType == measurableType && mod.measurables != null) {
                measurables = (Set<M>)mod.measurables;
            }

            context.put(mod.context);
            return (ICB)this;
        }

        public ICB name(String... nameParts) {
            return name(MetricName.of(nameParts));
        }

        public ICB name(MetricName name) {
            this.name = name;
            return (ICB)this;
        }

        public ICB measurables(M... measurables) {
            return measurables(Set.of(measurables));
        }

        public ICB noMeasurables() {
            return measurables(emptySet());
        }

        public ICB measurables(Set<M> measurables) {
            this.measurables = measurables;
            return (ICB)this;
        }

        public ICB put(Object key, Object value) {
            context.put(key, value);
            return (ICB)this;
        }

        public ICB withImpl(MetricImplConfigBuilder configBuilder) {
            return with(configBuilder);
        }

        public ICB with(Object value) {
            context.with(value);
            return (ICB)this;
        }

        public IC build() {
            return buildImpl(
                name,
                measurables,
                context.unmodifiable());
        }

        protected abstract IC buildImpl(
            MetricName name,
            Set<M> measurables,
            MetricContext context);
    }

    private List<MetricDimension> dimensions;
    private Integer maxDimensionalInstancesPerSlice;
    private Duration dimensionalInstanceExpirationTime;
    private MetricDimensionValuesPredicate exclusionPredicate;
    private final Class<M> measurableType;
    private Set<M> measurables;
    private ASCB allSliceConfigBuilder;
    private final Map<MetricName, SCB> sliceConfigBuilders = new LinkedHashMap<>();

    protected AbstractMeterConfigBuilder(Class<M> measurableType) {
        this.measurableType = requireNonNull(measurableType);
    }

    @Override
    public void rebase(MetricConfigBuilder<?> base) {
        if (base instanceof AbstractMeterConfigBuilder) {
            AbstractMeterConfigBuilder<?, ?, ?, ?, ?, ?, ?> meterBase = (AbstractMeterConfigBuilder<?, ?, ?, ?, ?, ?, ?>)base;

            if (!areDimensionsCompatible(meterBase)) {
                return;
            }

            if (prefixDimensionValues() == null
                && meterBase.prefixDimensionValues() != null
                && dimensions != null) {

                checkDimensionsUnique(meterBase.prefixDimensionValues(), dimensions);
            }

            super.rebase(meterBase);

            if (meterBase.maxDimensionalInstancesPerSlice != null && maxDimensionalInstancesPerSlice == null) {
                maxDimensionalInstancesPerSlice = meterBase.maxDimensionalInstancesPerSlice;
            }

            if (meterBase.dimensionalInstanceExpirationTime != null && dimensionalInstanceExpirationTime == null) {
                dimensionalInstanceExpirationTime = meterBase.dimensionalInstanceExpirationTime;
            }

            if (meterBase.exclusionPredicate != null && exclusionPredicate == null) {
                exclusionPredicate = meterBase.exclusionPredicate;
            }

            if (meterBase.measurableType == measurableType
                && meterBase.measurables != null
                && measurables == null) {

                measurables = (Set<M>)meterBase.measurables;
            }

            if (meterBase.allSliceConfigBuilder != null) {
                if (allSliceConfigBuilder == null) {
                    allSliceConfigBuilder = makeAllSliceConfigBuilder(builder(), null);
                }

                allSliceConfigBuilder.rebase(meterBase.allSliceConfigBuilder);
            }

            if (!meterBase.sliceConfigBuilders.isEmpty()) {
                meterBase.sliceConfigBuilders.forEach((n, b) -> {
                    SCB newScb =
                        sliceConfigBuilders.containsKey(n) ?
                        sliceConfigBuilders.remove(n).rebase(b) :
                        makeSliceConfigBuilder(builder(), n).rebase(b);

                    sliceConfigBuilders.put(n, newScb);
                });
            }

            context().putIfAbsent(meterBase.context());
        } else {
            super.rebase(base);
        }
    }

    private boolean areDimensionsCompatible(AbstractMeterConfigBuilder<?, ?, ?, ?, ?, ?, ?> that) {
        return that.dimensions == null
            || (dimensions != null && containsAllInOrder(dimensions, that.dimensions));
    }

    @Override
    public void modify(MetricConfigBuilder<?> mod) {
        if (mod instanceof AbstractMeterConfigBuilder) {
            AbstractMeterConfigBuilder<?, ?, ?, ?, ?, ?, ?> meterMod = (AbstractMeterConfigBuilder<?, ?, ?, ?, ?, ?, ?>)mod;

            if (!areDimensionsCompatible(meterMod)) {
                return;
            }

            if (meterMod.prefixDimensionValues() != null && dimensions != null) {
                checkDimensionsUnique(meterMod.prefixDimensionValues(), dimensions);
            }

            super.modify(meterMod);

            if (meterMod.maxDimensionalInstancesPerSlice != null) {
                maxDimensionalInstancesPerSlice = meterMod.maxDimensionalInstancesPerSlice;
            }

            if (meterMod.dimensionalInstanceExpirationTime != null) {
                dimensionalInstanceExpirationTime = meterMod.dimensionalInstanceExpirationTime;
            }

            if (meterMod.exclusionPredicate != null) {
                exclusionPredicate = meterMod.exclusionPredicate;
            }

            if (meterMod.measurableType == measurableType && meterMod.measurables != null) {
                measurables = (Set<M>)meterMod.measurables;
            }

            if (meterMod.allSliceConfigBuilder != null) {
                if (allSliceConfigBuilder == null) {
                    allSliceConfigBuilder = makeAllSliceConfigBuilder(builder(), null);
                }

                allSliceConfigBuilder.modify(meterMod.allSliceConfigBuilder);
            }

            if (!meterMod.sliceConfigBuilders.isEmpty()) {
                meterMod.sliceConfigBuilders.forEach((n, b) -> {
                    SCB newScb =
                        sliceConfigBuilders.containsKey(n) ?
                        sliceConfigBuilders.remove(n).modify(b) :
                        makeSliceConfigBuilder(builder(), n).modify(b);

                    sliceConfigBuilders.put(n, newScb);
                });
            }

            context().put(meterMod.context());
        } else {
            super.modify(mod);
        }
    }

    @Override
    public CB prefix(MetricDimensionValues dimensionValues) {
        checkDimensionsUnique(dimensionValues, dimensions);
        return super.prefix(dimensionValues);
    }

    public CB dimensions(MetricDimension... dimensions) {
        return dimensions(List.of(dimensions));
    }

    public CB dimensions(List<MetricDimension> dimensions) {
        checkState(this.dimensions == null, "Dimensions change is not allowed");
        checkArgument(dimensions != null && !dimensions.isEmpty(), "dimensions is null or empty");
        checkDimensionsUnique(prefixDimensionValues(), dimensions);
        this.dimensions = dimensions;
        return builder();
    }

    List<MetricDimension> dimensions() {
        return dimensions;
    }

    public CB maxDimensionalInstancesPerSlice(Integer maxDimensionalInstancesPerSlice) {
        checkArgument(
            maxDimensionalInstancesPerSlice == null || maxDimensionalInstancesPerSlice > 0,
            "maxDimensionalInstances <= 0");

        this.maxDimensionalInstancesPerSlice = maxDimensionalInstancesPerSlice;
        return builder();
    }

    public CB notExpireDimensionalInstances() {
        return expireDimensionalInstanceAfter(Duration.ZERO);
    }

    public CB expireDimensionalInstanceAfter(long time, ChronoUnit unit) {
        return expireDimensionalInstanceAfter(Duration.of(time, unit));
    }

    public CB expireDimensionalInstanceAfter(Duration time) {
        this.dimensionalInstanceExpirationTime = time;
        return builder();
    }

    public CB noExclusions() {
        return exclude(null);
    }

    public CB exclude(MetricDimensionValuesPredicate exclusionPredicate) {
        this.exclusionPredicate = exclusionPredicate;
        return builder();
    }

    public CB measurables(M... measurables) {
        return measurables(Set.of(measurables));
    }

    public CB noMeasurables() {
        return measurables(emptySet());
    }

    public CB measurables(Set<M> measurables) {
        this.measurables = measurables;
        return builder();
    }

    public ASCB allSlice() {
        return allSlice(null);
    }

    public ASCB allSlice(String namePart, String... nameParts) {
        return allSlice(nameParts.length == 0 ? MetricName.of(namePart) : MetricName.of(MetricName.of(namePart), nameParts));
    }

    public ASCB allSlice(MetricName name) {
        checkState(this.allSliceConfigBuilder == null, "AllSlice change is not allowed");

        if (name != null) {
            checkSliceNamesUnique(name);
        }

        return allSliceConfigBuilder = makeAllSliceConfigBuilder(builder(), name);
    }

    void checkSliceNamesUnique(MetricName name) {
        checkArgument(
            !sliceConfigBuilders.containsKey(name)
                && (allSliceConfigBuilder == null || !Objects.equals(name, allSliceConfigBuilder.name)),
            "Slice names are not unique");
    }

    public SCB slice(String... nameParts) {
        return slice(MetricName.of(nameParts));
    }

    public SCB slice(MetricName name) {
        checkDimensionsConfigured();
        checkSliceNameNotEmpty(name);
        checkSliceNamesUnique(name);
        SCB scb = makeSliceConfigBuilder(builder(), name);
        sliceConfigBuilders.put(name, scb);
        return scb;
    }

    protected void checkDimensionsConfigured() {
        checkState(dimensions != null, "Dimensions are not configured");
    }

    void checkSliceNameNotEmpty(MetricName name) {
        checkArgument(!name.isEmpty(), "Slice name is null or empty");
    }

    void checkSliceDimensions(List<MetricDimension> sliceDimensions) {
        checkArgument(
            sliceDimensions != null && !sliceDimensions.isEmpty(),
            "sliceDimensions is null or empty");

        checkArgument(
            dimensions != null,
            "sliceDimensions = " + sliceDimensions + " is not subsequence of dimensions = null");

        checkArgument(
            containsAllInOrder(dimensions, sliceDimensions),
            "sliceDimensions = " + sliceDimensions + " is not subsequence of dimensions = " + dimensions);
    }

    @Override
    public C build() {
        SC allSliceConfig = buildAllSliceConfig();

        Set<SC> sliceConfigs = sliceConfigBuilders.values().stream().map(scb -> scb.buildImpl(
            scb.enabled != null ? scb.enabled : DEFAULT_ENABLED,
            scb.name,
            scb.predicate,
            scb.dimensions,
            scb.maxDimensionalInstances != null ? scb.maxDimensionalInstances : maxDimensionalInstancesPerSlice,
            scb.dimensionalInstanceExpirationTime != null ? scb.dimensionalInstanceExpirationTime : dimensionalInstanceExpirationTime,
            scb.measurables != null ? scb.measurables : measurables,
            scb.totalEnabled != null ? scb.totalEnabled : true,
            scb.totalInstanceConfigBuilder != null ? scb.totalInstanceConfigBuilder.build() : null,
            scb.levelsEnabled != null ? scb.levelsEnabled : false,
            scb.levelInstanceNameProvider,
            scb.levelInstanceConfigBuilders != null ?
                scb.levelInstanceConfigBuilders.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().build())) :
                emptyMap(),
            scb.defaultLevelInstanceConfigBuilder != null ? scb.defaultLevelInstanceConfigBuilder.build() : null,
            scb.onlyConfiguredLevelsEnabled != null ? scb.onlyConfiguredLevelsEnabled : false,
            scb.context.unmodifiable())).collect(toCollection(LinkedHashSet::new));

        return buildImpl(
            hasEnabled() ? getEnabled() : DEFAULT_ENABLED,
            description(),
            prefixDimensionValues(),
            dimensions,
            exclusionPredicate,
            allSliceConfig,
            sliceConfigs,
            context().unmodifiable());
    }

    SC buildAllSliceConfig() {
        if (allSliceConfigBuilder == null) {
            allSliceConfigBuilder = allSlice();
        }

        return allSliceConfigBuilder.buildImpl(
            allSliceConfigBuilder.enabled != null ? allSliceConfigBuilder.enabled : DEFAULT_ENABLED,
            allSliceConfigBuilder.name != null ? allSliceConfigBuilder.name : emptyMetricName(),
            allSliceConfigBuilder.dimensions != null ? allSliceConfigBuilder.dimensions : dimensions,
            allSliceConfigBuilder.maxDimensionalInstances != null ? allSliceConfigBuilder.maxDimensionalInstances : maxDimensionalInstancesPerSlice,
            allSliceConfigBuilder.dimensionalInstanceExpirationTime != null ? allSliceConfigBuilder.dimensionalInstanceExpirationTime : dimensionalInstanceExpirationTime,
            allSliceConfigBuilder.measurables != null ? allSliceConfigBuilder.measurables : measurables,
            allSliceConfigBuilder.totalEnabled != null ? allSliceConfigBuilder.totalEnabled : true,
            allSliceConfigBuilder.totalInstanceConfigBuilder != null ? allSliceConfigBuilder.totalInstanceConfigBuilder.build() : null,
            allSliceConfigBuilder.levelsEnabled != null ? allSliceConfigBuilder.levelsEnabled : true,
            allSliceConfigBuilder.levelInstanceNameProvider,
            allSliceConfigBuilder.levelInstanceConfigBuilders != null ?
                allSliceConfigBuilder.levelInstanceConfigBuilders.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> e.getValue().build())) :
                emptyMap(),
            allSliceConfigBuilder.defaultLevelInstanceConfigBuilder != null ? allSliceConfigBuilder.defaultLevelInstanceConfigBuilder.build() : null,
            allSliceConfigBuilder.onlyConfiguredLevelsEnabled != null ? allSliceConfigBuilder.onlyConfiguredLevelsEnabled : false,
            allSliceConfigBuilder.context.unmodifiable());
    }

    protected abstract C buildImpl(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricDimensionValuesPredicate exclusionPredicate,
        SC allSliceConfig,
        Set<SC> sliceConfigs,
        MetricContext context);

    protected abstract ASCB makeAllSliceConfigBuilder(CB builder, MetricName name);
    protected abstract SCB makeSliceConfigBuilder(CB builder, MetricName name);
    protected abstract InstanceConfigBuilder<M, IC, ?> makeInstanceConfigBuilder();

    @Override
    public CB builder() {
        return (CB)this;
    }
}