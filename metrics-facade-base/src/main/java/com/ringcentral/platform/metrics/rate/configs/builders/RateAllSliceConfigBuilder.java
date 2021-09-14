package com.ringcentral.platform.metrics.rate.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.MeterSliceConfig.LevelInstanceNameProvider;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.AllSliceConfigBuilder;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateMeasurable;
import com.ringcentral.platform.metrics.rate.configs.*;

import java.time.Duration;
import java.util.*;

public class RateAllSliceConfigBuilder extends AllSliceConfigBuilder<
    RateMeasurable,
    RateInstanceConfig,
    RateSliceConfig,
    RateConfig,
    RateAllSliceConfigBuilder,
    RateSliceConfigBuilder,
    RateConfigBuilder> {

    public RateAllSliceConfigBuilder(RateConfigBuilder builder, MetricName name) {
        super(builder, name, RateMeasurable.class);
    }

    @Override
    public RateSliceConfig buildImpl(
        boolean enabled,
        MetricName name,
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
            null,
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
