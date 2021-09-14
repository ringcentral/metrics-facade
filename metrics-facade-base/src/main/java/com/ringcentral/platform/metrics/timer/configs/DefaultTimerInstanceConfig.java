package com.ringcentral.platform.metrics.timer.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterInstanceConfig;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.Set;

public class DefaultTimerInstanceConfig extends AbstractMeterInstanceConfig implements TimerInstanceConfig {

    public DefaultTimerInstanceConfig(
        MetricName name,
        Set<? extends Measurable> measurables,
        MetricContext context) {

        super(name, measurables, context);
    }
}
