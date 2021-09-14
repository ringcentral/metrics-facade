package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.time.Duration;
import java.util.*;

import static com.ringcentral.platform.metrics.UnmodifiableMetricContext.emptyUnmodifiableMetricContext;
import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;

public abstract class AbstractMeterSliceConfig<IC extends MeterInstanceConfig> implements MeterSliceConfig<IC> {

    private final boolean enabled;
    private final MetricName name;

    private final MetricDimensionValuesPredicate predicate;
    private final List<MetricDimension> dimensions;
    private final Integer maxDimensionalInstances;
    private final Duration dimensionalInstanceExpirationTime;

    private final Set<? extends Measurable> measurables;

    private final boolean totalEnabled;
    private final IC totalInstanceConfig;
    private final boolean levelsEnabled;
    private final LevelInstanceNameProvider levelInstanceNameProvider;
    private final Map<MetricDimension, IC> levelInstanceConfigs;
    private final IC defaultLevelInstanceConfig;
    private final boolean onlyConfiguredLevelsEnabled;

    private final MetricContext context;

    protected AbstractMeterSliceConfig(
        boolean enabled,
        MetricName name,
        MetricDimensionValuesPredicate predicate,
        List<MetricDimension> dimensions,
        Integer maxDimensionalInstances,
        Duration dimensionalInstanceExpirationTime,
        Set<? extends Measurable> measurables,
        boolean totalEnabled,
        IC totalInstanceConfig,
        boolean levelsEnabled,
        LevelInstanceNameProvider levelInstanceNameProvider,
        Map<MetricDimension, IC> levelInstanceConfigs,
        IC defaultLevelInstanceConfig,
        boolean onlyConfiguredLevelsEnabled,
        MetricContext context) {

        this.enabled = enabled;
        this.name = requireNonNull(name);

        this.predicate = predicate;
        this.dimensions = dimensions != null ? dimensions : emptyList();
        this.maxDimensionalInstances = maxDimensionalInstances;
        this.dimensionalInstanceExpirationTime = dimensionalInstanceExpirationTime;

        this.measurables = measurables != null ? measurables : emptySet();

        this.totalEnabled = totalEnabled;
        this.totalInstanceConfig = totalInstanceConfig;
        this.levelsEnabled = levelsEnabled;
        this.levelInstanceNameProvider = levelInstanceNameProvider;
        this.levelInstanceConfigs = levelInstanceConfigs != null ? levelInstanceConfigs : emptyMap();
        this.defaultLevelInstanceConfig = defaultLevelInstanceConfig;
        this.onlyConfiguredLevelsEnabled = onlyConfiguredLevelsEnabled;

        this.context = context != null ? context : emptyUnmodifiableMetricContext();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public MetricName name() {
        return name;
    }

    @Override
    public MetricDimensionValuesPredicate predicate() {
        return predicate;
    }

    @Override
    public List<MetricDimension> dimensions() {
        return dimensions;
    }

    @Override
    public Integer maxDimensionalInstances() {
        return maxDimensionalInstances;
    }

    @Override
    public Duration dimensionalInstanceExpirationTime() {
        return dimensionalInstanceExpirationTime;
    }

    @Override
    public Set<? extends Measurable> measurables() {
        return measurables;
    }

    @Override
    public boolean isTotalEnabled() {
        return totalEnabled;
    }

    @Override
    public IC totalInstanceConfig() {
        return totalInstanceConfig;
    }

    @Override
    public boolean areLevelsEnabled() {
        return levelsEnabled;
    }

    @Override
    public LevelInstanceNameProvider levelInstanceNameProvider() {
        return levelInstanceNameProvider;
    }

    @Override
    public Map<MetricDimension, IC> levelInstanceConfigs() {
        return levelInstanceConfigs;
    }

    @Override
    public IC defaultLevelInstanceConfig() {
        return defaultLevelInstanceConfig;
    }

    @Override
    public boolean areOnlyConfiguredLevelsEnabled() {
        return onlyConfiguredLevelsEnabled;
    }

    @Override
    public MetricContext context() {
        return context;
    }
}
