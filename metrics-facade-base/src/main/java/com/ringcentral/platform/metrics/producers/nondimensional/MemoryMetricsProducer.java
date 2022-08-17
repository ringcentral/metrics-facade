package com.ringcentral.platform.metrics.producers.nondimensional;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractMetricsProducer;
import com.ringcentral.platform.metrics.producers.Ratio;

import java.lang.management.*;
import java.util.List;

import static com.ringcentral.platform.metrics.names.MetricName.*;
import static java.lang.management.ManagementFactory.*;
import static java.util.Objects.*;
import static org.apache.commons.lang3.StringUtils.*;

public class MemoryMetricsProducer extends AbstractMetricsProducer {

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("Memory");

    private final MemoryMXBean memoryMxBean;
    private final List<MemoryPoolMXBean> memoryPoolMxBeans;

    public MemoryMetricsProducer() {
        this(DEFAULT_NAME_PREFIX, null);
    }

    public MemoryMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(
            namePrefix,
            metricModBuilder,
            getMemoryMXBean(),
            getMemoryPoolMXBeans());
    }

    public MemoryMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        MemoryMXBean memoryMxBean,
        List<MemoryPoolMXBean> memoryPoolMxBeans) {

        super(namePrefix, metricModBuilder);

        this.memoryMxBean = requireNonNull(memoryMxBean);
        this.memoryPoolMxBeans = requireNonNull(memoryPoolMxBeans);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        registry.longVar(nameWithSuffix("total", "init"), () -> memoryMxBean.getHeapMemoryUsage().getInit() + memoryMxBean.getNonHeapMemoryUsage().getInit(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("total", "used"), () -> memoryMxBean.getHeapMemoryUsage().getUsed() + memoryMxBean.getNonHeapMemoryUsage().getUsed(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("total", "max"), () -> memoryMxBean.getHeapMemoryUsage().getMax() + memoryMxBean.getNonHeapMemoryUsage().getMax(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("total", "committed"), () -> memoryMxBean.getHeapMemoryUsage().getCommitted() + memoryMxBean.getNonHeapMemoryUsage().getCommitted(), longVarConfigBuilderSupplier());

        registry.longVar(nameWithSuffix("heap", "init"), () -> memoryMxBean.getHeapMemoryUsage().getInit(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("heap", "used"), () -> memoryMxBean.getHeapMemoryUsage().getUsed(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("heap", "max"), () -> memoryMxBean.getHeapMemoryUsage().getMax(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("heap", "committed"), () -> memoryMxBean.getHeapMemoryUsage().getCommitted(), longVarConfigBuilderSupplier());

        registry.doubleVar(
            nameWithSuffix("heap", "usage"),
            () -> {
                MemoryUsage usage = memoryMxBean.getHeapMemoryUsage();
                return Ratio.of(usage.getUsed(), usage.getMax()).value();
            },
            doubleVarConfigBuilderSupplier());

        registry.longVar(nameWithSuffix("non-heap", "init"), () -> memoryMxBean.getNonHeapMemoryUsage().getInit(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("non-heap", "used"), () -> memoryMxBean.getNonHeapMemoryUsage().getUsed(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("non-heap", "max"), () -> memoryMxBean.getNonHeapMemoryUsage().getMax(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("non-heap", "committed"), () -> memoryMxBean.getNonHeapMemoryUsage().getCommitted(), longVarConfigBuilderSupplier());

        registry.doubleVar(
            nameWithSuffix("non-heap", "usage"),
            () -> {
                MemoryUsage usage = memoryMxBean.getNonHeapMemoryUsage();
                return Ratio.of(usage.getUsed(), usage.getMax()).value();
            },
            doubleVarConfigBuilderSupplier());

        for (MemoryPoolMXBean pool : memoryPoolMxBeans) {
            MetricName namePrefix = nameWithSuffix(name(
                name("pools"),
                split(WHITESPACE_PATTERN.matcher(pool.getName()).replaceAll("-"), ".")));

            registry.doubleVar(
                name(namePrefix, "usage"),
                () -> {
                    MemoryUsage usage = pool.getUsage();
                    return Ratio.of(usage.getUsed(), usage.getMax() == -1 ? usage.getCommitted() : usage.getMax()).value();
                },
                doubleVarConfigBuilderSupplier());

            registry.longVar(name(namePrefix, "max"), () -> pool.getUsage().getMax(), longVarConfigBuilderSupplier());
            registry.longVar(name(namePrefix, "used"), () -> pool.getUsage().getUsed(), longVarConfigBuilderSupplier());
            registry.longVar(name(namePrefix, "committed"), () -> pool.getUsage().getCommitted(), longVarConfigBuilderSupplier());

            if (pool.getCollectionUsage() != null) {
                registry.longVar(name(namePrefix, "used-after-gc"), () -> pool.getCollectionUsage().getUsed(), longVarConfigBuilderSupplier());
            }

            registry.longVar(name(namePrefix, "init"), () -> pool.getUsage().getInit(), longVarConfigBuilderSupplier());
        }
    }
}
