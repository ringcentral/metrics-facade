package com.ringcentral.platform.metrics.counter.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder;
import com.ringcentral.platform.metrics.counter.CounterMeasurable;
import com.ringcentral.platform.metrics.counter.configs.*;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;

public class CounterConfigBuilder extends AbstractMeterConfigBuilder<
    CounterMeasurable,
    CounterInstanceConfig,
    CounterSliceConfig,
    CounterConfig,
    CounterAllSliceConfigBuilder,
    CounterSliceConfigBuilder,
    CounterConfigBuilder> {

    public static CounterConfigBuilder counter() {
        return counterConfigBuilder();
    }

    public static CounterConfigBuilder withCounter() {
        return counterConfigBuilder();
    }

    public static CounterConfigBuilder counterConfigBuilder() {
        return new CounterConfigBuilder();
    }

    public CounterConfigBuilder() {
        super(CounterMeasurable.class);
    }

    @Override
    protected CounterAllSliceConfigBuilder makeAllSliceConfigBuilder(CounterConfigBuilder builder, MetricName name) {
        return new CounterAllSliceConfigBuilder(builder, name);
    }

    @Override
    protected CounterSliceConfigBuilder makeSliceConfigBuilder(CounterConfigBuilder builder, MetricName name) {
        return new CounterSliceConfigBuilder(builder, name);
    }

    @Override
    protected InstanceConfigBuilder<CounterMeasurable, CounterInstanceConfig, ?> makeInstanceConfigBuilder() {
        return new CounterInstanceConfigBuilder();
    }

    @Override
    protected CounterConfig buildImpl(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricDimensionValuesPredicate exclusionPredicate,
        CounterSliceConfig allSliceConfig,
        Set<CounterSliceConfig> sliceConfigs,
        MetricContext context) {

        return new DefaultCounterConfig(
            enabled,
            prefixDimensionValues,
            dimensions,
            exclusionPredicate,
            allSliceConfig,
            sliceConfigs,
            context);
    }
}
