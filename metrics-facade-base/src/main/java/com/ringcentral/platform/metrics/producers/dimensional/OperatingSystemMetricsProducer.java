package com.ringcentral.platform.metrics.producers.dimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractMetricsProducer;
import com.ringcentral.platform.metrics.var.Var;
import com.sun.management.OperatingSystemMXBean;
import com.sun.management.UnixOperatingSystemMXBean;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.dimensionValues;
import static com.ringcentral.platform.metrics.var.doubleVar.configs.builders.DoubleVarConfigBuilder.withDoubleVar;
import static com.ringcentral.platform.metrics.var.longVar.configs.builders.LongVarConfigBuilder.withLongVar;
import static java.lang.management.ManagementFactory.getOperatingSystemMXBean;
import static java.util.Objects.requireNonNull;

public class OperatingSystemMetricsProducer extends AbstractMetricsProducer {
    // TODO change to lower case

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("OperatingSystem");
    private final OperatingSystemMXBean osMxBean;

    public OperatingSystemMetricsProducer() {
        this(DEFAULT_NAME_PREFIX);
    }

    public OperatingSystemMetricsProducer(MetricName namePrefix) {
        this(namePrefix, null);
    }

    public OperatingSystemMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(namePrefix, metricModBuilder, (OperatingSystemMXBean) getOperatingSystemMXBean());
    }

    public OperatingSystemMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder, OperatingSystemMXBean osMxBean) {
        super(namePrefix, metricModBuilder);
        this.osMxBean = requireNonNull(osMxBean);
    }

    // TODO move to common class?
    private final static MetricDimension TYPE_DIMENSION = new MetricDimension("type");


    @Override
    public void produceMetrics(MetricRegistry registry) {
        registry.stringVar(nameWithSuffix("arch"), osMxBean::getArch, stringVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("available", "processors", "total"), () -> (long) osMxBean.getAvailableProcessors(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("committed", "virtual", "memory", "size", "bytes"), osMxBean::getCommittedVirtualMemorySize, longVarConfigBuilderSupplier());

        registry.stringVar(nameWithSuffix("name"), osMxBean::getName, stringVarConfigBuilderSupplier());

        final var cpuLoad = registry.doubleVar(
                nameWithSuffix("cpu", "load", "ratio"),
                Var.noTotal(),
                // TODO use longVarConfigBuilderSupplier
                () -> withDoubleVar()
                        .description("cpu load")
                        .dimensions(TYPE_DIMENSION)
        );
        cpuLoad.register(osMxBean::getProcessCpuLoad, dimensionValues(TYPE_DIMENSION.value("process")));
        cpuLoad.register(osMxBean::getSystemCpuLoad, dimensionValues(TYPE_DIMENSION.value("system")));

        registry.doubleVar(
                nameWithSuffix("system", "load", "average"),
                osMxBean::getSystemLoadAverage, doubleVarConfigBuilderSupplier());

        registry.longVar(nameWithSuffix("process", "cpu", "time", "nanos"), osMxBean::getProcessCpuTime, longVarConfigBuilderSupplier());

        final var physicalMemorySize = registry.longVar(
                nameWithSuffix("physical", "memory", "size", "bytes"),
                Var.noTotal(),
                // TODO use longVarConfigBuilderSupplier
                () -> withLongVar()
                        .description("physical memory size")
                        .dimensions(TYPE_DIMENSION)
        );
        physicalMemorySize.register(osMxBean::getTotalPhysicalMemorySize, dimensionValues(TYPE_DIMENSION.value("total")));
        physicalMemorySize.register(osMxBean::getFreePhysicalMemorySize, dimensionValues(TYPE_DIMENSION.value("free")));

        final var swapSpaceSize = registry.longVar(
                nameWithSuffix("swap", "space", "size", "bytes"),
                Var.noTotal(),
                // TODO use longVarConfigBuilderSupplier
                () -> withLongVar()
                        .description("swap space size")
                        .dimensions(TYPE_DIMENSION)
        );
        swapSpaceSize.register(osMxBean::getTotalSwapSpaceSize, dimensionValues(TYPE_DIMENSION.value("total")));
        swapSpaceSize.register(osMxBean::getFreeSwapSpaceSize, dimensionValues(TYPE_DIMENSION.value("free")));


        if (osMxBean instanceof UnixOperatingSystemMXBean) {
            final UnixOperatingSystemMXBean unixMbean = (UnixOperatingSystemMXBean) osMxBean;
            registry.longVar(nameWithSuffix("descriptor", "file", "open", "total"), unixMbean::getOpenFileDescriptorCount, longVarConfigBuilderSupplier());
            registry.longVar(nameWithSuffix("descriptor", "file", "limit", "total"), unixMbean::getMaxFileDescriptorCount, longVarConfigBuilderSupplier());
            registry.doubleVar(nameWithSuffix("descriptor", "file", "usage", "ratio"), () -> {
                long openedDescriptors = unixMbean.getOpenFileDescriptorCount();
                long limit = unixMbean.getMaxFileDescriptorCount();
                return (double) openedDescriptors / limit;
            }, doubleVarConfigBuilderSupplier());
        }
    }
}
