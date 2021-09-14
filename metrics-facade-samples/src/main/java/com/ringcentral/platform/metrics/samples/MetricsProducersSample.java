package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dropwizard.DropwizardMetricRegistry;
import com.ringcentral.platform.metrics.producers.*;

@SuppressWarnings("ALL")
public class MetricsProducersSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        MetricRegistry registry = new DropwizardMetricRegistry();

        // Adds some system metrics
        new SystemMetricsProducer().produceMetrics(registry);

        new OperatingSystemMetricsProducer().produceMetrics(registry);
        new GarbageCollectorsMetricsProducer().produceMetrics(registry);
        new MemoryMetricsProducer().produceMetrics(registry);
        new ThreadsMetricsProducer().produceMetrics(registry);
        new BufferPoolsMetricsProducer().produceMetrics(registry);

        export(registry);
        hang();
    }
}
