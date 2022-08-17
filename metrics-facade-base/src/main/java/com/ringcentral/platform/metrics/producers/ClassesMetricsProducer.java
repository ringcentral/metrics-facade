package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;

import java.lang.management.ClassLoadingMXBean;

import static java.lang.management.ManagementFactory.getClassLoadingMXBean;
import static java.util.Objects.requireNonNull;

public class ClassesMetricsProducer extends AbstractMetricsProducer {

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("Classes");
    private final ClassLoadingMXBean classLoadingMxBean;

    public ClassesMetricsProducer() {
        this(DEFAULT_NAME_PREFIX);
    }

    public ClassesMetricsProducer(MetricName namePrefix) {
        this(namePrefix, null);
    }

    public ClassesMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(namePrefix, metricModBuilder, getClassLoadingMXBean());
    }

    public ClassesMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        ClassLoadingMXBean classLoadingMxBean) {

        super(namePrefix, metricModBuilder);
        this.classLoadingMxBean = requireNonNull(classLoadingMxBean);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        registry.longVar(
                nameWithSuffix("loaded"),
                () -> (long) classLoadingMxBean.getLoadedClassCount(),
                longVarConfigBuilderSupplier("The number of classes that are currently loaded in the Java virtual machine")
        );
        registry.longVar(
                nameWithSuffix("loaded", "total"),
                classLoadingMxBean::getTotalLoadedClassCount,
                longVarConfigBuilderSupplier("The total number of classes that have been loaded since the Java virtual machine has started execution")

        );
        registry.longVar(
                nameWithSuffix("unloaded", "total"),
                classLoadingMxBean::getUnloadedClassCount,
                longVarConfigBuilderSupplier("The total number of classes unloaded since the Java virtual machine has started execution")
        );
    }
}
