package com.ringcentral.platform.metrics.producers.nondimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractBufferPoolsMetricsProducer;
import com.ringcentral.platform.metrics.producers.JmxAttrValueSupplier;
import org.slf4j.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static org.slf4j.LoggerFactory.getLogger;

public class DefaultBufferPoolsMetricsProducer extends AbstractBufferPoolsMetricsProducer {

    private static final Logger logger = getLogger(DefaultBufferPoolsMetricsProducer.class);

    public DefaultBufferPoolsMetricsProducer() {
        this(DEFAULT_NAME_PREFIX, null);
    }

    public DefaultBufferPoolsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(
            namePrefix,
            metricModBuilder,
            getPlatformMBeanServer());
    }

    public DefaultBufferPoolsMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        MBeanServer mBeanServer) {
        super(namePrefix, metricModBuilder, mBeanServer);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        for (int i = 0; i < ATTRS.length; ++i) {
            String attr = ATTRS[i];
            String attrNamePart = ATTR_NAME_PARTS[i];
            final var description = ATTR_DESCRIPTION[i];
            for (String pool : POOLS) {
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
                        longVarConfigBuilderSupplier(description));
                } catch (Exception e) {
                    logger.debug("Failed to get buffer pool MBeans", e);
                }
            }
        }
    }
}
