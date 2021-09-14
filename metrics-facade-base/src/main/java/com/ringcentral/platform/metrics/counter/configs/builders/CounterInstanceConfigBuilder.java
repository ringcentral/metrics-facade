package com.ringcentral.platform.metrics.counter.configs.builders;

import java.util.Set;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.builders.AbstractMeterConfigBuilder.InstanceConfigBuilder;
import com.ringcentral.platform.metrics.counter.CounterMeasurable;
import com.ringcentral.platform.metrics.counter.configs.*;
import com.ringcentral.platform.metrics.names.MetricName;

public class CounterInstanceConfigBuilder extends InstanceConfigBuilder<
    CounterMeasurable,
    CounterInstanceConfig,
    CounterInstanceConfigBuilder> {

    public static CounterInstanceConfigBuilder counterInstance() {
        return new CounterInstanceConfigBuilder();
    }

    public static CounterInstanceConfigBuilder counterInstance(String... nameParts) {
        return new CounterInstanceConfigBuilder(MetricName.of(nameParts));
    }

    public static CounterInstanceConfigBuilder counterInstance(MetricName name) {
        return new CounterInstanceConfigBuilder(name);
    }

    public static CounterInstanceConfigBuilder counterInstanceConfigBuilder() {
        return new CounterInstanceConfigBuilder();
    }

    public CounterInstanceConfigBuilder() {
        this(null);
    }

    public CounterInstanceConfigBuilder(MetricName name) {
        super(name, CounterMeasurable.class);
    }

    @Override
    public CounterInstanceConfig buildImpl(
        MetricName name,
        Set<CounterMeasurable> measurables,
        MetricContext context) {

        return new DefaultCounterInstanceConfig(
            name,
            measurables,
            context);
    }
}