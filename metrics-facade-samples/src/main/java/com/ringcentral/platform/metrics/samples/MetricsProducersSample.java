package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.producers.*;
import com.ringcentral.platform.metrics.producers.nondimensional.*;

@SuppressWarnings("ALL")
public class MetricsProducersSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();

        // Adds some system metrics
        new SystemMetricsProducer().produceMetrics(registry);

        new OperatingSystemMetricsProducer().produceMetrics(registry);
        new DefaultGarbageCollectorsMetricsProducer().produceMetrics(registry);
        new DefaultMemoryMetricsProducer().produceMetrics(registry);
        new DefaultThreadsMetricsProducer().produceMetrics(registry);
        new DefaultBufferPoolsMetricsProducer().produceMetrics(registry);

        export(registry);
        hang();
    }
}
