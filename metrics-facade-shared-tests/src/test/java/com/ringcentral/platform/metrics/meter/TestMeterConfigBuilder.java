package com.ringcentral.platform.metrics.meter;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.*;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;

public class TestMeterConfigBuilder extends AbstractMeterConfigBuilder<
    Measurable,
    BaseMeterInstanceConfig,
    BaseMeterSliceConfig,
    BaseMeterConfig,
    TestMeterAllSliceConfigBuilder,
    TestMeterSliceConfigBuilder,
    TestMeterConfigBuilder> {

    public TestMeterConfigBuilder() {
        super(Measurable.class);
    }

    public static TestMeterConfigBuilder meter() {
        return meterConfigBuilder();
    }

    public static TestMeterConfigBuilder withMeter() {
        return meterConfigBuilder();
    }

    public static TestMeterConfigBuilder meterConfigBuilder() {
        return new TestMeterConfigBuilder();
    }

    @Override
    protected TestMeterAllSliceConfigBuilder makeAllSliceConfigBuilder(TestMeterConfigBuilder builder, MetricName name) {
        return new TestMeterAllSliceConfigBuilder(builder, name);
    }

    @Override
    protected TestMeterSliceConfigBuilder makeSliceConfigBuilder(TestMeterConfigBuilder builder, MetricName name) {
        return new TestMeterSliceConfigBuilder(builder, name);
    }

    @Override
    protected InstanceConfigBuilder<Measurable, BaseMeterInstanceConfig, ?> makeInstanceConfigBuilder() {
        return new TestMeterInstanceConfigBuilder();
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
