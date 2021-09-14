package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.sun.management.OperatingSystemMXBean;

import static java.lang.management.ManagementFactory.*;
import static java.util.Objects.*;

public class OperatingSystemMetricsProducer extends AbstractMetricsProducer {

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("OperatingSystem");
    private final OperatingSystemMXBean osMxBean;

    public OperatingSystemMetricsProducer() {
        this(DEFAULT_NAME_PREFIX);
    }

    public OperatingSystemMetricsProducer(MetricName namePrefix) {
        this(namePrefix, null);
    }

    public OperatingSystemMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(namePrefix, metricModBuilder, (OperatingSystemMXBean)getOperatingSystemMXBean());
    }

    public OperatingSystemMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder, OperatingSystemMXBean osMxBean) {
        super(namePrefix, metricModBuilder);
        this.osMxBean = requireNonNull(osMxBean);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        registry.stringVar(nameWithSuffix("arch"), osMxBean::getArch, stringVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("availableProcessors"), () -> (long)osMxBean.getAvailableProcessors(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("committedVirtualMemorySize"), osMxBean::getCommittedVirtualMemorySize, longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("freePhysicalMemorySize"), osMxBean::getFreePhysicalMemorySize, longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("freeSwapSpaceSize"), osMxBean::getFreeSwapSpaceSize, longVarConfigBuilderSupplier());
        registry.stringVar(nameWithSuffix("name"), osMxBean::getName, stringVarConfigBuilderSupplier());
        registry.doubleVar(nameWithSuffix("processCpuLoad"), osMxBean::getProcessCpuLoad, doubleVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("processCpuTime"), osMxBean::getProcessCpuTime, longVarConfigBuilderSupplier());
        registry.doubleVar(nameWithSuffix("systemCpuLoad"), osMxBean::getSystemCpuLoad, doubleVarConfigBuilderSupplier());
        registry.doubleVar(nameWithSuffix("systemLoadAverage"), osMxBean::getSystemLoadAverage, doubleVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("totalPhysicalMemorySize"), osMxBean::getTotalPhysicalMemorySize, longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("totalSwapSpaceSize"), osMxBean::getTotalSwapSpaceSize, longVarConfigBuilderSupplier());
    }
}
