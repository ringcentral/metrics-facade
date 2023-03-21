package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.names.MetricName;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

import static java.util.Objects.requireNonNull;

public abstract class AbstractMemoryMetricsProducer extends AbstractMetricsProducer implements MemoryMetricsProducer {

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("Memory");

    protected static final String INIT_DESCRIPTION = "The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management";
    protected static final String USED_DESCRIPTION = "The amount of used memory in bytes";
    protected static final String MAX_DESCRIPTION = "The maximum amount of memory in bytes that can be used for memory management";
    protected static final String COMMITTED_DESCRIPTION = "The amount of memory in bytes that is committed for the Java virtual machine to use";
    protected static final String USAGE_DESCRIPTION = "Used divided by max";
    protected static final String USED_AFTER_GC_DESCRIPTION = "The amount of used memory in bytes after the Java virtual machine most recently expended effort in recycling unused objects in this memory pool";

    protected final MemoryMXBean memoryMxBean;
    protected final List<MemoryPoolMXBean> memoryPoolMxBeans;

    public AbstractMemoryMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        MemoryMXBean memoryMxBean,
        List<MemoryPoolMXBean> memoryPoolMxBeans) {

        super(namePrefix, metricModBuilder);

        this.memoryMxBean = requireNonNull(memoryMxBean);
        this.memoryPoolMxBeans = requireNonNull(memoryPoolMxBeans);
    }
}
