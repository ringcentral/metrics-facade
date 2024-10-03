package com.ringcentral.platform.metrics.instrument.executors;

import com.ringcentral.platform.metrics.MetricKey;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

public class MonitoredExecutorServiceBuilder extends AbstractMonitoredExecutorServiceBuilder<ExecutorService, MonitoredExecutorServiceBuilder> {

    public static MonitoredExecutorServiceBuilder monitoredExecutorService(@Nonnull ExecutorService parent, @Nonnull MetricRegistry registry) {
        return new MonitoredExecutorServiceBuilder(parent, registry);
    }

    public MonitoredExecutorServiceBuilder(@Nonnull ExecutorService parent, @Nonnull MetricRegistry registry) {
        super(parent, registry);
    }

    @Override
    protected ExecutorService build(ExecutorService parent, MetricRegistry registry, Function<MetricName, MetricKey> metricKeyProvider, LabelValues labelValues) {
        return new MonitoredExecutorService(parent, registry, metricKeyProvider, labelValues);
    }
}
