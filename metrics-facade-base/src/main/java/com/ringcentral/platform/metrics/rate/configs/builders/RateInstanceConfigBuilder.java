package com.ringcentral.platform.metrics.rate.configs.builders;

import java.util.Set;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.InstanceConfigBuilder;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateMeasurable;
import com.ringcentral.platform.metrics.rate.configs.*;

public class RateInstanceConfigBuilder extends InstanceConfigBuilder<
    RateMeasurable,
    RateInstanceConfig,
    RateInstanceConfigBuilder> {

    public static RateInstanceConfigBuilder rateInstance() {
        return new RateInstanceConfigBuilder();
    }

    public static RateInstanceConfigBuilder rateInstance(String... nameParts) {
        return new RateInstanceConfigBuilder(MetricName.of(nameParts));
    }

    public static RateInstanceConfigBuilder rateInstance(MetricName name) {
        return new RateInstanceConfigBuilder(name);
    }

    public static RateInstanceConfigBuilder rateInstanceConfigBuilder() {
        return new RateInstanceConfigBuilder();
    }

    public RateInstanceConfigBuilder() {
        this(null);
    }

    public RateInstanceConfigBuilder(MetricName name) {
        super(name, RateMeasurable.class);
    }

    @Override
    public RateInstanceConfig buildImpl(
        MetricName name,
        Set<RateMeasurable> measurables,
        MetricContext context) {

        return new DefaultRateInstanceConfig(
            name,
            measurables,
            context);
    }
}