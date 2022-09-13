package com.ringcentral.platform.metrics.producers.dimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractBufferPoolsMetricsProducer;
import com.ringcentral.platform.metrics.producers.JmxAttrValueSupplier;
import com.ringcentral.platform.metrics.var.Var;
import org.slf4j.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Produces<br>
 * <ul>
 *     <li>
 *         <i>pool.capacity</i> - an estimate of the total capacity of the buffers in this pool.<br>
 *         Dimensions:<br>
 *         name = {"direct", "mapped"}<br>
 *     </li>
 *     <li>
 *         <i>pool.used</i> - an estimate of the memory that the Java virtual machine is using for this buffer pool.<br>
 *         Dimensions:<br>
 *         name = {"direct", "mapped"}<br>
 *     </li>
 *     <li>
 *         <i>pool.count</i> - an estimate of the number of buffers in the pool.<br>
 *         Dimensions:<br>
 *         name = {"direct", "mapped"}<br>
 *     </li>
 * </ul>
 *
 * All metrics have a name prefix. By default it is 'Buffers'.<br>
 * <br>
 * Example of usage:
 * <pre>
 * MetricRegistry registry = new DefaultMetricRegistry();
 * new DimensionalBufferPoolsMetricsProducer().produceMetrics(registry);
 * PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);
 * System.out.println(exporter.exportMetrics());
 * </pre>
 * Corresponding output:
 * <pre>
 * # HELP Buffers_pool_capacity An estimate of the total capacity of the buffers in this pool
 * # TYPE Buffers_pool_capacity gauge
 * Buffers_pool_capacity{name="direct",} 0.0
 * Buffers_pool_capacity{name="mapped",} 0.0
 * # HELP Buffers_pool_used An estimate of the memory that the Java virtual machine is using for this buffer pool
 * # TYPE Buffers_pool_used gauge
 * Buffers_pool_used{name="direct",} 0.0
 * Buffers_pool_used{name="mapped",} 0.0
 * # HELP Buffers_pool_count An estimate of the number of buffers in the pool
 * # TYPE Buffers_pool_count gauge
 * Buffers_pool_count{name="direct",} 0.0
 * Buffers_pool_count{name="mapped",} 0.0
 * </pre>
 */
public class DimensionalBufferPoolsMetricsProducer extends AbstractBufferPoolsMetricsProducer {

    private static final MetricDimension NAME_DIMENSION = new MetricDimension("name");
    private static final List<MetricDimensionValues> POOL_DIMENSION_VALUES = Arrays.stream(POOLS)
            .map(NAME_DIMENSION::value)
            .map(MetricDimensionValues::dimensionValues)
            .collect(Collectors.toList());

    private static final Logger logger = getLogger(DimensionalBufferPoolsMetricsProducer.class);

    public DimensionalBufferPoolsMetricsProducer() {
        this(DEFAULT_NAME_PREFIX, null);
    }

    public DimensionalBufferPoolsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(
                namePrefix,
                metricModBuilder,
                getPlatformMBeanServer());
    }

    public DimensionalBufferPoolsMetricsProducer(
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
            final var attrLongVar = registry.longVar(
                    nameWithSuffix("pool", attrNamePart),
                    Var.noTotal(),
                    longVarConfigBuilderSupplier(description, NAME_DIMENSION));

            for (int poolIdx = 0; poolIdx < POOLS.length; poolIdx++) {
                final String pool = POOLS[poolIdx];
                final var metricDimensionValues = POOL_DIMENSION_VALUES.get(poolIdx);
                try {
                    ObjectName objectName = new ObjectName("java.nio:type=BufferPool,name=" + pool);
                    mBeanServer.getMBeanInfo(objectName);
                    JmxAttrValueSupplier jmxAttrValueSupplier = new JmxAttrValueSupplier(mBeanServer, objectName, attr);

                    attrLongVar.register(
                            () -> {
                                Object jmxAttrValue = jmxAttrValueSupplier.get();
                                return jmxAttrValue instanceof Number ? ((Number) jmxAttrValue).longValue() : -1L;
                            },
                            metricDimensionValues
                    );
                } catch (Exception e) {
                    logger.debug("Failed to get buffer pool MBeans", e);
                }
            }
        }
    }
}
