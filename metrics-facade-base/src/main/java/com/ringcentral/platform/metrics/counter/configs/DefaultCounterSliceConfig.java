package com.ringcentral.platform.metrics.counter.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterSliceConfig;
import com.ringcentral.platform.metrics.counter.CounterMeasurable;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.names.MetricName;

import java.time.Duration;
import java.util.*;

public class DefaultCounterSliceConfig extends AbstractMeterSliceConfig<CounterInstanceConfig> implements CounterSliceConfig {

    public DefaultCounterSliceConfig(
        boolean enabled,
        MetricName name,
        MetricDimensionValuesPredicate predicate,
        List<MetricDimension> dimensions,
        Integer maxDimensionalInstances,
        Duration dimensionalInstanceExpirationTime,
        Set<CounterMeasurable> measurables,
        boolean totalEnabled,
        CounterInstanceConfig totalInstanceConfig,
        boolean levelsEnabled,
        LevelInstanceNameProvider levelInstanceNameProvider,
        Map<MetricDimension, CounterInstanceConfig> levelInstanceConfigs,
        CounterInstanceConfig defaultLevelInstanceConfig,
        boolean onlyConfiguredLevelsEnabled,
        MetricContext context) {

        super(
            enabled,
            name,
            predicate,
            dimensions,
            maxDimensionalInstances,
            dimensionalInstanceExpirationTime,
            measurables,
            totalEnabled,
            totalInstanceConfig,
            levelsEnabled,
            levelInstanceNameProvider,
            levelInstanceConfigs,
            defaultLevelInstanceConfig,
            onlyConfiguredLevelsEnabled,
            context);
    }
}
