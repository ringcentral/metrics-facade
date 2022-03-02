package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.*;
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
        registry.longVar(nameWithSuffix("loaded"), () -> (long)classLoadingMxBean.getLoadedClassCount(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("loaded", "total"), classLoadingMxBean::getTotalLoadedClassCount, longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("unloaded", "total"), classLoadingMxBean::getUnloadedClassCount, longVarConfigBuilderSupplier());
    }
}
