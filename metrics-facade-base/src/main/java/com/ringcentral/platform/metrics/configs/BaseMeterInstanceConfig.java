package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.Set;

public class BaseMeterInstanceConfig extends AbstractMeterInstanceConfig {

    public BaseMeterInstanceConfig(
        MetricName name,
        Set<? extends Measurable> measurables,
        MetricContext context) {

        super(name, measurables, context);
    }
}
