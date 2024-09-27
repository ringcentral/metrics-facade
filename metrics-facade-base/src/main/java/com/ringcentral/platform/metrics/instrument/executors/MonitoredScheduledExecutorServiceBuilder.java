package com.ringcentral.platform.metrics.instrument.executors;

import com.ringcentral.platform.metrics.MetricKey;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;

import javax.annotation.Nonnull;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public class MonitoredScheduledExecutorServiceBuilder extends AbstractMonitoredExecutorServiceBuilder<ScheduledExecutorService, MonitoredScheduledExecutorServiceBuilder> {

    public static MonitoredScheduledExecutorServiceBuilder monitoredScheduledExecutorService(@Nonnull ScheduledExecutorService parent, @Nonnull MetricRegistry registry) {
        return new MonitoredScheduledExecutorServiceBuilder(parent, registry);
    }

    public MonitoredScheduledExecutorServiceBuilder(@Nonnull ScheduledExecutorService parent, @Nonnull MetricRegistry registry) {
        super(parent, registry);
    }

    @Override
    protected ScheduledExecutorService build(ScheduledExecutorService parent, MetricRegistry registry, Function<MetricName, MetricKey> metricKeyProvider, LabelValues labelValues) {
        return new MonitoredScheduledExecutorService(parent, registry, metricKeyProvider, labelValues);
    }
}
