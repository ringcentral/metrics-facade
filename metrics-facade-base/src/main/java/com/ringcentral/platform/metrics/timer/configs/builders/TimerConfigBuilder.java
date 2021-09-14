package com.ringcentral.platform.metrics.timer.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.timer.TimerMeasurable;
import com.ringcentral.platform.metrics.timer.configs.*;

import java.util.*;

public class TimerConfigBuilder extends AbstractMeterConfigBuilder<
    TimerMeasurable,
    TimerInstanceConfig,
    TimerSliceConfig,
    TimerConfig,
    TimerAllSliceConfigBuilder,
    TimerSliceConfigBuilder,
    TimerConfigBuilder> {

    public static TimerConfigBuilder timer() {
        return timerConfigBuilder();
    }

    public static TimerConfigBuilder withTimer() {
        return timerConfigBuilder();
    }

    public static TimerConfigBuilder timerConfigBuilder() {
        return new TimerConfigBuilder();
    }

    public TimerConfigBuilder() {
        super(TimerMeasurable.class);
    }

    @Override
    protected TimerAllSliceConfigBuilder makeAllSliceConfigBuilder(TimerConfigBuilder builder, MetricName name) {
        return new TimerAllSliceConfigBuilder(builder, name);
    }

    @Override
    protected TimerSliceConfigBuilder makeSliceConfigBuilder(TimerConfigBuilder builder, MetricName name) {
        return new TimerSliceConfigBuilder(builder, name);
    }

    @Override
    protected InstanceConfigBuilder<TimerMeasurable, TimerInstanceConfig, ?> makeInstanceConfigBuilder() {
        return new TimerInstanceConfigBuilder();
    }

    @Override
    protected TimerConfig buildImpl(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricDimensionValuesPredicate exclusionPredicate,
        TimerSliceConfig allSliceConfig,
        Set<TimerSliceConfig> sliceConfigs,
        MetricContext context) {

        return new DefaultTimerConfig(
            enabled,
            prefixDimensionValues,
            dimensions,
            exclusionPredicate,
            allSliceConfig,
            sliceConfigs,
            context);
    }
}
