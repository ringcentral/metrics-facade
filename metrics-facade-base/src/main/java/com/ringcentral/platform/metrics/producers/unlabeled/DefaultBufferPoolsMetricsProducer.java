package com.ringcentral.platform.metrics.producers.unlabeled;

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

/**
 * Produces<br>
 *
 * <ul>
 *     <li>direct.used - an estimate of the memory that the Java virtual machine is using for the direct buffer pool.</li>
 *     <li>mapped.used - an estimate of the memory that the Java virtual machine is using for the mapped buffer pool.</li>
 *     <li>direct.capacity - an estimate of the total capacity of the buffers in the direct pool.</li>
 *     <li>mapped.capacity - an estimate of the total capacity of the buffers in the mapped pool.</li>
 *     <li>direct.count - an estimate of the number of buffers in the direct pool.</li>
 *     <li>mapped.count - an estimate of the number of buffers in the mapped pool.</li>
 * </ul>
 *
 * All metrics have a name prefix. By default it is 'Buffers'.<br>
 * <br>
 * Example of usage:
 * <pre>
 * MetricRegistry registry = new DefaultMetricRegistry();
 * new DefaultBufferPoolsMetricsProducer().produceMetrics(registry);
 * PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);
 * System.out.println(exporter.exportMetrics());
 * </pre>
 * Corresponding output:
 * <pre>
 * # HELP Buffers_direct_used An estimate of the memory that the Java virtual machine is using for this buffer pool
 * # TYPE Buffers_direct_used gauge
 * Buffers_direct_used 0.0
 * # HELP Buffers_direct_capacity An estimate of the total capacity of the buffers in this pool
 * # TYPE Buffers_direct_capacity gauge
 * Buffers_direct_capacity 0.0
 * # HELP Buffers_mapped_capacity An estimate of the total capacity of the buffers in this pool
 * # TYPE Buffers_mapped_capacity gauge
 * Buffers_mapped_capacity 0.0
 * # HELP Buffers_mapped_used An estimate of the memory that the Java virtual machine is using for this buffer pool
 * # TYPE Buffers_mapped_used gauge
 * Buffers_mapped_used 0.0
 * # HELP Buffers_direct_count An estimate of the number of buffers in the pool
 * # TYPE Buffers_direct_count gauge
 * Buffers_direct_count 0.0
 * # HELP Buffers_mapped_count An estimate of the number of buffers in the pool
 * # TYPE Buffers_mapped_count gauge
 * Buffers_mapped_count 0.0
 * </pre>
 */
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
