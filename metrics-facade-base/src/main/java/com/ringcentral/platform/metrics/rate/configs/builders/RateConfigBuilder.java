package com.ringcentral.platform.metrics.rate.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateMeasurable;
import com.ringcentral.platform.metrics.rate.configs.*;

import java.util.*;

public class RateConfigBuilder extends AbstractMeterConfigBuilder<
    RateMeasurable,
    RateInstanceConfig,
    RateSliceConfig,
    RateConfig,
    RateAllSliceConfigBuilder,
    RateSliceConfigBuilder,
    RateConfigBuilder> {

    public static RateConfigBuilder rate() {
        return rateConfigBuilder();
    }

    public static RateConfigBuilder withRate() {
        return rateConfigBuilder();
    }

    public static RateConfigBuilder rateConfigBuilder() {
        return new RateConfigBuilder();
    }

    public RateConfigBuilder() {
        super(RateMeasurable.class);
    }

    @Override
    protected RateAllSliceConfigBuilder makeAllSliceConfigBuilder(RateConfigBuilder builder, MetricName name) {
        return new RateAllSliceConfigBuilder(builder, name);
    }

    @Override
    protected RateSliceConfigBuilder makeSliceConfigBuilder(RateConfigBuilder builder, MetricName name) {
        return new RateSliceConfigBuilder(builder, name);
    }

    @Override
    protected InstanceConfigBuilder<RateMeasurable, RateInstanceConfig, ?> makeInstanceConfigBuilder() {
        return new RateInstanceConfigBuilder();
    }

    @Override
    protected RateConfig buildImpl(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricDimensionValuesPredicate exclusionPredicate,
        RateSliceConfig allSliceConfig,
        Set<RateSliceConfig> sliceConfigs,
        MetricContext context) {

        return new DefaultRateConfig(
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
