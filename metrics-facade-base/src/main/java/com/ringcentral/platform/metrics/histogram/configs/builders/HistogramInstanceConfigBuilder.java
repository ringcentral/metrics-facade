package com.ringcentral.platform.metrics.histogram.configs.builders;

import java.util.Set;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.InstanceConfigBuilder;
import com.ringcentral.platform.metrics.histogram.HistogramMeasurable;
import com.ringcentral.platform.metrics.histogram.configs.*;
import com.ringcentral.platform.metrics.names.MetricName;

public class HistogramInstanceConfigBuilder extends InstanceConfigBuilder<
    HistogramMeasurable,
    HistogramInstanceConfig,
    HistogramInstanceConfigBuilder> {

    public static HistogramInstanceConfigBuilder histogramInstance() {
        return new HistogramInstanceConfigBuilder();
    }

    public static HistogramInstanceConfigBuilder histogramInstance(String... nameParts) {
        return new HistogramInstanceConfigBuilder(MetricName.of(nameParts));
    }

    public static HistogramInstanceConfigBuilder histogramInstance(MetricName name) {
        return new HistogramInstanceConfigBuilder(name);
    }

    public static HistogramInstanceConfigBuilder histogramInstanceConfigBuilder() {
        return new HistogramInstanceConfigBuilder();
    }

    public HistogramInstanceConfigBuilder() {
        this(null);
    }

    public HistogramInstanceConfigBuilder(MetricName name) {
        super(name, HistogramMeasurable.class);
    }

    @Override
    public HistogramInstanceConfig buildImpl(
        MetricName name,
        Set<HistogramMeasurable> measurables,
        MetricContext context) {

        return new DefaultHistogramInstanceConfig(
            name,
            measurables,
            context);
    }
}