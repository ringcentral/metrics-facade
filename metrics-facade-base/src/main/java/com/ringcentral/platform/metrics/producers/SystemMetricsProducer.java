package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.names.MetricName;

import static java.util.Objects.requireNonNull;

public class SystemMetricsProducer implements MetricsProducer {

    private final RuntimeMetricsProducer runtimeMetricsProducer;
    private final OperatingSystemMetricsProducer operatingSystemMetricsProducer;
    private final GarbageCollectorsMetricsProducer garbageCollectorsMetricsProducer;
    private final MemoryMetricsProducer memoryMetricsProducer;
    private final ThreadsMetricsProducer threadsMetricsProducer;
    private final BufferPoolsMetricsProducer bufferPoolsMetricsProducer;
    private final ClassesMetricsProducer classesMetricsProducer;

    public SystemMetricsProducer() {
        this(null);
    }

    public SystemMetricsProducer(
        MetricName runtimeMetricNamePrefix,
        MetricName operatingSystemMetricNamePrefix,
        MetricName garbageCollectorsMetricNamePrefix,
        MetricName memoryMetricNamePrefix,
        MetricName threadsMetricNamePrefix,
        MetricName bufferPoolsMetricNamePrefix,
        MetricName classesMetricNamePrefix) {

        this(
            runtimeMetricNamePrefix,
            operatingSystemMetricNamePrefix,
            garbageCollectorsMetricNamePrefix,
            memoryMetricNamePrefix,
            threadsMetricNamePrefix,
            bufferPoolsMetricNamePrefix,
            classesMetricNamePrefix,
            null);
    }

    public SystemMetricsProducer(MetricModBuilder metricModBuilder) {
        this(
            RuntimeMetricsProducer.DEFAULT_NAME_PREFIX,
            OperatingSystemMetricsProducer.DEFAULT_NAME_PREFIX,
            GarbageCollectorsMetricsProducer.DEFAULT_NAME_PREFIX,
            MemoryMetricsProducer.DEFAULT_NAME_PREFIX,
            ThreadsMetricsProducer.DEFAULT_NAME_PREFIX,
            BufferPoolsMetricsProducer.DEFAULT_NAME_PREFIX,
            ClassesMetricsProducer.DEFAULT_NAME_PREFIX,
            metricModBuilder);
    }

    public SystemMetricsProducer(
        MetricName runtimeMetricNamePrefix,
        MetricName operatingSystemMetricNamePrefix,
        MetricName garbageCollectorsMetricNamePrefix,
        MetricName memoryMetricNamePrefix,
        MetricName threadsMetricNamePrefix,
        MetricName bufferPoolsMetricNamePrefix,
        MetricName classesMetricNamePrefix,
        MetricModBuilder metricModBuilder) {

        this(
            new RuntimeMetricsProducer(runtimeMetricNamePrefix, metricModBuilder),
            new OperatingSystemMetricsProducer(operatingSystemMetricNamePrefix, metricModBuilder),
            new GarbageCollectorsMetricsProducer(garbageCollectorsMetricNamePrefix, metricModBuilder),
            new MemoryMetricsProducer(memoryMetricNamePrefix, metricModBuilder),
            new ThreadsMetricsProducer(threadsMetricNamePrefix, metricModBuilder),
            new BufferPoolsMetricsProducer(bufferPoolsMetricNamePrefix, metricModBuilder),
            new ClassesMetricsProducer(classesMetricNamePrefix, metricModBuilder));
    }

    public SystemMetricsProducer(
        RuntimeMetricsProducer runtimeMetricsProducer,
        OperatingSystemMetricsProducer operatingSystemMetricsProducer,
        GarbageCollectorsMetricsProducer garbageCollectorsMetricsProducer,
        MemoryMetricsProducer memoryMetricsProducer,
        ThreadsMetricsProducer threadsMetricsProducer,
        BufferPoolsMetricsProducer bufferPoolsMetricsProducer,
        ClassesMetricsProducer classesMetricsProducer) {

        this.runtimeMetricsProducer = requireNonNull(runtimeMetricsProducer);
        this.operatingSystemMetricsProducer = requireNonNull(operatingSystemMetricsProducer);
        this.garbageCollectorsMetricsProducer = requireNonNull(garbageCollectorsMetricsProducer);
        this.memoryMetricsProducer = requireNonNull(memoryMetricsProducer);
        this.threadsMetricsProducer = requireNonNull(threadsMetricsProducer);
        this.bufferPoolsMetricsProducer = requireNonNull(bufferPoolsMetricsProducer);
        this.classesMetricsProducer = requireNonNull(classesMetricsProducer);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        runtimeMetricsProducer.produceMetrics(registry);
        operatingSystemMetricsProducer.produceMetrics(registry);
        garbageCollectorsMetricsProducer.produceMetrics(registry);
        memoryMetricsProducer.produceMetrics(registry);
        threadsMetricsProducer.produceMetrics(registry);
        bufferPoolsMetricsProducer.produceMetrics(registry);
        classesMetricsProducer.produceMetrics(registry);
    }
}
