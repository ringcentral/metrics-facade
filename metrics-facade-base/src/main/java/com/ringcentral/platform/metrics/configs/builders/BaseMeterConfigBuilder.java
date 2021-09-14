package com.ringcentral.platform.metrics.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.*;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.measurables.NothingMeasurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;

public class BaseMeterConfigBuilder extends AbstractMeterConfigBuilder<
    NothingMeasurable,
    BaseMeterInstanceConfig,
    BaseMeterSliceConfig,
    BaseMeterConfig,
    BaseMeterAllSliceConfigBuilder,
    BaseMeterSliceConfigBuilder,
    BaseMeterConfigBuilder> {

    public BaseMeterConfigBuilder() {
        super(NothingMeasurable.class);
    }

    public static BaseMeterConfigBuilder meter() {
        return meterConfigBuilder();
    }

    public static BaseMeterConfigBuilder withMeter() {
        return meterConfigBuilder();
    }

    public static BaseMeterConfigBuilder meterConfigBuilder() {
        return new BaseMeterConfigBuilder();
    }

    @Override
    protected BaseMeterAllSliceConfigBuilder makeAllSliceConfigBuilder(BaseMeterConfigBuilder builder, MetricName name) {
        return new BaseMeterAllSliceConfigBuilder(builder, name);
    }

    @Override
    protected BaseMeterSliceConfigBuilder makeSliceConfigBuilder(BaseMeterConfigBuilder builder, MetricName name) {
        return new BaseMeterSliceConfigBuilder(builder, name);
    }

    @Override
    protected InstanceConfigBuilder<NothingMeasurable, BaseMeterInstanceConfig, ?> makeInstanceConfigBuilder() {
        return new BaseMeterInstanceConfigBuilder();
    }

    @Override
    protected BaseMeterConfig buildImpl(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricDimensionValuesPredicate exclusionPredicate,
        BaseMeterSliceConfig allSliceConfig,
        Set<BaseMeterSliceConfig> sliceConfigs,
        MetricContext context) {

        return new BaseMeterConfig(
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
