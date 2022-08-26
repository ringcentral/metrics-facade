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
