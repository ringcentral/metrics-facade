package com.ringcentral.platform.metrics.producers.dimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractMetricsProducer;
import com.ringcentral.platform.metrics.producers.JmxAttrValueSupplier;
import com.ringcentral.platform.metrics.var.Var;
import org.slf4j.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.dimensionValues;
import static com.ringcentral.platform.metrics.var.longVar.configs.builders.LongVarConfigBuilder.withLongVar;
import static java.lang.management.ManagementFactory.getPlatformMBeanServer;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

public class BufferPoolsMetricsProducer extends AbstractMetricsProducer {
    // TODO move to constants?
    private final static MetricDimension NAME_DIMENSION = new MetricDimension("name");

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("Buffers");

    private static final String[] POOLS = {"direct", "mapped"};
    private static final String[] ATTRS = {"Count", "MemoryUsed", "TotalCapacity"};
    private static final String[] ATTR_NAME_PARTS = {"count", "used", "capacity"};
    private static final String[] ATTR_DESCRIPTION = {
            "An estimate of the number of buffers in the pool",
            "An estimate of the memory that the Java virtual machine is using for this buffer pool",
            "An estimate of the total capacity of the buffers in this pool"
    };

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

        for (int i = 0; i < ATTRS.length; ++i) {
            String attr = ATTRS[i];
            String attrNamePart = ATTR_NAME_PARTS[i];
            final var description = ATTR_DESCRIPTION[i];
            final var attrLongVar = registry.longVar(
                    nameWithSuffix("pool", attrNamePart),
                    // TODO use longVarConfigBuilderSupplier
                    Var.noTotal(),
                    () -> withLongVar()
                            .description(description)
                            .dimensions(NAME_DIMENSION)
            );

            for (String pool : POOLS) {
                // TODO initialize on start
                final var nameDimensionValue = NAME_DIMENSION.value(pool);
                final var metricDimensionValues = dimensionValues(nameDimensionValue);
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
