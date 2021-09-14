package com.ringcentral.platform.metrics.timer.configs.builders;

import java.util.Set;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.InstanceConfigBuilder;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.timer.TimerMeasurable;
import com.ringcentral.platform.metrics.timer.configs.*;

public class TimerInstanceConfigBuilder extends InstanceConfigBuilder<
    TimerMeasurable,
    TimerInstanceConfig,
    TimerInstanceConfigBuilder> {

    public static TimerInstanceConfigBuilder timerInstance() {
        return new TimerInstanceConfigBuilder();
    }

    public static TimerInstanceConfigBuilder timerInstance(String... nameParts) {
        return new TimerInstanceConfigBuilder(MetricName.of(nameParts));
    }

    public static TimerInstanceConfigBuilder timerInstance(MetricName name) {
        return new TimerInstanceConfigBuilder(name);
    }

    public static TimerInstanceConfigBuilder timerInstanceConfigBuilder() {
        return new TimerInstanceConfigBuilder();
    }

    public TimerInstanceConfigBuilder() {
        this(null);
    }

    public TimerInstanceConfigBuilder(MetricName name) {
        super(name, TimerMeasurable.class);
    }

    @Override
    public TimerInstanceConfig buildImpl(
        MetricName name,
        Set<TimerMeasurable> measurables,
        MetricContext context) {

        return new DefaultTimerInstanceConfig(
            name,
            measurables,
            context);
    }
}