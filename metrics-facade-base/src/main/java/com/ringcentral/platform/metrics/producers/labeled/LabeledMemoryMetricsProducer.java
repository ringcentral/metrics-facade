package com.ringcentral.platform.metrics.producers.labeled;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractMemoryMetricsProducer;
import com.ringcentral.platform.metrics.producers.Ratio;
import com.ringcentral.platform.metrics.var.Var;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import static com.ringcentral.platform.metrics.labels.LabelValues.labelValues;
import static java.lang.management.ManagementFactory.getMemoryMXBean;
import static java.lang.management.ManagementFactory.getMemoryPoolMXBeans;

/**
 * Produces<br>
 * <ul>
 *     <li>
 *         <i>init</i> - the amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management.<br>
 *         Labels:<br>
 *         type = {"heap", "total", "non-heap"}<br>
 *     </li>
 *     <li>
 *         <i>max</i> - the maximum amount of memory in bytes that can be used for memory management.<br>
 *         Labels:<br>
 *         type = {"heap", "total", "non-heap"}<br>
 *     </li>
 *     <li>
 *         <i>used</i> - the amount of used memory in bytes.<br>
 *         Labels:<br>
 *         type = {"heap", "total", "non-heap"}<br>
 *     </li>
 *     <li>
 *         <i>usage</i> - used divided by max.<br>
 *         Labels:<br>
 *         type = {"heap", "non-heap"}<br>
 *     </li>
 *     <li>
 *         <i>committed</i> - the amount of memory in bytes that is committed for the Java virtual machine to use.<br>
 *         Labels:<br>
 *         type = {"heap", "total", "non-heap"}<br>
 *     </li>
 *     <li>
 *         <i>usedAfterGc</i> - the amount of used memory in bytes after the Java virtual machine most recently expended effort in recycling unused objects in this memory pool.<br>
 *         Labels:<br>
 *         name = {"G1 Old Gen", "G1 Eden Space", "G1 Survivor Space"}<br>
 *     </li>
 *     <li>
 *         <i>pools.init</i> - the amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management.<br>
 *         Labels:<br>
 *         type = {"G1 Old Gen", "Compressed Class Space", "G1 Eden Space", "G1 Survivor Space", "CodeHeap 'non-profiled nmethods'", "CodeHeap 'non-nmethods'", "CodeHeap 'profiled nmethods'", "Metaspace"}<br>
 *     </li>
 *     <li>
 *         <i>pools.max</i> - the maximum amount of memory in bytes that can be used for memory management.<br>
 *         Labels:<br>
 *         type = {"G1 Old Gen", "Compressed Class Space", "G1 Eden Space", "G1 Survivor Space", "CodeHeap 'non-profiled nmethods'", "CodeHeap 'non-nmethods'", "CodeHeap 'profiled nmethods'", "Metaspace"}<br>
 *     </li>
 *     <li>
 *         <i>pools.used</i> - the amount of used memory in bytes.<br>
 *         Labels:<br>
 *         type = {"G1 Old Gen", "Compressed Class Space", "G1 Eden Space", "G1 Survivor Space", "CodeHeap 'non-profiled nmethods'", "CodeHeap 'non-nmethods'", "CodeHeap 'profiled nmethods'", "Metaspace"}<br>
 *     </li>
 *     <li>
 *         <i>pools.usage</i> - used divided by max.<br>
 *         Labels:<br>
 *         type = {"G1 Old Gen", "Compressed Class Space", "G1 Eden Space", "G1 Survivor Space", "CodeHeap 'non-profiled nmethods'", "CodeHeap 'non-nmethods'", "CodeHeap 'profiled nmethods'", "Metaspace"}<br>
 *     </li>
 *     <li>
 *         <i>pools.committed</i> - the amount of memory in bytes that is committed for the Java virtual machine to use.<br>
 *         Labels:<br>
 *         type = {"G1 Old Gen", "Compressed Class Space", "G1 Eden Space", "G1 Survivor Space", "CodeHeap 'non-profiled nmethods'", "CodeHeap 'non-nmethods'", "CodeHeap 'profiled nmethods'", "Metaspace"}<br>
 *     </li>
 * </ul>
 *
 * All metrics have a name prefix. By default it is 'Memory'.<br>
 * <br>
 * Example of usage:
 * <pre>
 * MetricRegistry registry = new DefaultMetricRegistry();
 * new LabeledMemoryMetricsProducer().produceMetrics(registry);
 * PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);
 * System.out.println(exporter.exportMetrics());
 * </pre>
 * Corresponding output:
 * <pre>
 * # HELP Memory_committed The amount of memory in bytes that is committed for the Java virtual machine to use
 * # TYPE Memory_committed gauge
 * Memory_committed{type="heap",} 2.68435456E8
 * Memory_committed{type="total",} 2.86851072E8
 * Memory_committed{type="non-heap",} 1.8153472E7
 * # HELP Memory_usedAfterGc The amount of used memory in bytes after the Java virtual machine most recently expended effort in recycling unused objects in this memory pool
 * # TYPE Memory_usedAfterGc gauge
 * Memory_usedAfterGc{name="G1 Old Gen",} 0.0
 * Memory_usedAfterGc{name="G1 Eden Space",} 0.0
 * Memory_usedAfterGc{name="G1 Survivor Space",} 0.0
 * # HELP Memory_usage Used divided by max
 * # TYPE Memory_usage gauge
 * Memory_usage{type="heap",} 0.001953125
 * Memory_usage{type="non-heap",} -1.2532952E7
 * # HELP Memory_pools_committed The amount of memory in bytes that is committed for the Java virtual machine to use
 * # TYPE Memory_pools_committed gauge
 * Memory_pools_committed{name="G1 Old Gen",} 2.4117248E8
 * Memory_pools_committed{name="Compressed Class Space",} 1048576.0
 * Memory_pools_committed{name="G1 Eden Space",} 2.7262976E7
 * Memory_pools_committed{name="G1 Survivor Space",} 0.0
 * Memory_pools_committed{name="CodeHeap 'non-profiled nmethods'",} 2555904.0
 * Memory_pools_committed{name="CodeHeap 'non-nmethods'",} 2555904.0
 * Memory_pools_committed{name="CodeHeap 'profiled nmethods'",} 2555904.0
 * Memory_pools_committed{name="Metaspace",} 9437184.0
 * # HELP Memory_used The amount of used memory in bytes
 * # TYPE Memory_used gauge
 * Memory_used{type="heap",} 7340032.0
 * Memory_used{type="total",} 2.092876E7
 * Memory_used{type="non-heap",} 1.1884776E7
 * # HELP Memory_max The maximum amount of memory in bytes that can be used for memory management
 * # TYPE Memory_max gauge
 * Memory_max{type="heap",} 4.294967296E9
 * Memory_max{type="total",} 4.294967295E9
 * Memory_max{type="non-heap",} -1.0
 * # HELP Memory_pools_init The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management
 * # TYPE Memory_pools_init gauge
 * Memory_pools_init{name="G1 Old Gen",} 2.4117248E8
 * Memory_pools_init{name="Compressed Class Space",} 0.0
 * Memory_pools_init{name="G1 Eden Space",} 2.7262976E7
 * Memory_pools_init{name="G1 Survivor Space",} 0.0
 * Memory_pools_init{name="CodeHeap 'non-profiled nmethods'",} 2555904.0
 * Memory_pools_init{name="CodeHeap 'non-nmethods'",} 2555904.0
 * Memory_pools_init{name="CodeHeap 'profiled nmethods'",} 2555904.0
 * Memory_pools_init{name="Metaspace",} 0.0
 * # HELP Memory_pools_usage Used divided by max
 * # TYPE Memory_pools_usage gauge
 * Memory_pools_usage{name="G1 Old Gen",} 0.0
 * Memory_pools_usage{name="Compressed Class Space",} 8.621141314506531E-4
 * Memory_pools_usage{name="G1 Eden Space",} 0.3076923076923077
 * Memory_pools_usage{name="G1 Survivor Space",} NaN
 * Memory_pools_usage{name="CodeHeap 'non-profiled nmethods'",} 0.0015579178885630498
 * Memory_pools_usage{name="CodeHeap 'non-nmethods'",} 0.18679824561403507
 * Memory_pools_usage{name="CodeHeap 'profiled nmethods'",} 0.007654463958409704
 * Memory_pools_usage{name="Metaspace",} 0.9689644478462838
 * # HELP Memory_init The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management
 * # TYPE Memory_init gauge
 * Memory_init{type="heap",} 2.68435456E8
 * Memory_init{type="total",} 2.76103168E8
 * Memory_init{type="non-heap",} 7667712.0
 * # HELP Memory_pools_max The maximum amount of memory in bytes that can be used for memory management
 * # TYPE Memory_pools_max gauge
 * Memory_pools_max{name="G1 Old Gen",} 4.294967296E9
 * Memory_pools_max{name="Compressed Class Space",} 1.073741824E9
 * Memory_pools_max{name="G1 Eden Space",} -1.0
 * Memory_pools_max{name="G1 Survivor Space",} -1.0
 * Memory_pools_max{name="CodeHeap 'non-profiled nmethods'",} 1.22912768E8
 * Memory_pools_max{name="CodeHeap 'non-nmethods'",} 5836800.0
 * Memory_pools_max{name="CodeHeap 'profiled nmethods'",} 1.22908672E8
 * Memory_pools_max{name="Metaspace",} -1.0
 * # HELP Memory_pools_used The amount of used memory in bytes
 * # TYPE Memory_pools_used gauge
 * Memory_pools_used{name="G1 Old Gen",} 0.0
 * Memory_pools_used{name="Compressed Class Space",} 891592.0
 * Memory_pools_used{name="G1 Eden Space",} 7340032.0
 * Memory_pools_used{name="G1 Survivor Space",} 0.0
 * Memory_pools_used{name="CodeHeap 'non-profiled nmethods'",} 167680.0
 * Memory_pools_used{name="CodeHeap 'non-nmethods'",} 1088896.0
 * Memory_pools_used{name="CodeHeap 'profiled nmethods'",} 862848.0
 * Memory_pools_used{name="Metaspace",} 9053760.0
 * </pre>
 */
