package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.producers.SystemMetricsProducer;

@SuppressWarnings("ALL")
public class MetricsProducersSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        // MetricRegistry registry = new DropwizardMetricRegistry();
        MetricRegistry registry = new DefaultMetricRegistry();

        // Adds some system metrics
        new SystemMetricsProducer(true).produceMetrics(registry);

// Labeled metrics producers. The same as new SystemMetricsProducer(true).produceMetrics(registry):
//        new LabeledOperatingSystemMetricsProducer().produceMetrics(registry);
//        new LabeledGarbageCollectorsMetricsProducer().produceMetrics(registry);
//        new LabeledMemoryMetricsProducer().produceMetrics(registry);
//        new LabeledThreadsMetricsProducer().produceMetrics(registry);
//        new LabeledBufferPoolsMetricsProducer().produceMetrics(registry);

// Unlabeled metrics producers. The same as new SystemMetricsProducer(false).produceMetrics(registry):
//        new DefaultOperatingSystemMetricsProducer().produceMetrics(registry);
//        new DefaultGarbageCollectorsMetricsProducer().produceMetrics(registry);
//        new DefaultMemoryMetricsProducer().produceMetrics(registry);
//        new DefaultThreadsMetricsProducer().produceMetrics(registry);
//        new DefaultBufferPoolsMetricsProducer().produceMetrics(registry);

        export(registry);
        hang();
    }
}
