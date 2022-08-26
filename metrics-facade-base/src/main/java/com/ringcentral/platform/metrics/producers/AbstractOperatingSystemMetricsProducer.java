package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.sun.management.OperatingSystemMXBean;
import com.sun.management.UnixOperatingSystemMXBean;

import static java.util.Objects.requireNonNull;

public abstract class AbstractOperatingSystemMetricsProducer extends AbstractMetricsProducer implements OperatingSystemMetricsProducer {

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("OperatingSystem");

    private static final String ARCH_DESCRIPTION = "The operating system architecture";
    private static final String AVAILABLE_PROCESSORS_DESCRIPTION = "The number of processors available to the Java virtual machine";
    private static final String COMMITTED_VIRTUAL_MEMORY_SIZE_DESCRIPTION = "The amount of virtual memory that is guaranteed to be available to the running process in bytes";
    private static final String OS_NAME_DESCRIPTION = "The operating system name";
    private static final String SYSTEM_LOAD_AVERAGE_DESCRIPTION = "The system load average for the last minute";
    private static final String PROCESS_CPU_TIME_DESCRIPTION = "The CPU time used by the process on which the Java virtual machine is running in nanoseconds";
    private static final String OPEN_FILE_DESCRIPTOR_COUNT_DESCRIPTION = "The number of open file descriptors";
    private static final String FILE_DESCRIPTOR_LIMIT_DESCRIPTION = "The maximum number of file descriptors";
    private static final String FILE_DESCRIPTOR_USAGE_DESCRIPTION = "The number of open file descriptors divided by the maximum number of file descriptors";
    protected static final String AMOUNT_OF_PHYSICAL_MEMORY_IN_BYTES_DESCRIPTION = "The amount of physical memory in bytes";
    protected static final String AMOUNT_OF_SWAP_MEMORY_IN_BYTES_DESCRIPTION = "The amount of swap memory in bytes";
    protected static final String CPU_USAGE_DESCRIPTION = "The 'recent cpu usage' for the process of corresponding type";

    protected final OperatingSystemMXBean osMxBean;

    public AbstractOperatingSystemMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder, OperatingSystemMXBean osMxBean) {
        super(namePrefix, metricModBuilder);
        this.osMxBean = requireNonNull(osMxBean);
    }

    protected void produceNonDimensional(MetricRegistry registry) {
        requireNonNull(registry);

        registry.stringVar(
                nameWithSuffix("arch"),
                osMxBean::getArch,
                stringVarConfigBuilderSupplier(ARCH_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("availableProcessors"),
                () -> (long) osMxBean.getAvailableProcessors(),
                longVarConfigBuilderSupplier(AVAILABLE_PROCESSORS_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("committedVirtualMemorySize"),
                osMxBean::getCommittedVirtualMemorySize,
                longVarConfigBuilderSupplier(COMMITTED_VIRTUAL_MEMORY_SIZE_DESCRIPTION));

        registry.stringVar(
                nameWithSuffix("name"),
                osMxBean::getName,
                stringVarConfigBuilderSupplier(OS_NAME_DESCRIPTION));

        registry.doubleVar(
                nameWithSuffix("systemLoadAverage"),
                osMxBean::getSystemLoadAverage,
                doubleVarConfigBuilderSupplier(SYSTEM_LOAD_AVERAGE_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("processCpuTime"),
                osMxBean::getProcessCpuTime,
                longVarConfigBuilderSupplier(PROCESS_CPU_TIME_DESCRIPTION));

        if (osMxBean instanceof UnixOperatingSystemMXBean) {
            final UnixOperatingSystemMXBean unixMbean = (UnixOperatingSystemMXBean) osMxBean;
            registry.longVar(
                    nameWithSuffix("descriptor", "file", "open", "total"),
                    unixMbean::getOpenFileDescriptorCount,
                    longVarConfigBuilderSupplier(OPEN_FILE_DESCRIPTOR_COUNT_DESCRIPTION));

            registry.longVar(
                    nameWithSuffix("descriptor", "file", "limit", "total"),
                    unixMbean::getMaxFileDescriptorCount,
                    longVarConfigBuilderSupplier(FILE_DESCRIPTOR_LIMIT_DESCRIPTION));

            registry.doubleVar(nameWithSuffix("descriptor", "file", "usage", "ratio"), () -> {
                long openedDescriptors = unixMbean.getOpenFileDescriptorCount();
                long limit = unixMbean.getMaxFileDescriptorCount();
                return (double)openedDescriptors / limit;
            }, doubleVarConfigBuilderSupplier(FILE_DESCRIPTOR_USAGE_DESCRIPTION));
        }
    }
}
