package com.ringcentral.platform.metrics.rate.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterSliceConfig;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateMeasurable;

import java.time.Duration;
import java.util.*;

public class DefaultRateSliceConfig extends AbstractMeterSliceConfig<RateInstanceConfig> implements RateSliceConfig {

    public DefaultRateSliceConfig(
        boolean enabled,
        MetricName name,
        MetricDimensionValuesPredicate predicate,
        List<MetricDimension> dimensions,
        Integer maxDimensionalInstances,
        Duration dimensionalInstanceExpirationTime,
        Set<RateMeasurable> measurables,
        boolean totalEnabled,
        RateInstanceConfig totalInstanceConfig,
        boolean levelsEnabled,
        LevelInstanceNameProvider levelInstanceNameProvider,
        Map<MetricDimension, RateInstanceConfig> levelInstanceConfigs,
        RateInstanceConfig defaultLevelInstanceConfig,
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
