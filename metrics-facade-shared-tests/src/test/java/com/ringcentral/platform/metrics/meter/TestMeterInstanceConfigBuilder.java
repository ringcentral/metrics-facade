package com.ringcentral.platform.metrics.meter;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.BaseMeterInstanceConfig;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.InstanceConfigBuilder;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.Set;

public class TestMeterInstanceConfigBuilder extends InstanceConfigBuilder<
    Measurable,
    BaseMeterInstanceConfig,
    TestMeterInstanceConfigBuilder> {

    public static TestMeterInstanceConfigBuilder meterInstance() {
        return meterInstanceConfigBuilder();
    }

    public static TestMeterInstanceConfigBuilder meterInstance(String... nameParts) {
        return meterInstanceConfigBuilder(nameParts);
    }

    public static TestMeterInstanceConfigBuilder meterInstance(MetricName name) {
        return meterInstanceConfigBuilder(name);
    }

    public static TestMeterInstanceConfigBuilder meterInstanceConfigBuilder() {
        return new TestMeterInstanceConfigBuilder();
    }

    public static TestMeterInstanceConfigBuilder meterInstanceConfigBuilder(String... nameParts) {
        return meterInstanceConfigBuilder(MetricName.of(nameParts));
    }

    public static TestMeterInstanceConfigBuilder meterInstanceConfigBuilder(MetricName name) {
        return new TestMeterInstanceConfigBuilder(name);
    }

    public TestMeterInstanceConfigBuilder() {
        this(null);
    }

    public TestMeterInstanceConfigBuilder(MetricName name) {
        super(name, Measurable.class);
    }

    @Override
    public BaseMeterInstanceConfig buildImpl(
        MetricName name,
        Set<Measurable> measurables,
        MetricContext context) {

        return new BaseMeterInstanceConfig(
            name,
            measurables,
            context);
    }
}
