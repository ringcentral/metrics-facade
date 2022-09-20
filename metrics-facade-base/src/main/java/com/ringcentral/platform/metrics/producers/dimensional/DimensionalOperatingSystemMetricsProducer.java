package com.ringcentral.platform.metrics.producers.dimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractOperatingSystemMetricsProducer;
import com.ringcentral.platform.metrics.var.Var;
import com.sun.management.OperatingSystemMXBean;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.dimensionValues;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;
import static java.util.Objects.requireNonNull;

/**
 * Produces<br>
 * <ul>
 *     <li>
 *         <i>swapSpaceSize</i> - the amount of swap memory in bytes.<br>
 *         Dimensions:<br>
 *         type = {"free", "total"}<br>
 *     </li>
 *     <li>
 *         <i>physicalMemorySize</i> - the amount of physical memory in bytes.<br>
 *         Dimensions:<br>
 *         type = {"free", "total"}<br>
 *     </li>
 *     <li>
 *         <i>cpuLoad</i> - the 'recent cpu usage' for the process of corresponding type.<br>
 *         Dimensions:<br>
 *         type = {"system", "process"}<br>
 *     </li>
 *     <li><i>systemLoadAverage</i> - the system load average for the last minute.<br></li>
 *     <li><i>processCpuTime</i> - the CPU time used by the process on which the Java virtual machine is running in nanoseconds.<br></li>
 *     <li><i>committedVirtualMemorySize</i> - the amount of virtual memory that is guaranteed to be available to the running process in bytes.<br></li>
 *     <li><i>availableProcessors</i> - the number of processors available to the Java virtual machine.<br></li>
 *     <li><i>descriptor.file.open.total</i> - the number of open file descriptors.<br></li>
 *     <li><i>descriptor.file.limit.total</i> - the maximum number of file descriptors.<br></li>
 *     <li><i>descriptor.file.usage.ratio</i> - the number of open file descriptors divided by the maximum number of file descriptors.<br></li>
 * </ul>
 *
 * All metrics have a name prefix. By default it is 'OperatingSystem'.<br>
 * <br>
 * Example of usage:
 * <pre>
 * MetricRegistry registry = new DefaultMetricRegistry();
 * new DimensionalOperatingSystemMetricsProducer().produceMetrics(registry);
 * PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);
 * System.out.println(exporter.exportMetrics());
 * </pre>
 * Corresponding output:
 * <pre>
 * # HELP OperatingSystem_swapSpaceSize The amount of swap memory in bytes
 * # TYPE OperatingSystem_swapSpaceSize gauge
 * OperatingSystem_swapSpaceSize{type="free",} 8.59570176E8
 * OperatingSystem_swapSpaceSize{type="total",} 2.147483648E9
 * # HELP OperatingSystem_descriptor_file_open_total The number of open file descriptors
 * # TYPE OperatingSystem_descriptor_file_open_total gauge
 * OperatingSystem_descriptor_file_open_total 25.0
 * # HELP OperatingSystem_physicalMemorySize The amount of physical memory in bytes
 * # TYPE OperatingSystem_physicalMemorySize gauge
 * OperatingSystem_physicalMemorySize{type="free",} 3.9356416E8
 * OperatingSystem_physicalMemorySize{type="total",} 1.7179869184E10
 * # HELP OperatingSystem_systemLoadAverage The system load average for the last minute
 * # TYPE OperatingSystem_systemLoadAverage gauge
 * OperatingSystem_systemLoadAverage 2.8427734375
 * # HELP OperatingSystem_cpuLoad The 'recent cpu usage' for the process of corresponding type
 * # TYPE OperatingSystem_cpuLoad gauge
 * OperatingSystem_cpuLoad{type="system",} 0.0
 * OperatingSystem_cpuLoad{type="process",} 0.0
 * # HELP OperatingSystem_descriptor_file_limit_total The maximum number of file descriptors
 * # TYPE OperatingSystem_descriptor_file_limit_total gauge
 * OperatingSystem_descriptor_file_limit_total 10240.0
 * # HELP OperatingSystem_descriptor_file_usage_ratio The number of open file descriptors divided by the maximum number of file descriptors
 * # TYPE OperatingSystem_descriptor_file_usage_ratio gauge
 * OperatingSystem_descriptor_file_usage_ratio 0.00244140625
 * # HELP OperatingSystem_processCpuTime The CPU time used by the process on which the Java virtual machine is running in nanoseconds
 * # TYPE OperatingSystem_processCpuTime gauge
 * OperatingSystem_processCpuTime 5.24424E8
 * # HELP OperatingSystem_committedVirtualMemorySize The amount of virtual memory that is guaranteed to be available to the running process in bytes
 * # TYPE OperatingSystem_committedVirtualMemorySize gauge
 * OperatingSystem_committedVirtualMemorySize 1.0547204096E10
 * # HELP OperatingSystem_availableProcessors The number of processors available to the Java virtual machine
 * # TYPE OperatingSystem_availableProcessors gauge
 * OperatingSystem_availableProcessors 12.0
 * </pre>
 */
