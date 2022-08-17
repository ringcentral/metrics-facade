package com.ringcentral.platform.metrics.producers.dimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractMemoryMetricsProducer;
import com.ringcentral.platform.metrics.producers.Ratio;
import com.ringcentral.platform.metrics.var.Var;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionUtils.NAME_DIMENSION;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionUtils.TYPE_DIMENSION;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.dimensionValues;
import static java.lang.management.ManagementFactory.getMemoryMXBean;
import static java.lang.management.ManagementFactory.getMemoryPoolMXBeans;

public class DimensionalMemoryMetricsProducer extends AbstractMemoryMetricsProducer {
    private static final MetricDimensionValues HEAP_TYPE_DIMENSION_VALUES = dimensionValues(TYPE_DIMENSION.value("heap"));
    private static final MetricDimensionValues NON_HEAP_TYPE_DIMENSION_VALUES = dimensionValues(TYPE_DIMENSION.value("non-heap"));
    private static final MetricDimensionValues TOTAL_TYPE_DIMENSION_VALUES = dimensionValues(TYPE_DIMENSION.value("total"));

    public DimensionalMemoryMetricsProducer() {
        this(DEFAULT_NAME_PREFIX, null);
    }

    public DimensionalMemoryMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(
                namePrefix,
                metricModBuilder,
                getMemoryMXBean(),
                getMemoryPoolMXBeans());
    }

    public DimensionalMemoryMetricsProducer(
            MetricName namePrefix,
            MetricModBuilder metricModBuilder,
            MemoryMXBean memoryMxBean,
            List<MemoryPoolMXBean> memoryPoolMxBeans) {

        super(namePrefix, metricModBuilder, memoryMxBean, memoryPoolMxBeans);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        final var initialSize = registry.longVar(
                nameWithSuffix("init", "bytes"),
                Var.noTotal(),
                longVarConfigBuilderSupplier(INIT_MESSAGE, TYPE_DIMENSION)
        );
        initialSize.register(
                () -> memoryMxBean.getHeapMemoryUsage().getInit() + memoryMxBean.getNonHeapMemoryUsage().getInit(),
                TOTAL_TYPE_DIMENSION_VALUES
        );
        initialSize.register(memoryMxBean.getHeapMemoryUsage()::getInit, HEAP_TYPE_DIMENSION_VALUES);
        initialSize.register(memoryMxBean.getNonHeapMemoryUsage()::getInit, NON_HEAP_TYPE_DIMENSION_VALUES);

        final var usedSize = registry.longVar(
                nameWithSuffix("used", "bytes"),
                Var.noTotal(),
                longVarConfigBuilderSupplier(USED_MESSAGE, TYPE_DIMENSION)
        );
        usedSize.register(
                () -> memoryMxBean.getHeapMemoryUsage().getUsed() + memoryMxBean.getNonHeapMemoryUsage().getUsed(),
                TOTAL_TYPE_DIMENSION_VALUES
        );
        usedSize.register(memoryMxBean.getHeapMemoryUsage()::getUsed, HEAP_TYPE_DIMENSION_VALUES);
        usedSize.register(memoryMxBean.getNonHeapMemoryUsage()::getUsed, NON_HEAP_TYPE_DIMENSION_VALUES);

        final var maxSize = registry.longVar(
                nameWithSuffix("max", "bytes"),
                Var.noTotal(),
                longVarConfigBuilderSupplier(MAX_MESSAGE, TYPE_DIMENSION)
        );
        maxSize.register(() -> memoryMxBean.getHeapMemoryUsage().getMax() + memoryMxBean.getNonHeapMemoryUsage().getMax(), TOTAL_TYPE_DIMENSION_VALUES);

        maxSize.register(memoryMxBean.getHeapMemoryUsage()::getMax, HEAP_TYPE_DIMENSION_VALUES);
        maxSize.register(memoryMxBean.getNonHeapMemoryUsage()::getMax, NON_HEAP_TYPE_DIMENSION_VALUES);

        final var committedSize = registry.longVar(
                nameWithSuffix("committed", "bytes"),
                Var.noTotal(),
                longVarConfigBuilderSupplier(COMMITTED_MESSAGE, TYPE_DIMENSION)
        );
        committedSize.register(
                () -> memoryMxBean.getHeapMemoryUsage().getCommitted() + memoryMxBean.getNonHeapMemoryUsage().getCommitted(),
                TOTAL_TYPE_DIMENSION_VALUES
        );
        committedSize.register(memoryMxBean.getHeapMemoryUsage()::getCommitted, HEAP_TYPE_DIMENSION_VALUES);
        committedSize.register(memoryMxBean.getNonHeapMemoryUsage()::getCommitted, NON_HEAP_TYPE_DIMENSION_VALUES);


        final var usageRatio = registry.doubleVar(
                nameWithSuffix("usage", "ratio"),
                Var.noTotal(),
                doubleVarConfigBuilderSupplier(USAGE_MESSAGE, TYPE_DIMENSION)
        );
        usageRatio.register(
                () -> {
                    MemoryUsage usage = memoryMxBean.getHeapMemoryUsage();
                    return Ratio.of(usage.getUsed(), usage.getMax()).value();
                },
                HEAP_TYPE_DIMENSION_VALUES
        );
        usageRatio.register(
                () -> {
                    MemoryUsage usage = memoryMxBean.getNonHeapMemoryUsage();
                    return Ratio.of(usage.getUsed(), usage.getMax()).value();
                },
                NON_HEAP_TYPE_DIMENSION_VALUES
        );


        final var initialPoolSize = registry.longVar(
                nameWithSuffix("pools", "init", "bytes"),
                Var.noTotal(),
                longVarConfigBuilderSupplier(INIT_MESSAGE, NAME_DIMENSION)
        );

        final var maxPoolSize = registry.longVar(
                nameWithSuffix("pools", "max", "bytes"),
                Var.noTotal(),
                longVarConfigBuilderSupplier(MAX_MESSAGE, NAME_DIMENSION)

        );

        final var committedPoolSize = registry.longVar(
                nameWithSuffix("pools", "committed", "bytes"),
                Var.noTotal(),
                longVarConfigBuilderSupplier(COMMITTED_MESSAGE, NAME_DIMENSION)

        );

        final var usedPoolSize = registry.longVar(
                nameWithSuffix("pools", "used", "bytes"),
                Var.noTotal(),
                longVarConfigBuilderSupplier(USED_MESSAGE, NAME_DIMENSION)

        );

        final var usagePoolRatio = registry.doubleVar(
                nameWithSuffix("pools", "usage", "ratio"),
                Var.noTotal(),
                doubleVarConfigBuilderSupplier(USAGE_MESSAGE, NAME_DIMENSION)
        );

        final var usedAfterGcPoolRatio = registry.longVar(
                nameWithSuffix("usedAfterGc", "ratio"),
                Var.noTotal(),
                longVarConfigBuilderSupplier(USED_AFTER_GC_MESSAGE, NAME_DIMENSION)
        );

        for (MemoryPoolMXBean pool : memoryPoolMxBeans) {
            final var dimensionValues = dimensionValues(NAME_DIMENSION.value(pool.getName()));
            initialPoolSize.register(pool.getUsage()::getInit, dimensionValues);
            maxPoolSize.register(pool.getUsage()::getMax, dimensionValues);
            committedPoolSize.register(pool.getUsage()::getCommitted, dimensionValues);
            usedPoolSize.register(pool.getUsage()::getUsed, dimensionValues);
            usagePoolRatio.register(
                    () -> {
                        MemoryUsage usage = pool.getUsage();
                        final var max = usage.getMax();
                        final var denominator = max == -1 ? usage.getCommitted() : max;
                        return Ratio.of(usage.getUsed(), denominator).value();
                    },
                    dimensionValues
            );

            if (pool.getCollectionUsage() != null) {
                usedAfterGcPoolRatio.register(pool.getCollectionUsage()::getUsed, dimensionValues);
            }
        }
    }
}
