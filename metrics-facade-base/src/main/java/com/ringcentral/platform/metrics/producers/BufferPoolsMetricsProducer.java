package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.names.MetricName;
import org.slf4j.Logger;

import javax.management.*;

import static java.lang.management.ManagementFactory.*;
import static java.util.Objects.*;
import static org.slf4j.LoggerFactory.*;

public class BufferPoolsMetricsProducer extends AbstractMetricsProducer {

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("Buffers");

    private static final String[] POOLS = { "direct", "mapped" };
    private static final String[] ATTRS = { "Count", "MemoryUsed", "TotalCapacity" };
    private static final String[] ATTR_NAME_PARTS = { "count", "used", "capacity" };

    private final MBeanServer mBeanServer;
    private static final Logger logger = getLogger(BufferPoolsMetricsProducer.class);

    public BufferPoolsMetricsProducer() {
        this(DEFAULT_NAME_PREFIX, null);
    }

    public BufferPoolsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(
            namePrefix,
            metricModBuilder,
            getPlatformMBeanServer());
    }

    public BufferPoolsMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        MBeanServer mBeanServer) {

        super(namePrefix, metricModBuilder);
        this.mBeanServer = requireNonNull(mBeanServer);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        for (String pool : POOLS) {
            for (int i = 0; i < ATTRS.length; ++i) {
                String attr = ATTRS[i];
                String attrNamePart = ATTR_NAME_PARTS[i];

                try {
                    ObjectName objectName = new ObjectName("java.nio:type=BufferPool,name=" + pool);
                    mBeanServer.getMBeanInfo(objectName);
                    JmxAttrValueSupplier jmxAttrValueSupplier = new JmxAttrValueSupplier(mBeanServer, objectName, attr);

                    registry.longVar(
                        nameWithSuffix(pool, attrNamePart),
                        () -> {
                            Object jmxAttrValue = jmxAttrValueSupplier.get();
                            return jmxAttrValue instanceof Number ? ((Number)jmxAttrValue).longValue() : -1L;
                        },
                        longVarConfigBuilderSupplier());
                } catch (Exception e) {
                    logger.debug("Failed to get buffer pool MBeans", e);
                }
            }
        }
    }
}
