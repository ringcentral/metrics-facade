package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.dimensional.DimensionalBufferPoolsMetricsProducer;
import com.ringcentral.platform.metrics.producers.dimensional.DimensionalGarbageCollectorsMetricsProducer;
import com.ringcentral.platform.metrics.producers.dimensional.DimensionalThreadsMetricsProducer;
import com.ringcentral.platform.metrics.producers.nondimensional.*;

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

    public SystemMetricsProducer(boolean isDimensional) {
        this(null, isDimensional);
    }

    public SystemMetricsProducer(
        MetricName runtimeMetricNamePrefix,
        MetricName operatingSystemMetricNamePrefix,
        MetricName garbageCollectorsMetricNamePrefix,
        MetricName memoryMetricNamePrefix,
        MetricName threadsMetricNamePrefix,
        MetricName bufferPoolsMetricNamePrefix,
        MetricName classesMetricNamePrefix,
        boolean isDimensional) {

        this(
            runtimeMetricNamePrefix,
            operatingSystemMetricNamePrefix,
            garbageCollectorsMetricNamePrefix,
            memoryMetricNamePrefix,
            threadsMetricNamePrefix,
            bufferPoolsMetricNamePrefix,
            classesMetricNamePrefix,
            null,
                isDimensional);
    }

    public SystemMetricsProducer(
            MetricModBuilder metricModBuilder,
            boolean isDimensional
    ) {
        this(
                RuntimeMetricsProducer.DEFAULT_NAME_PREFIX,
                OperatingSystemMetricsProducer.DEFAULT_NAME_PREFIX,
                AbstractGarbageCollectorsMetricsProducer.DEFAULT_NAME_PREFIX,
                MemoryMetricsProducer.DEFAULT_NAME_PREFIX,
                AbstractThreadsMetricsProducer.DEFAULT_NAME_PREFIX,
                AbstractBufferPoolsMetricsProducer.DEFAULT_NAME_PREFIX,
                ClassesMetricsProducer.DEFAULT_NAME_PREFIX,
                metricModBuilder,
                isDimensional);
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
        boolean isDimensional) {

        this(
            new RuntimeMetricsProducer(runtimeMetricNamePrefix, metricModBuilder),
            new OperatingSystemMetricsProducer(operatingSystemMetricNamePrefix, metricModBuilder),
                getGarbageCollectorsMetricsProducer(garbageCollectorsMetricNamePrefix, metricModBuilder, isDimensional),
            new MemoryMetricsProducer(memoryMetricNamePrefix, metricModBuilder),
                getThreadsMetricsProducer(threadsMetricNamePrefix, metricModBuilder, isDimensional),
                getBufferPoolsMetricsProducer(bufferPoolsMetricNamePrefix, metricModBuilder, isDimensional),
            new ClassesMetricsProducer(classesMetricNamePrefix, metricModBuilder));
    }

    private static GarbageCollectorsMetricsProducer getGarbageCollectorsMetricsProducer(final MetricName garbageCollectorsMetricNamePrefix, final MetricModBuilder metricModBuilder, final boolean isDimensional) {
        return isDimensional ?
                new DimensionalGarbageCollectorsMetricsProducer(garbageCollectorsMetricNamePrefix, metricModBuilder) :
                new DefaultGarbageCollectorsMetricsProducer(garbageCollectorsMetricNamePrefix, metricModBuilder);
    }

    private static BufferPoolsMetricsProducer getBufferPoolsMetricsProducer(final MetricName bufferPoolsMetricNamePrefix, final MetricModBuilder metricModBuilder, final boolean isDimensional) {
        return isDimensional ?
                new DimensionalBufferPoolsMetricsProducer(bufferPoolsMetricNamePrefix, metricModBuilder) :
                new DefaultBufferPoolsMetricsProducer(bufferPoolsMetricNamePrefix, metricModBuilder);
    }

    private static ThreadsMetricsProducer getThreadsMetricsProducer(final MetricName threadsMetricNamePrefix, final MetricModBuilder metricModBuilder, final boolean isDimensional) {
        return isDimensional ?
                new DimensionalThreadsMetricsProducer(threadsMetricNamePrefix, metricModBuilder) :
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
