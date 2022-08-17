package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.names.MetricName;

import javax.management.MBeanServer;

import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static java.util.Objects.requireNonNull;

public abstract class AbstractBufferPoolsMetricsProducer extends AbstractMetricsProducer implements BufferPoolsMetricsProducer {

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("Buffers");

    protected static final String[] POOLS = { "direct", "mapped" };
    protected static final String[] ATTRS = { "Count", "MemoryUsed", "TotalCapacity" };
    protected static final String[] ATTR_NAME_PARTS = { "count", "used", "capacity" };
    protected static final String[] ATTR_DESCRIPTION = {
            "An estimate of the number of buffers in the pool",
            "An estimate of the memory that the Java virtual machine is using for this buffer pool",
            "An estimate of the total capacity of the buffers in this pool"
    };

    protected final MBeanServer mBeanServer;

    public AbstractBufferPoolsMetricsProducer() {
        this(DEFAULT_NAME_PREFIX, null);
    }

    public AbstractBufferPoolsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(
            namePrefix,
            metricModBuilder,
            getPlatformMBeanServer());
    }

    public AbstractBufferPoolsMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        MBeanServer mBeanServer) {

        super(namePrefix, metricModBuilder);
        this.mBeanServer = requireNonNull(mBeanServer);
    }

}