public class LabeledMemoryMetricsProducer extends AbstractMemoryMetricsProducer {

    private static final Label NAME_LABEL = new Label("name");
    private static final Label TYPE_LABEL = new Label("type");
    private static final LabelValues HEAP_TYPE_LABEL_VALUES = labelValues(TYPE_LABEL.value("heap"));
    private static final LabelValues NON_HEAP_TYPE_LABEL_VALUES = labelValues(TYPE_LABEL.value("non-heap"));
    private static final LabelValues TOTAL_TYPE_LABEL_VALUES = labelValues(TYPE_LABEL.value("total"));

    public LabeledMemoryMetricsProducer() {
        this(DEFAULT_NAME_PREFIX, null);
    }

    public LabeledMemoryMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(
            namePrefix,
            metricModBuilder,
            getMemoryMXBean(),
            getMemoryPoolMXBeans());
    }

    public LabeledMemoryMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        MemoryMXBean memoryMxBean,
        List<MemoryPoolMXBean> memoryPoolMxBeans) {

        super(namePrefix, metricModBuilder, memoryMxBean, memoryPoolMxBeans);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        final var initialSize = registry.longVar(
            nameWithSuffix("init"),
            Var.noTotal(),
            longVarConfigBuilderSupplier(INIT_DESCRIPTION, TYPE_LABEL));

        initialSize.register(
            () -> memoryMxBean.getHeapMemoryUsage().getInit() + memoryMxBean.getNonHeapMemoryUsage().getInit(),
            TOTAL_TYPE_LABEL_VALUES);

        initialSize.register(() -> memoryMxBean.getHeapMemoryUsage().getInit(), HEAP_TYPE_LABEL_VALUES);
        initialSize.register(() -> memoryMxBean.getNonHeapMemoryUsage().getInit(), NON_HEAP_TYPE_LABEL_VALUES);

        final var usedSize = registry.longVar(
            nameWithSuffix("used"),
            Var.noTotal(),
            longVarConfigBuilderSupplier(USED_DESCRIPTION, TYPE_LABEL));

        usedSize.register(
            () -> memoryMxBean.getHeapMemoryUsage().getUsed() + memoryMxBean.getNonHeapMemoryUsage().getUsed(),
            TOTAL_TYPE_LABEL_VALUES);

        usedSize.register(() -> memoryMxBean.getHeapMemoryUsage().getUsed(), HEAP_TYPE_LABEL_VALUES);
        usedSize.register(() -> memoryMxBean.getNonHeapMemoryUsage().getUsed(), NON_HEAP_TYPE_LABEL_VALUES);

        final var maxSize = registry.longVar(
            nameWithSuffix("max"),
            Var.noTotal(),
            longVarConfigBuilderSupplier(MAX_DESCRIPTION, TYPE_LABEL));

        maxSize.register(() -> memoryMxBean.getHeapMemoryUsage().getMax() + memoryMxBean.getNonHeapMemoryUsage().getMax(), TOTAL_TYPE_LABEL_VALUES);

        maxSize.register(() -> memoryMxBean.getHeapMemoryUsage().getMax(), HEAP_TYPE_LABEL_VALUES);
        maxSize.register(() -> memoryMxBean.getNonHeapMemoryUsage().getMax(), NON_HEAP_TYPE_LABEL_VALUES);

        final var committedSize = registry.longVar(
            nameWithSuffix("committed"),
            Var.noTotal(),
            longVarConfigBuilderSupplier(COMMITTED_DESCRIPTION, TYPE_LABEL));

        committedSize.register(
            () -> memoryMxBean.getHeapMemoryUsage().getCommitted() + memoryMxBean.getNonHeapMemoryUsage().getCommitted(),
            TOTAL_TYPE_LABEL_VALUES);

        committedSize.register(() -> memoryMxBean.getHeapMemoryUsage().getCommitted(), HEAP_TYPE_LABEL_VALUES);
        committedSize.register(() -> memoryMxBean.getNonHeapMemoryUsage().getCommitted(), NON_HEAP_TYPE_LABEL_VALUES);

        final var usageRatio = registry.doubleVar(
            nameWithSuffix("usage"),
            Var.noTotal(),
            doubleVarConfigBuilderSupplier(USAGE_DESCRIPTION, TYPE_LABEL));

        usageRatio.register(
            () -> {
                MemoryUsage usage = memoryMxBean.getHeapMemoryUsage();
                return Ratio.of(usage.getUsed(), usage.getMax()).value();
            },
            HEAP_TYPE_LABEL_VALUES);

        usageRatio.register(
            () -> {
                MemoryUsage usage = memoryMxBean.getNonHeapMemoryUsage();
                return Ratio.of(usage.getUsed(), usage.getMax()).value();
            },
            NON_HEAP_TYPE_LABEL_VALUES);

        final var initialPoolSize = registry.longVar(
            nameWithSuffix("pools", "init"),
            Var.noTotal(),
            longVarConfigBuilderSupplier(INIT_DESCRIPTION, NAME_LABEL));

        final var maxPoolSize = registry.longVar(
            nameWithSuffix("pools", "max"),
            Var.noTotal(),
            longVarConfigBuilderSupplier(MAX_DESCRIPTION, NAME_LABEL));

        final var committedPoolSize = registry.longVar(
            nameWithSuffix("pools", "committed"),
            Var.noTotal(),
            longVarConfigBuilderSupplier(COMMITTED_DESCRIPTION, NAME_LABEL));

        final var usedPoolSize = registry.longVar(
            nameWithSuffix("pools", "used"),
            Var.noTotal(),
            longVarConfigBuilderSupplier(USED_DESCRIPTION, NAME_LABEL));

        final var usagePoolRatio = registry.doubleVar(
            nameWithSuffix("pools", "usage"),
            Var.noTotal(),
            doubleVarConfigBuilderSupplier(USAGE_DESCRIPTION, NAME_LABEL));

        final var usedAfterGcPoolRatio = registry.longVar(
            nameWithSuffix("usedAfterGc"),
            Var.noTotal(),
            longVarConfigBuilderSupplier(USED_AFTER_GC_DESCRIPTION, NAME_LABEL));

        for (MemoryPoolMXBean pool : memoryPoolMxBeans) {
            final var labelValues = labelValues(NAME_LABEL.value(pool.getName()));
            initialPoolSize.register(() -> pool.getUsage().getInit(), labelValues);
            maxPoolSize.register(() -> pool.getUsage().getMax(), labelValues);
            committedPoolSize.register(() -> pool.getUsage().getCommitted(), labelValues);
            usedPoolSize.register(() -> pool.getUsage().getUsed(), labelValues);

            usagePoolRatio.register(
                () -> {
                    MemoryUsage usage = pool.getUsage();
                    final var max = usage.getMax();
                    final var denominator = max == -1 ? usage.getCommitted() : max;
                    return Ratio.of(usage.getUsed(), denominator).value();
                },
                labelValues);

            if (pool.getCollectionUsage() != null) {
                usedAfterGcPoolRatio.register(() -> pool.getCollectionUsage().getUsed(), labelValues);
            }
        }
    }
}
