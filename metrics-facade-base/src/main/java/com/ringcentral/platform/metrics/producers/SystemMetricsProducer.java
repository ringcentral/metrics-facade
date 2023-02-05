package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.unlabeled.*;
import com.ringcentral.platform.metrics.producers.labeled.*;

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
        this(null, false);
    }

    public SystemMetricsProducer(boolean labeled) {
        this(null, labeled);
    }

    public SystemMetricsProducer(
        MetricName runtimeMetricNamePrefix,
        MetricName operatingSystemMetricNamePrefix,
        MetricName garbageCollectorsMetricNamePrefix,
        MetricName memoryMetricNamePrefix,
        MetricName threadsMetricNamePrefix,
        MetricName bufferPoolsMetricNamePrefix,
        MetricName classesMetricNamePrefix,
        boolean labeled) {

        this(
            runtimeMetricNamePrefix,
            operatingSystemMetricNamePrefix,
            garbageCollectorsMetricNamePrefix,
            memoryMetricNamePrefix,
            threadsMetricNamePrefix,
            bufferPoolsMetricNamePrefix,
            classesMetricNamePrefix,
            null,
            labeled);
    }

    public SystemMetricsProducer(MetricModBuilder metricModBuilder, boolean labeled) {
        this(
            RuntimeMetricsProducer.DEFAULT_NAME_PREFIX,
            AbstractOperatingSystemMetricsProducer.DEFAULT_NAME_PREFIX,
            AbstractGarbageCollectorsMetricsProducer.DEFAULT_NAME_PREFIX,
            AbstractMemoryMetricsProducer.DEFAULT_NAME_PREFIX,
            AbstractThreadsMetricsProducer.DEFAULT_NAME_PREFIX,
            AbstractBufferPoolsMetricsProducer.DEFAULT_NAME_PREFIX,
            ClassesMetricsProducer.DEFAULT_NAME_PREFIX,
            metricModBuilder,
            labeled);
    }

    public SystemMetricsProducer(
        MetricName runtimeMetricNamePrefix,
        MetricName operatingSystemMetricNamePrefix,
        MetricName garbageCollectorsMetricNamePrefix,
        MetricName memoryMetricNamePrefix,
        MetricName threadsMetricNamePrefix,
        MetricName bufferPoolsMetricNamePrefix,
        MetricName classesMetricNamePrefix,
        MetricModBuilder metricModBuilder,
        boolean labeled) {

        this(
            new RuntimeMetricsProducer(runtimeMetricNamePrefix, metricModBuilder),
            makeOperatingSystemMetricsProducer(operatingSystemMetricNamePrefix, metricModBuilder, labeled),
            makeGarbageCollectorsMetricsProducer(garbageCollectorsMetricNamePrefix, metricModBuilder, labeled),
            makeMemoryMetricsProducer(memoryMetricNamePrefix, metricModBuilder, labeled),
            makeThreadsMetricsProducer(threadsMetricNamePrefix, metricModBuilder, labeled),
            makeBufferPoolsMetricsProducer(bufferPoolsMetricNamePrefix, metricModBuilder, labeled),
            new ClassesMetricsProducer(classesMetricNamePrefix, metricModBuilder));
    }

    private static OperatingSystemMetricsProducer makeOperatingSystemMetricsProducer(final MetricName operatingSystemMetricNamePrefix, final MetricModBuilder metricModBuilder, final boolean labeled) {
        return
            labeled ?
            new LabeledOperatingSystemMetricsProducer(operatingSystemMetricNamePrefix, metricModBuilder) :
            new DefaultOperatingSystemMetricsProducer(operatingSystemMetricNamePrefix, metricModBuilder);
    }

    private static GarbageCollectorsMetricsProducer makeGarbageCollectorsMetricsProducer(final MetricName garbageCollectorsMetricNamePrefix, final MetricModBuilder metricModBuilder, final boolean labeled) {
        return
            labeled ?
            new LabeledGarbageCollectorsMetricsProducer(garbageCollectorsMetricNamePrefix, metricModBuilder) :
            new DefaultGarbageCollectorsMetricsProducer(garbageCollectorsMetricNamePrefix, metricModBuilder);
    }

    private static MemoryMetricsProducer makeMemoryMetricsProducer(final MetricName memoryMetricNamePrefix, final MetricModBuilder metricModBuilder, final boolean labeled) {
        return
            labeled ?
            new LabeledMemoryMetricsProducer(memoryMetricNamePrefix, metricModBuilder):
            new DefaultMemoryMetricsProducer(memoryMetricNamePrefix, metricModBuilder);
    }

    private static BufferPoolsMetricsProducer makeBufferPoolsMetricsProducer(final MetricName bufferPoolsMetricNamePrefix, final MetricModBuilder metricModBuilder, final boolean labeled) {
        return
            labeled ?
            new LabeledBufferPoolsMetricsProducer(bufferPoolsMetricNamePrefix, metricModBuilder) :
            new DefaultBufferPoolsMetricsProducer(bufferPoolsMetricNamePrefix, metricModBuilder);
    }

    private static ThreadsMetricsProducer makeThreadsMetricsProducer(final MetricName threadsMetricNamePrefix, final MetricModBuilder metricModBuilder, final boolean labeled) {
        return
            labeled ?
            new LabeledThreadsMetricsProducer(threadsMetricNamePrefix, metricModBuilder) :
            new DefaultThreadsMetricsProducer(threadsMetricNamePrefix, metricModBuilder);
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
