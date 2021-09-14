package com.ringcentral.platform.metrics.rate.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.MeterSliceConfig.LevelInstanceNameProvider;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.SliceConfigBuilder;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateMeasurable;
import com.ringcentral.platform.metrics.rate.configs.*;

import java.time.Duration;
import java.util.*;

public class RateSliceConfigBuilder extends SliceConfigBuilder<
    RateMeasurable,
    RateInstanceConfig,
    RateSliceConfig,
    RateConfig,
    RateAllSliceConfigBuilder,
    RateSliceConfigBuilder,
    RateConfigBuilder> {

    public RateSliceConfigBuilder(RateConfigBuilder builder, MetricName name) {
        super(builder, name, RateMeasurable.class);
    }

    @Override
    public RateSliceConfig buildImpl(
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

        return new DefaultRateSliceConfig(
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