public class DimensionalOperatingSystemMetricsProducer extends AbstractOperatingSystemMetricsProducer {

    private static final MetricDimension TYPE_DIMENSION = new MetricDimension("type");
    private static final MetricDimensionValues TOTAL_TYPE_DIMENSION_VALUES = dimensionValues(TYPE_DIMENSION.value("total"));
    private static final MetricDimensionValues FREE_TYPE_DIMENSION_VALUES = dimensionValues(TYPE_DIMENSION.value("free"));

    public DimensionalOperatingSystemMetricsProducer() {
        this(DEFAULT_NAME_PREFIX);
    }

    public DimensionalOperatingSystemMetricsProducer(MetricName namePrefix) {
        this(namePrefix, null);
    }

    public DimensionalOperatingSystemMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(namePrefix, metricModBuilder, (OperatingSystemMXBean) getOperatingSystemMXBean());
    }

    public DimensionalOperatingSystemMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder, OperatingSystemMXBean osMxBean) {
        super(namePrefix, metricModBuilder, osMxBean);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        requireNonNull(registry);
        produceNonDimensional(registry);

        final var cpuLoad = registry.doubleVar(
                nameWithSuffix("cpuLoad"),
                Var.noTotal(),
                doubleVarConfigBuilderSupplier(CPU_USAGE_DESCRIPTION, TYPE_DIMENSION)
        );
        cpuLoad.register(osMxBean::getProcessCpuLoad, dimensionValues(TYPE_DIMENSION.value("process")));
        cpuLoad.register(osMxBean::getSystemCpuLoad, dimensionValues(TYPE_DIMENSION.value("system")));

        final var physicalMemorySize = registry.longVar(
                nameWithSuffix("physicalMemorySize"),
                Var.noTotal(),
                longVarConfigBuilderSupplier(AMOUNT_OF_PHYSICAL_MEMORY_IN_BYTES_DESCRIPTION, TYPE_DIMENSION)
        );
        physicalMemorySize.register(osMxBean::getTotalPhysicalMemorySize, TOTAL_TYPE_DIMENSION_VALUES);
        physicalMemorySize.register(osMxBean::getFreePhysicalMemorySize, FREE_TYPE_DIMENSION_VALUES);

        final var swapSpaceSize = registry.longVar(
                nameWithSuffix("swapSpaceSize"),
                Var.noTotal(),
                longVarConfigBuilderSupplier(AMOUNT_OF_SWAP_MEMORY_IN_BYTES_DESCRIPTION, TYPE_DIMENSION)
        );
        swapSpaceSize.register(osMxBean::getTotalSwapSpaceSize, TOTAL_TYPE_DIMENSION_VALUES);
        swapSpaceSize.register(osMxBean::getFreeSwapSpaceSize, FREE_TYPE_DIMENSION_VALUES);
    }
}
