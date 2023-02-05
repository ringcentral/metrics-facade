package com.ringcentral.platform.metrics.timer.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.MeterSliceConfig.LevelInstanceNameProvider;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.AllSliceConfigBuilder;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.timer.TimerMeasurable;
import com.ringcentral.platform.metrics.timer.configs.*;

import java.time.Duration;
import java.util.*;

public class TimerAllSliceConfigBuilder extends AllSliceConfigBuilder<
    TimerMeasurable,
    TimerInstanceConfig,
    TimerSliceConfig,
    TimerConfig,
    TimerAllSliceConfigBuilder,
    TimerSliceConfigBuilder,
    TimerConfigBuilder> {

    public TimerAllSliceConfigBuilder(TimerConfigBuilder builder, MetricName name) {
        super(builder, name, TimerMeasurable.class);
    }

    @Override
    public TimerSliceConfig buildImpl(
        boolean enabled,
        MetricName name,
        List<Label> labels,
        Integer maxLabeledInstances,
        Duration labeledInstanceExpirationTime,
        Set<TimerMeasurable> measurables,
        boolean totalEnabled,
        TimerInstanceConfig totalInstanceConfig,
        boolean levelsEnabled,
        LevelInstanceNameProvider levelInstanceNameProvider,
        Map<Label, TimerInstanceConfig> levelInstanceConfigs,
        TimerInstanceConfig defaultLevelInstanceConfig,
        boolean onlyConfiguredLevelsEnabled,
        MetricContext context) {

        return new DefaultTimerSliceConfig(
            enabled,
            name,
            null,
            labels,
            maxLabeledInstances,
            labeledInstanceExpirationTime,
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
