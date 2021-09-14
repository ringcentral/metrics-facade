package com.ringcentral.platform.metrics.histogram.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.histogram.HistogramMeasurable;
import com.ringcentral.platform.metrics.histogram.configs.*;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;

public class HistogramConfigBuilder extends AbstractMeterConfigBuilder<
    HistogramMeasurable,
    HistogramInstanceConfig,
    HistogramSliceConfig,
    HistogramConfig,
    HistogramAllSliceConfigBuilder,
    HistogramSliceConfigBuilder,
    HistogramConfigBuilder> {

    public static HistogramConfigBuilder histogram() {
        return histogramConfigBuilder();
    }

    public static HistogramConfigBuilder withHistogram() {
        return histogramConfigBuilder();
    }

    public static HistogramConfigBuilder histogramConfigBuilder() {
        return new HistogramConfigBuilder();
    }

    public HistogramConfigBuilder() {
        super(HistogramMeasurable.class);
    }

    @Override
    protected HistogramAllSliceConfigBuilder makeAllSliceConfigBuilder(HistogramConfigBuilder builder, MetricName name) {
        return new HistogramAllSliceConfigBuilder(builder, name);
    }

    @Override
    protected HistogramSliceConfigBuilder makeSliceConfigBuilder(HistogramConfigBuilder builder, MetricName name) {
        return new HistogramSliceConfigBuilder(builder, name);
    }

    @Override
    protected InstanceConfigBuilder<HistogramMeasurable, HistogramInstanceConfig, ?> makeInstanceConfigBuilder() {
        return new HistogramInstanceConfigBuilder();
    }

    @Override
    protected HistogramConfig buildImpl(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricDimensionValuesPredicate exclusionPredicate,
        HistogramSliceConfig allSliceConfig,
        Set<HistogramSliceConfig> sliceConfigs,
        MetricContext context) {

        return new DefaultHistogramConfig(
            enabled,
            description,
            prefixDimensionValues,
            dimensions,
            exclusionPredicate,
            allSliceConfig,
            sliceConfigs,
            context);
    }
}
