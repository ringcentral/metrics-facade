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

/**
 * Produces<br>
 * <ul>
 *     <li>
 *         <i>{type}.init</i> - the amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management.<br>
 *         type = {"heap", "total", "non-heap"}<br>
 *     </li>
 *     <li>
 *         <i>{type}.max</i> - the maximum amount of memory in bytes that can be used for memory management.<br>
 *         type = {"heap", "total", "non-heap"}<br>
 *     </li>
 *     <li>
 *         <i>{type}.used</i> - the amount of used memory in bytes.<br>
 *         type = {"heap", "total", "non-heap"}<br>
 *     </li>
 *     <li>
 *         <i>{type}.usage</i> - used divided by max.<br>
 *         type = {"heap", "non-heap"}<br>
 *     </li>
 *     <li>
 *         <i>{type}.committed</i> - the amount of memory in bytes that is committed for the Java virtual machine to use.<br>
 *         type = {"heap", "total", "non-heap"}<br>
 *     </li>
 *     <li>
 *         <i>pools.{pool}.used-after-gc</i> - the amount of used memory in bytes after the Java virtual machine most recently expended effort in recycling unused objects in this memory pool.<br>
 *         pool = {"G1 Old Gen", "G1 Eden Space", "G1 Survivor Space"}<br>
 *     </li>
 *     <li>
 *         <i>pools.{pool}.init</i> - the amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management.<br>
 *         pool = {"G1 Old Gen", "Compressed Class Space", "G1 Eden Space", "G1 Survivor Space", "CodeHeap 'non-profiled nmethods'", "CodeHeap 'non-nmethods'", "CodeHeap 'profiled nmethods'", "Metaspace"}<br>
 *     </li>
 *     <li>
 *         <i>pools.{pool}.max</i> - the maximum amount of memory in bytes that can be used for memory management.<br>
 *         pool = {"G1 Old Gen", "Compressed Class Space", "G1 Eden Space", "G1 Survivor Space", "CodeHeap 'non-profiled nmethods'", "CodeHeap 'non-nmethods'", "CodeHeap 'profiled nmethods'", "Metaspace"}<br>
 *     </li>
 *     <li>
 *         <i>pools.{pool}.used</i> - the amount of used memory in bytes.<br>
 *         pool = {"G1 Old Gen", "Compressed Class Space", "G1 Eden Space", "G1 Survivor Space", "CodeHeap 'non-profiled nmethods'", "CodeHeap 'non-nmethods'", "CodeHeap 'profiled nmethods'", "Metaspace"}<br>
 *     </li>
 *     <li>
 *         <i>pools.{pool}.usage</i> - used divided by max.<br>
 *         pool = {"G1 Old Gen", "Compressed Class Space", "G1 Eden Space", "G1 Survivor Space", "CodeHeap 'non-profiled nmethods'", "CodeHeap 'non-nmethods'", "CodeHeap 'profiled nmethods'", "Metaspace"}<br>
 *     </li>
 *     <li>
 *         <i>pools.{pool}.committed</i> - the amount of memory in bytes that is committed for the Java virtual machine to use.<br>
 *         pool = {"G1 Old Gen", "Compressed Class Space", "G1 Eden Space", "G1 Survivor Space", "CodeHeap 'non-profiled nmethods'", "CodeHeap 'non-nmethods'", "CodeHeap 'profiled nmethods'", "Metaspace"}<br>
 *     </li>
 * </ul>
 *
 * All metrics have a name prefix. By default it is 'Memory'.<br>
 * <br>
 * Example of usage:
 * <pre>
 * MetricRegistry registry = new DefaultMetricRegistry();
 * new DefaultMemoryMetricsProducer().produceMetrics(registry);
 * PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);
 * System.out.println(exporter.exportMetrics());
 * </pre>
 * Corresponding output:
 * <pre>
 * # HELP Memory_pools_G1_Survivor_Space_usage Used divided by max
 * # TYPE Memory_pools_G1_Survivor_Space_usage gauge
 * Memory_pools_G1_Survivor_Space_usage NaN
 * # HELP Memory_heap_usage Used divided by max
 * # TYPE Memory_heap_usage gauge
 * Memory_heap_usage 0.001708984375
 * # HELP Memory_pools_G1_Old_Gen_max The maximum amount of memory in bytes that can be used for memory management
 * # TYPE Memory_pools_G1_Old_Gen_max gauge
 * Memory_pools_G1_Old_Gen_max 4.294967296E9
 * # HELP Memory_pools_CodeHeap__non_profiled_nmethods__usage Used divided by max
 * # TYPE Memory_pools_CodeHeap__non_profiled_nmethods__usage gauge
 * Memory_pools_CodeHeap__non_profiled_nmethods__usage 0.0014621101039722741
 * # HELP Memory_pools_G1_Old_Gen_init The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management
 * # TYPE Memory_pools_G1_Old_Gen_init gauge
 * Memory_pools_G1_Old_Gen_init 2.4117248E8
 * # HELP Memory_pools_G1_Old_Gen_committed The amount of memory in bytes that is committed for the Java virtual machine to use
 * # TYPE Memory_pools_G1_Old_Gen_committed gauge
 * Memory_pools_G1_Old_Gen_committed 2.4117248E8
 * # HELP Memory_heap_used The amount of used memory in bytes
 * # TYPE Memory_heap_used gauge
 * Memory_heap_used 7340032.0
 * # HELP Memory_pools_Metaspace_max The maximum amount of memory in bytes that can be used for memory management
 * # TYPE Memory_pools_Metaspace_max gauge
 * Memory_pools_Metaspace_max -1.0
 * # HELP Memory_pools_Compressed_Class_Space_init The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management
 * # TYPE Memory_pools_Compressed_Class_Space_init gauge
 * Memory_pools_Compressed_Class_Space_init 0.0
 * # HELP Memory_pools_G1_Old_Gen_usage Used divided by max
 * # TYPE Memory_pools_G1_Old_Gen_usage gauge
 * Memory_pools_G1_Old_Gen_usage 0.0
 * # HELP Memory_heap_init The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management
 * # TYPE Memory_heap_init gauge
 * Memory_heap_init 2.68435456E8
 * # HELP Memory_pools_Compressed_Class_Space_usage Used divided by max
 * # TYPE Memory_pools_Compressed_Class_Space_usage gauge
 * Memory_pools_Compressed_Class_Space_usage 8.50968062877655E-4
 * # HELP Memory_pools_Metaspace_committed The amount of memory in bytes that is committed for the Java virtual machine to use
 * # TYPE Memory_pools_Metaspace_committed gauge
 * Memory_pools_Metaspace_committed 9699328.0
 * # HELP Memory_pools_G1_Survivor_Space_committed The amount of memory in bytes that is committed for the Java virtual machine to use
 * # TYPE Memory_pools_G1_Survivor_Space_committed gauge
 * Memory_pools_G1_Survivor_Space_committed 0.0
 * # HELP Memory_pools_CodeHeap__non_profiled_nmethods__used The amount of used memory in bytes
 * # TYPE Memory_pools_CodeHeap__non_profiled_nmethods__used gauge
 * Memory_pools_CodeHeap__non_profiled_nmethods__used 179712.0
 * # HELP Memory_pools_G1_Survivor_Space_init The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management
 * # TYPE Memory_pools_G1_Survivor_Space_init gauge
 * Memory_pools_G1_Survivor_Space_init 0.0
 * # HELP Memory_pools_CodeHeap__non_profiled_nmethods__init The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management
 * # TYPE Memory_pools_CodeHeap__non_profiled_nmethods__init gauge
 * Memory_pools_CodeHeap__non_profiled_nmethods__init 2555904.0
 * # HELP Memory_pools_CodeHeap__profiled_nmethods__usage Used divided by max
 * # TYPE Memory_pools_CodeHeap__profiled_nmethods__usage gauge
 * Memory_pools_CodeHeap__profiled_nmethods__usage 0.007595102809344487
 * # HELP Memory_pools_CodeHeap__non_nmethods__max The maximum amount of memory in bytes that can be used for memory management
 * # TYPE Memory_pools_CodeHeap__non_nmethods__max gauge
 * Memory_pools_CodeHeap__non_nmethods__max 5836800.0
 * # HELP Memory_pools_CodeHeap__non_profiled_nmethods__max The maximum amount of memory in bytes that can be used for memory management
 * # TYPE Memory_pools_CodeHeap__non_profiled_nmethods__max gauge
 * Memory_pools_CodeHeap__non_profiled_nmethods__max 1.22912768E8
 * # HELP Memory_pools_Metaspace_used The amount of used memory in bytes
 * # TYPE Memory_pools_Metaspace_used gauge
 * Memory_pools_Metaspace_used 9312008.0
 * # HELP Memory_pools_Compressed_Class_Space_max The maximum amount of memory in bytes that can be used for memory management
 * # TYPE Memory_pools_Compressed_Class_Space_max gauge
 * Memory_pools_Compressed_Class_Space_max 1.073741824E9
 * # HELP Memory_pools_Compressed_Class_Space_used The amount of used memory in bytes
 * # TYPE Memory_pools_Compressed_Class_Space_used gauge
 * Memory_pools_Compressed_Class_Space_used 913720.0
 * # HELP Memory_non_heap_max The maximum amount of memory in bytes that can be used for memory management
 * # TYPE Memory_non_heap_max gauge
 * Memory_non_heap_max -1.0
 * # HELP Memory_heap_committed The amount of memory in bytes that is committed for the Java virtual machine to use
 * # TYPE Memory_heap_committed gauge
 * Memory_heap_committed 2.68435456E8
 * # HELP Memory_pools_G1_Eden_Space_committed The amount of memory in bytes that is committed for the Java virtual machine to use
 * # TYPE Memory_pools_G1_Eden_Space_committed gauge
 * Memory_pools_G1_Eden_Space_committed 2.7262976E7
 * # HELP Memory_total_committed The amount of memory in bytes that is committed for the Java virtual machine to use
 * # TYPE Memory_total_committed gauge
 * Memory_total_committed 2.86851072E8
 * # HELP Memory_pools_CodeHeap__non_nmethods__used The amount of used memory in bytes
 * # TYPE Memory_pools_CodeHeap__non_nmethods__used gauge
 * Memory_pools_CodeHeap__non_nmethods__used 1084416.0
 * # HELP Memory_pools_CodeHeap__non_nmethods__usage Used divided by max
 * # TYPE Memory_pools_CodeHeap__non_nmethods__usage gauge
 * Memory_pools_CodeHeap__non_nmethods__usage 0.18578947368421053
 * # HELP Memory_non_heap_usage Used divided by max
 * # TYPE Memory_non_heap_usage gauge
 * Memory_non_heap_usage -1.2424848E7
 * # HELP Memory_total_max The maximum amount of memory in bytes that can be used for memory management
 * # TYPE Memory_total_max gauge
 * Memory_total_max 4.294967295E9
 * # HELP Memory_pools_CodeHeap__profiled_nmethods__init The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management
 * # TYPE Memory_pools_CodeHeap__profiled_nmethods__init gauge
 * Memory_pools_CodeHeap__profiled_nmethods__init 2555904.0
 * # HELP Memory_pools_CodeHeap__profiled_nmethods__max The maximum amount of memory in bytes that can be used for memory management
 * # TYPE Memory_pools_CodeHeap__profiled_nmethods__max gauge
 * Memory_pools_CodeHeap__profiled_nmethods__max 1.22908672E8
 * # HELP Memory_pools_G1_Eden_Space_usage Used divided by max
 * # TYPE Memory_pools_G1_Eden_Space_usage gauge
 * Memory_pools_G1_Eden_Space_usage 0.2692307692307692
 * # HELP Memory_non_heap_used The amount of used memory in bytes
 * # TYPE Memory_non_heap_used gauge
 * Memory_non_heap_used 1.2425584E7
 * # HELP Memory_total_init The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management
 * # TYPE Memory_total_init gauge
 * Memory_total_init 2.76103168E8
 * # HELP Memory_pools_CodeHeap__profiled_nmethods__committed The amount of memory in bytes that is committed for the Java virtual machine to use
 * # TYPE Memory_pools_CodeHeap__profiled_nmethods__committed gauge
 * Memory_pools_CodeHeap__profiled_nmethods__committed 2555904.0
 * # HELP Memory_non_heap_init The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management
 * # TYPE Memory_non_heap_init gauge
 * Memory_non_heap_init 7667712.0
 * # HELP Memory_pools_G1_Eden_Space_used The amount of used memory in bytes
 * # TYPE Memory_pools_G1_Eden_Space_used gauge
 * Memory_pools_G1_Eden_Space_used 7340032.0
 * # HELP Memory_pools_CodeHeap__non_nmethods__committed The amount of memory in bytes that is committed for the Java virtual machine to use
 * # TYPE Memory_pools_CodeHeap__non_nmethods__committed gauge
 * Memory_pools_CodeHeap__non_nmethods__committed 2555904.0
 * # HELP Memory_pools_CodeHeap__non_nmethods__init The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management
 * # TYPE Memory_pools_CodeHeap__non_nmethods__init gauge
 * Memory_pools_CodeHeap__non_nmethods__init 2555904.0
 * # HELP Memory_pools_G1_Eden_Space_max The maximum amount of memory in bytes that can be used for memory management
 * # TYPE Memory_pools_G1_Eden_Space_max gauge
 * Memory_pools_G1_Eden_Space_max -1.0
 * # HELP Memory_total_used The amount of used memory in bytes
 * # TYPE Memory_total_used gauge
 * Memory_total_used 1.9766992E7
 * # HELP Memory_pools_Metaspace_usage Used divided by max
 * # TYPE Memory_pools_Metaspace_usage gauge
 * Memory_pools_Metaspace_usage 0.9602669380806588
 * # HELP Memory_pools_G1_Eden_Space_init The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management
 * # TYPE Memory_pools_G1_Eden_Space_init gauge
 * Memory_pools_G1_Eden_Space_init 2.7262976E7
 * # HELP Memory_pools_G1_Survivor_Space_used_after_gc The amount of used memory in bytes after the Java virtual machine most recently expended effort in recycling unused objects in this memory pool
 * # TYPE Memory_pools_G1_Survivor_Space_used_after_gc gauge
 * Memory_pools_G1_Survivor_Space_used_after_gc 0.0
 * # HELP Memory_pools_G1_Eden_Space_used_after_gc The amount of used memory in bytes after the Java virtual machine most recently expended effort in recycling unused objects in this memory pool
 * # TYPE Memory_pools_G1_Eden_Space_used_after_gc gauge
 * Memory_pools_G1_Eden_Space_used_after_gc 0.0
 * # HELP Memory_pools_Compressed_Class_Space_committed The amount of memory in bytes that is committed for the Java virtual machine to use
 * # TYPE Memory_pools_Compressed_Class_Space_committed gauge
 * Memory_pools_Compressed_Class_Space_committed 1048576.0
 * # HELP Memory_pools_G1_Survivor_Space_max The maximum amount of memory in bytes that can be used for memory management
 * # TYPE Memory_pools_G1_Survivor_Space_max gauge
 * Memory_pools_G1_Survivor_Space_max -1.0
 * # HELP Memory_pools_G1_Old_Gen_used_after_gc The amount of used memory in bytes after the Java virtual machine most recently expended effort in recycling unused objects in this memory pool
 * # TYPE Memory_pools_G1_Old_Gen_used_after_gc gauge
 * Memory_pools_G1_Old_Gen_used_after_gc 0.0
 * # HELP Memory_heap_max The maximum amount of memory in bytes that can be used for memory management
 * # TYPE Memory_heap_max gauge
 * Memory_heap_max 4.294967296E9
 * # HELP Memory_pools_G1_Survivor_Space_used The amount of used memory in bytes
 * # TYPE Memory_pools_G1_Survivor_Space_used gauge
 * Memory_pools_G1_Survivor_Space_used 0.0
 * # HELP Memory_pools_CodeHeap__non_profiled_nmethods__committed The amount of memory in bytes that is committed for the Java virtual machine to use
 * # TYPE Memory_pools_CodeHeap__non_profiled_nmethods__committed gauge
 * Memory_pools_CodeHeap__non_profiled_nmethods__committed 2555904.0
 * # HELP Memory_pools_G1_Old_Gen_used The amount of used memory in bytes
 * # TYPE Memory_pools_G1_Old_Gen_used gauge
 * Memory_pools_G1_Old_Gen_used 0.0
 * # HELP Memory_non_heap_committed The amount of memory in bytes that is committed for the Java virtual machine to use
 * # TYPE Memory_non_heap_committed gauge
 * Memory_non_heap_committed 1.8415616E7
 * # HELP Memory_pools_Metaspace_init The amount of memory in bytes that the Java virtual machine initially requests from the operating system for memory management
 * # TYPE Memory_pools_Metaspace_init gauge
 * Memory_pools_Metaspace_init 0.0
 * # HELP Memory_pools_CodeHeap__profiled_nmethods__used The amount of used memory in bytes
 * # TYPE Memory_pools_CodeHeap__profiled_nmethods__used gauge
 * Memory_pools_CodeHeap__profiled_nmethods__used 934400.0
 * </pre>
 */
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
