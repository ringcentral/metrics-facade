package com.ringcentral.platform.metrics.producers.nondimensional;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractOperatingSystemMetricsProducer;
import com.sun.management.OperatingSystemMXBean;

import static java.lang.management.ManagementFactory.*;
import static java.util.Objects.requireNonNull;

/**
 * Produces<br>
 * <ul>
 *     <li>
 *         <i>{type}swapSpaceSize</i> - the amount of swap memory in bytes.<br>
 *         type = {"free", "total"}<br>
 *     </li>
 *     <li>
 *         <i>{type}physicalMemorySize</i> - the amount of physical memory in bytes.<br>
 *         type = {"free", "total"}<br>
 *     </li>
 *     <li>
 *         <i>{type}cpuLoad</i> - the 'recent cpu usage' for the process of corresponding type.<br>
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
 * new DefaultOperatingSystemMetricsProducer().produceMetrics(registry);
 * PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);
 * System.out.println(exporter.exportMetrics());
 * </pre>
 * Corresponding output:
 * <pre>
 * # HELP OperatingSystem_freeSwapSpaceSize The amount of physical memory in bytes
 * # TYPE OperatingSystem_freeSwapSpaceSize gauge
 * OperatingSystem_freeSwapSpaceSize 8.59570176E8
 * # HELP OperatingSystem_descriptor_file_open_total The number of open file descriptors
 * # TYPE OperatingSystem_descriptor_file_open_total gauge
 * OperatingSystem_descriptor_file_open_total 25.0
 * # HELP OperatingSystem_systemLoadAverage The system load average for the last minute
 * # TYPE OperatingSystem_systemLoadAverage gauge
 * OperatingSystem_systemLoadAverage 2.2861328125
 * # HELP OperatingSystem_descriptor_file_limit_total The maximum number of file descriptors
 * # TYPE OperatingSystem_descriptor_file_limit_total gauge
 * OperatingSystem_descriptor_file_limit_total 10240.0
 * # HELP OperatingSystem_freePhysicalMemorySize The amount of physical memory in bytes
 * # TYPE OperatingSystem_freePhysicalMemorySize gauge
 * OperatingSystem_freePhysicalMemorySize 4.33627136E8
 * # HELP OperatingSystem_descriptor_file_usage_ratio The number of open file descriptors divided by the maximum number of file descriptors
 * # TYPE OperatingSystem_descriptor_file_usage_ratio gauge
 * OperatingSystem_descriptor_file_usage_ratio 0.00244140625
 * # HELP OperatingSystem_processCpuTime The CPU time used by the process on which the Java virtual machine is running in nanoseconds
 * # TYPE OperatingSystem_processCpuTime gauge
 * OperatingSystem_processCpuTime 4.63614E8
 * # HELP OperatingSystem_systemCpuLoad The 'recent cpu usage' for the process of corresponding type
 * # TYPE OperatingSystem_systemCpuLoad gauge
 * OperatingSystem_systemCpuLoad 0.0
 * # HELP OperatingSystem_totalSwapSpaceSize The amount of swap memory in bytes
 * # TYPE OperatingSystem_totalSwapSpaceSize gauge
 * OperatingSystem_totalSwapSpaceSize 2.147483648E9
 * # HELP OperatingSystem_processCpuLoad The 'recent cpu usage' for the process of corresponding type
 * # TYPE OperatingSystem_processCpuLoad gauge
 * OperatingSystem_processCpuLoad 0.0
 * # HELP OperatingSystem_committedVirtualMemorySize The amount of virtual memory that is guaranteed to be available to the running process in bytes
 * # TYPE OperatingSystem_committedVirtualMemorySize gauge
 * OperatingSystem_committedVirtualMemorySize 1.0527133696E10
 * # HELP OperatingSystem_totalPhysicalMemorySize The amount of swap memory in bytes
 * # TYPE OperatingSystem_totalPhysicalMemorySize gauge
 * OperatingSystem_totalPhysicalMemorySize 1.7179869184E10
 * # HELP OperatingSystem_availableProcessors The number of processors available to the Java virtual machine
 * # TYPE OperatingSystem_availableProcessors gauge
 * OperatingSystem_availableProcessors 12.0
 * </pre>
 */
public class DefaultOperatingSystemMetricsProducer extends AbstractOperatingSystemMetricsProducer {

    public DefaultOperatingSystemMetricsProducer() {
        this(DEFAULT_NAME_PREFIX);
    }

    public DefaultOperatingSystemMetricsProducer(MetricName namePrefix) {
        this(namePrefix, null);
    }

    public DefaultOperatingSystemMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(namePrefix, metricModBuilder, (OperatingSystemMXBean)getOperatingSystemMXBean());
    }

    public DefaultOperatingSystemMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder, OperatingSystemMXBean osMxBean) {
        super(namePrefix, metricModBuilder, osMxBean);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        requireNonNull(registry);
        produceNonDimensional(registry);

        registry.longVar(
                nameWithSuffix("freePhysicalMemorySize"),
                osMxBean::getFreePhysicalMemorySize,
                longVarConfigBuilderSupplier(AMOUNT_OF_PHYSICAL_MEMORY_IN_BYTES_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("freeSwapSpaceSize"),
                osMxBean::getFreeSwapSpaceSize,
                longVarConfigBuilderSupplier(AMOUNT_OF_PHYSICAL_MEMORY_IN_BYTES_DESCRIPTION));

        registry.doubleVar(
                nameWithSuffix("processCpuLoad"),
                osMxBean::getProcessCpuLoad,
                doubleVarConfigBuilderSupplier(CPU_USAGE_DESCRIPTION));

        registry.doubleVar(
                nameWithSuffix("systemCpuLoad"),
                osMxBean::getSystemCpuLoad,
                doubleVarConfigBuilderSupplier(CPU_USAGE_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("totalPhysicalMemorySize"),
                osMxBean::getTotalPhysicalMemorySize,
                longVarConfigBuilderSupplier(AMOUNT_OF_SWAP_MEMORY_IN_BYTES_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("totalSwapSpaceSize"),
                osMxBean::getTotalSwapSpaceSize,
                longVarConfigBuilderSupplier(AMOUNT_OF_SWAP_MEMORY_IN_BYTES_DESCRIPTION));
    }
}
