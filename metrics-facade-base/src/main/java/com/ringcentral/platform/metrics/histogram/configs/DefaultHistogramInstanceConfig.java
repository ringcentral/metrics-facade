package com.ringcentral.platform.metrics.histogram.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMeterInstanceConfig;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.Set;

public class DefaultHistogramInstanceConfig extends AbstractMeterInstanceConfig implements HistogramInstanceConfig {

    public DefaultHistogramInstanceConfig(
        MetricName name,
        Set<? extends Measurable> measurables,
        MetricContext context) {

        super(name, measurables, context);
    }
}
