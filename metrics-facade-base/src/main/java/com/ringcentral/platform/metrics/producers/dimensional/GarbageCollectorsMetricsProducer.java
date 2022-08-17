package com.ringcentral.platform.metrics.producers.dimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractMetricsProducer;
import com.ringcentral.platform.metrics.var.Var;

import java.lang.management.GarbageCollectorMXBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.dimensionValues;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.var.longVar.configs.builders.LongVarConfigBuilder.withLongVar;
import static java.lang.management.ManagementFactory.getGarbageCollectorMXBeans;
import static org.apache.commons.lang3.StringUtils.split;

public class GarbageCollectorsMetricsProducer extends AbstractMetricsProducer {
    // TODO move to constants?
    private final static MetricDimension NAME_DIMENSION = new MetricDimension("name");

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("GarbageCollectors");
    private final List<GarbageCollectorMXBean> gcMxBeans;

    public GarbageCollectorsMetricsProducer() {
        this(DEFAULT_NAME_PREFIX);
    }

    public GarbageCollectorsMetricsProducer(MetricName namePrefix) {
        this(namePrefix, null);
    }

    public GarbageCollectorsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(namePrefix, metricModBuilder, getGarbageCollectorMXBeans());
    }

    public GarbageCollectorsMetricsProducer(
            MetricName namePrefix,
            MetricModBuilder metricModBuilder,
            Collection<GarbageCollectorMXBean> gcMxBeans) {

        super(namePrefix, metricModBuilder);
        this.gcMxBeans = new ArrayList<>(gcMxBeans);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        final var collectionCount = registry.longVar(
                nameWithSuffix("collection", "count", "total"),
                Var.noTotal(),
                // TODO use longVarConfigBuilderSupplier
                () -> withLongVar().dimensions(NAME_DIMENSION)
        );

        final var collectionTime = registry.longVar(
                nameWithSuffix("collection", "time", "ms"),
                Var.noTotal(),
                // TODO use longVarConfigBuilderSupplier
                () -> withLongVar().dimensions(NAME_DIMENSION)
        );

        for (GarbageCollectorMXBean gcMxBean : gcMxBeans) {
            final var name = gcMxBean.getName();
            final var nameDimensionValue = NAME_DIMENSION.value(name);
            final var dimensionValues = dimensionValues(nameDimensionValue);

            collectionCount.register(gcMxBean::getCollectionCount, dimensionValues);
            collectionTime.register(gcMxBean::getCollectionTime, dimensionValues);
        }
    }
}
