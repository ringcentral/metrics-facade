package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.names.MetricName;

import static java.util.Objects.*;

public class SystemMetricsProducer implements MetricsProducer {

    private final OperatingSystemMetricsProducer operatingSystemMetricsProducer;
    private final GarbageCollectorsMetricsProducer garbageCollectorsMetricsProducer;
    private final MemoryMetricsProducer memoryMetricsProducer;
    private final ThreadsMetricsProducer threadsMetricsProducer;
    private final BufferPoolsMetricsProducer bufferPoolsMetricsProducer;

    public SystemMetricsProducer() {
        this(null);
    }

    public SystemMetricsProducer(
        MetricName operatingSystemMetricNamePrefix,
        MetricName garbageCollectorsMetricNamePrefix,
        MetricName memoryMetricNamePrefix,
        MetricName threadsMetricNamePrefix,
        MetricName bufferPoolsMetricNamePrefix) {

        this(
            operatingSystemMetricNamePrefix,
            garbageCollectorsMetricNamePrefix,
            memoryMetricNamePrefix,
            threadsMetricNamePrefix,
            bufferPoolsMetricNamePrefix,
            null);
    }

    public SystemMetricsProducer(MetricModBuilder metricModBuilder) {
        this(
            OperatingSystemMetricsProducer.DEFAULT_NAME_PREFIX,
            GarbageCollectorsMetricsProducer.DEFAULT_NAME_PREFIX,
            MemoryMetricsProducer.DEFAULT_NAME_PREFIX,
            ThreadsMetricsProducer.DEFAULT_NAME_PREFIX,
            BufferPoolsMetricsProducer.DEFAULT_NAME_PREFIX,
            metricModBuilder);
    }

    public SystemMetricsProducer(
        MetricName operatingSystemMetricNamePrefix,
        MetricName garbageCollectorsMetricNamePrefix,
        MetricName memoryMetricNamePrefix,
        MetricName threadsMetricNamePrefix,
        MetricName bufferPoolsMetricNamePrefix,
        MetricModBuilder metricModBuilder) {

        this(
            new OperatingSystemMetricsProducer(operatingSystemMetricNamePrefix, metricModBuilder),
            new GarbageCollectorsMetricsProducer(garbageCollectorsMetricNamePrefix, metricModBuilder),
            new MemoryMetricsProducer(memoryMetricNamePrefix, metricModBuilder),
            new ThreadsMetricsProducer(threadsMetricNamePrefix, metricModBuilder),
            new BufferPoolsMetricsProducer(bufferPoolsMetricNamePrefix, metricModBuilder));
    }

    public SystemMetricsProducer(
        OperatingSystemMetricsProducer operatingSystemMetricsProducer,
        GarbageCollectorsMetricsProducer garbageCollectorsMetricsProducer,
        MemoryMetricsProducer memoryMetricsProducer,
        ThreadsMetricsProducer threadsMetricsProducer,
        BufferPoolsMetricsProducer bufferPoolsMetricsProducer) {

        this.operatingSystemMetricsProducer = requireNonNull(operatingSystemMetricsProducer);
        this.garbageCollectorsMetricsProducer = requireNonNull(garbageCollectorsMetricsProducer);
        this.memoryMetricsProducer = requireNonNull(memoryMetricsProducer);
        this.threadsMetricsProducer = requireNonNull(threadsMetricsProducer);
        this.bufferPoolsMetricsProducer = requireNonNull(bufferPoolsMetricsProducer);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        operatingSystemMetricsProducer.produceMetrics(registry);
        garbageCollectorsMetricsProducer.produceMetrics(registry);
        memoryMetricsProducer.produceMetrics(registry);
        threadsMetricsProducer.produceMetrics(registry);
        bufferPoolsMetricsProducer.produceMetrics(registry);
    }
}
