package com.ringcentral.platform.metrics.counter.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.MeterSliceConfig.LevelInstanceNameProvider;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.AllSliceConfigBuilder;
import com.ringcentral.platform.metrics.counter.CounterMeasurable;
import com.ringcentral.platform.metrics.counter.configs.*;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.names.MetricName;

import java.time.Duration;
import java.util.*;

public class CounterAllSliceConfigBuilder extends AllSliceConfigBuilder<
    CounterMeasurable,
    CounterInstanceConfig,
    CounterSliceConfig,
    CounterConfig,
    CounterAllSliceConfigBuilder,
    CounterSliceConfigBuilder,
    CounterConfigBuilder> {

    public CounterAllSliceConfigBuilder(CounterConfigBuilder builder, MetricName name) {
        super(builder, name, CounterMeasurable.class);
    }

    @Override
    public CounterSliceConfig buildImpl(
        boolean enabled,
        MetricName name,
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

        return new DefaultCounterSliceConfig(
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
