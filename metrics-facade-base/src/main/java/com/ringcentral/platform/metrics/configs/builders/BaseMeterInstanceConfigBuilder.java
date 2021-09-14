package com.ringcentral.platform.metrics.configs.builders;

import java.util.Set;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.BaseMeterInstanceConfig;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.InstanceConfigBuilder;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;

public class BaseMeterInstanceConfigBuilder extends InstanceConfigBuilder<
    NothingMeasurable,
    BaseMeterInstanceConfig,
    BaseMeterInstanceConfigBuilder> {

    public static BaseMeterInstanceConfigBuilder meterInstance() {
        return meterInstanceConfigBuilder();
    }

    public static BaseMeterInstanceConfigBuilder meterInstance(String... nameParts) {
        return meterInstanceConfigBuilder(nameParts);
    }

    public static BaseMeterInstanceConfigBuilder meterInstance(MetricName name) {
        return meterInstanceConfigBuilder(name);
    }

    public static BaseMeterInstanceConfigBuilder meterInstanceConfigBuilder() {
        return new BaseMeterInstanceConfigBuilder();
    }

    public static BaseMeterInstanceConfigBuilder meterInstanceConfigBuilder(String... nameParts) {
        return meterInstanceConfigBuilder(MetricName.of(nameParts));
    }

    public static BaseMeterInstanceConfigBuilder meterInstanceConfigBuilder(MetricName name) {
        return new BaseMeterInstanceConfigBuilder(name);
    }

    public BaseMeterInstanceConfigBuilder() {
        this(null);
    }

    public BaseMeterInstanceConfigBuilder(MetricName name) {
        super(name, NothingMeasurable.class);
    }

    @Override
    public BaseMeterInstanceConfig buildImpl(
        MetricName name,
        Set<NothingMeasurable> measurables,
        MetricContext context) {

        return new BaseMeterInstanceConfig(
            name,
            measurables,
            context);
    }
}
