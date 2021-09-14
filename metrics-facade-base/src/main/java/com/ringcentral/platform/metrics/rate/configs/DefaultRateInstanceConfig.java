package com.ringcentral.platform.metrics.rate.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterInstanceConfig;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.Set;

public class DefaultRateInstanceConfig extends AbstractMeterInstanceConfig implements RateInstanceConfig {

    public DefaultRateInstanceConfig(
        MetricName name,
        Set<? extends Measurable> measurables,
        MetricContext context) {

        super(name, measurables, context);
    }
}
