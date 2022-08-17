package com.ringcentral.platform.metrics.producers.nondimensional;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractOperatingSystemMetricsProducer;
import com.sun.management.OperatingSystemMXBean;

import static java.lang.management.ManagementFactory.*;
import static java.util.Objects.requireNonNull;

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

        registry.longVar(nameWithSuffix("freePhysicalMemorySize"), osMxBean::getFreePhysicalMemorySize, longVarConfigBuilderSupplier(AMOUNT_OF_PHYSICAL_MEMORY_IN_BYTES_DESCRIPTION));
        registry.longVar(nameWithSuffix("freeSwapSpaceSize"), osMxBean::getFreeSwapSpaceSize, longVarConfigBuilderSupplier(AMOUNT_OF_PHYSICAL_MEMORY_IN_BYTES_DESCRIPTION));
        registry.doubleVar(nameWithSuffix("processCpuLoad"), osMxBean::getProcessCpuLoad, doubleVarConfigBuilderSupplier(CPU_USAGE_DESCRIPTION));
        registry.doubleVar(nameWithSuffix("systemCpuLoad"), osMxBean::getSystemCpuLoad, doubleVarConfigBuilderSupplier(CPU_USAGE_DESCRIPTION));
        registry.longVar(nameWithSuffix("totalPhysicalMemorySize"), osMxBean::getTotalPhysicalMemorySize, longVarConfigBuilderSupplier(AMOUNT_OF_SWAP_MEMORY_IN_BYTES_DESCRIPTION));
        registry.longVar(nameWithSuffix("totalSwapSpaceSize"), osMxBean::getTotalSwapSpaceSize, longVarConfigBuilderSupplier(AMOUNT_OF_SWAP_MEMORY_IN_BYTES_DESCRIPTION));

    }
}
