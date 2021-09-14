package com.ringcentral.platform.metrics.timer.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.MeterSliceConfig.LevelInstanceNameProvider;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.SliceConfigBuilder;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.timer.TimerMeasurable;
import com.ringcentral.platform.metrics.timer.configs.*;

import java.time.Duration;
import java.util.*;

public class TimerSliceConfigBuilder extends SliceConfigBuilder<
    TimerMeasurable,
    TimerInstanceConfig,
    TimerSliceConfig,
    TimerConfig,
    TimerAllSliceConfigBuilder,
    TimerSliceConfigBuilder,
    TimerConfigBuilder> {

    public TimerSliceConfigBuilder(TimerConfigBuilder builder, MetricName name) {
        super(builder, name, TimerMeasurable.class);
    }

    @Override
    public TimerSliceConfig buildImpl(
        boolean enabled,
        MetricName name,
        MetricDimensionValuesPredicate predicate,
        List<MetricDimension> dimensions,
        Integer maxDimensionalInstances,
        Duration dimensionalInstanceExpirationTime,
        Set<TimerMeasurable> measurables,
        boolean totalEnabled,
        TimerInstanceConfig totalInstanceConfig,
        boolean levelsEnabled,
        LevelInstanceNameProvider levelInstanceNameProvider,
        Map<MetricDimension, TimerInstanceConfig> levelInstanceConfigs,
        TimerInstanceConfig defaultLevelInstanceConfig,
        boolean onlyConfiguredLevelsEnabled,
        MetricContext context) {

        return new DefaultTimerSliceConfig(
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
