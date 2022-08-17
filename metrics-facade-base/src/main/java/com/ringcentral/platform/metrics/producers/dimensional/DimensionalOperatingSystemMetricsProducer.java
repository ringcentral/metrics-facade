package com.ringcentral.platform.metrics.producers.dimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractOperatingSystemMetricsProducer;
import com.ringcentral.platform.metrics.var.Var;
import com.sun.management.OperatingSystemMXBean;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.dimensionValues;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;
import static java.util.Objects.requireNonNull;

public class DimensionalOperatingSystemMetricsProducer extends AbstractOperatingSystemMetricsProducer {

    // TODO move to common class?
    private final static MetricDimension TYPE_DIMENSION = new MetricDimension("type");

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
                nameWithSuffix("physicalMemorySize", "bytes"),
                Var.noTotal(),
                longVarConfigBuilderSupplier(AMOUNT_OF_PHYSICAL_MEMORY_IN_BYTES_DESCRIPTION, TYPE_DIMENSION)
        );
        physicalMemorySize.register(osMxBean::getTotalPhysicalMemorySize, dimensionValues(TYPE_DIMENSION.value("total")));
        physicalMemorySize.register(osMxBean::getFreePhysicalMemorySize, dimensionValues(TYPE_DIMENSION.value("free")));

        final var swapSpaceSize = registry.longVar(
                nameWithSuffix("swapSpaceSize", "bytes"),
                Var.noTotal(),
                longVarConfigBuilderSupplier(AMOUNT_OF_SWAP_MEMORY_IN_BYTES_DESCRIPTION, TYPE_DIMENSION)
        );
        swapSpaceSize.register(osMxBean::getTotalSwapSpaceSize, dimensionValues(TYPE_DIMENSION.value("total")));
        swapSpaceSize.register(osMxBean::getFreeSwapSpaceSize, dimensionValues(TYPE_DIMENSION.value("free")));
    }
}
