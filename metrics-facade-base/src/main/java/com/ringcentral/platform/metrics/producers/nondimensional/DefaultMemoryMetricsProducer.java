package com.ringcentral.platform.metrics.producers.nondimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractMemoryMetricsProducer;
import com.ringcentral.platform.metrics.producers.Ratio;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import static com.ringcentral.platform.metrics.names.MetricName.name;
import static java.lang.management.ManagementFactory.getMemoryMXBean;
import static java.lang.management.ManagementFactory.getMemoryPoolMXBeans;
import static org.apache.commons.lang3.StringUtils.split;

public class DefaultMemoryMetricsProducer extends AbstractMemoryMetricsProducer {

    public DefaultMemoryMetricsProducer() {
        this(DEFAULT_NAME_PREFIX, null);
    }

    public DefaultMemoryMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(
            namePrefix,
            metricModBuilder,
            getMemoryMXBean(),
            getMemoryPoolMXBeans());
    }

    public DefaultMemoryMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        MemoryMXBean memoryMxBean,
        List<MemoryPoolMXBean> memoryPoolMxBeans) {

        super(namePrefix, metricModBuilder, memoryMxBean, memoryPoolMxBeans);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        registry.longVar(
                nameWithSuffix("total", "init"),
                () -> memoryMxBean.getHeapMemoryUsage().getInit() + memoryMxBean.getNonHeapMemoryUsage().getInit(),
                longVarConfigBuilderSupplier(INIT_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("total", "used"),
                () -> memoryMxBean.getHeapMemoryUsage().getUsed() + memoryMxBean.getNonHeapMemoryUsage().getUsed(),
                longVarConfigBuilderSupplier(USED_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("total", "max"),
                () -> memoryMxBean.getHeapMemoryUsage().getMax() + memoryMxBean.getNonHeapMemoryUsage().getMax(),
                longVarConfigBuilderSupplier(MAX_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("total", "committed"),
                () -> memoryMxBean.getHeapMemoryUsage().getCommitted() + memoryMxBean.getNonHeapMemoryUsage().getCommitted(),
                longVarConfigBuilderSupplier(COMMITTED_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("heap", "init"),
                () -> memoryMxBean.getHeapMemoryUsage().getInit(),
                longVarConfigBuilderSupplier(INIT_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("heap", "used"),
                () -> memoryMxBean.getHeapMemoryUsage().getUsed(),
                longVarConfigBuilderSupplier(USED_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("heap", "max"),
                () -> memoryMxBean.getHeapMemoryUsage().getMax(),
                longVarConfigBuilderSupplier(MAX_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("heap", "committed"),
                () -> memoryMxBean.getHeapMemoryUsage().getCommitted(),
                longVarConfigBuilderSupplier(COMMITTED_DESCRIPTION));

        registry.doubleVar(
            nameWithSuffix("heap", "usage"),
            () -> {
                MemoryUsage usage = memoryMxBean.getHeapMemoryUsage();
                return Ratio.of(usage.getUsed(), usage.getMax()).value();
            },
            doubleVarConfigBuilderSupplier(USAGE_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("non-heap", "init"),
                () -> memoryMxBean.getNonHeapMemoryUsage().getInit(),
                longVarConfigBuilderSupplier(INIT_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("non-heap", "used"),
                () -> memoryMxBean.getNonHeapMemoryUsage().getUsed(),
                longVarConfigBuilderSupplier(USED_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("non-heap", "max"),
                () -> memoryMxBean.getNonHeapMemoryUsage().getMax(),
                longVarConfigBuilderSupplier(MAX_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("non-heap", "committed"),
                () -> memoryMxBean.getNonHeapMemoryUsage().getCommitted(),
                longVarConfigBuilderSupplier(COMMITTED_DESCRIPTION));

        registry.doubleVar(
            nameWithSuffix("non-heap", "usage"),
            () -> {
                MemoryUsage usage = memoryMxBean.getNonHeapMemoryUsage();
                return Ratio.of(usage.getUsed(), usage.getMax()).value();
            },
            doubleVarConfigBuilderSupplier(USAGE_DESCRIPTION));

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
                doubleVarConfigBuilderSupplier(USAGE_DESCRIPTION));

            registry.longVar(
                    name(namePrefix, "max"),
                    () -> pool.getUsage().getMax(),
                    longVarConfigBuilderSupplier(MAX_DESCRIPTION));

            registry.longVar(
                    name(namePrefix, "used"),
                    () -> pool.getUsage().getUsed(),
                    longVarConfigBuilderSupplier(USED_DESCRIPTION));

            registry.longVar(
                    name(namePrefix, "committed"),
                    () -> pool.getUsage().getCommitted(),
                    longVarConfigBuilderSupplier(COMMITTED_DESCRIPTION));

            if (pool.getCollectionUsage() != null) {
                registry.longVar(
                        name(namePrefix, "used-after-gc"),
                        () -> pool.getCollectionUsage().getUsed(),
                        longVarConfigBuilderSupplier(USED_AFTER_GC_DESCRIPTION));
            }

            registry.longVar(
                    name(namePrefix, "init"),
                    () -> pool.getUsage().getInit(),
                    longVarConfigBuilderSupplier(INIT_DESCRIPTION));
        }
    }
}
