package com.ringcentral.platform.metrics.counter.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterInstanceConfig;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.Set;

public class DefaultCounterInstanceConfig extends AbstractMeterInstanceConfig implements CounterInstanceConfig {

    public DefaultCounterInstanceConfig(
        MetricName name,
        Set<? extends Measurable> measurables,
        MetricContext context) {

        super(name, measurables, context);
    }
}
