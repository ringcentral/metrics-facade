package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;

import java.lang.management.ClassLoadingMXBean;

import static java.lang.management.ManagementFactory.getClassLoadingMXBean;
import static java.util.Objects.requireNonNull;

/**
 * Produces
 * <ul>
 *     <li>loaded - the number of classes that are currently loaded in the Java virtual machine.</li>
 *     <li>loaded.total - the total number of classes that have been loaded since the Java virtual machine has started execution.</li>
 *     <li>unloaded.total - the total number of classes unloaded since the Java virtual machine has started execution.</li>
 * </ul>
 * All metrics have a name prefix. By default it is 'Classes'.<br>
 *<br>
 * Example of usage:
 * <pre>
 *MetricRegistry registry = new DefaultMetricRegistry();
 *new ClassesMetricsProducer().produceMetrics(registry);
 *PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);
 *System.out.println(exporter.exportMetrics());
 * </pre>
 * Corresponding output:
 * <pre>
 *# HELP Classes_loaded The number of classes that are currently loaded in the Java virtual machine
 *# TYPE Classes_loaded gauge
 *Classes_loaded 1167.0
 *# HELP Classes_loaded_total The total number of classes that have been loaded since the Java virtual machine has started execution
 *# TYPE Classes_loaded_total gauge
 *Classes_loaded_total 1176.0
 *# HELP Classes_unloaded_total The total number of classes unloaded since the Java virtual machine has started execution
 *# TYPE Classes_unloaded_total gauge
 *Classes_unloaded_total 0.0
 * </pre>
 */
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
                longVarConfigBuilderSupplier("The number of classes that are currently loaded in the Java virtual machine"));

        registry.longVar(
                nameWithSuffix("loaded", "total"),
                classLoadingMxBean::getTotalLoadedClassCount,
                longVarConfigBuilderSupplier("The total number of classes that have been loaded since the Java virtual machine has started execution"));

        registry.longVar(
                nameWithSuffix("unloaded", "total"),
                classLoadingMxBean::getUnloadedClassCount,
                longVarConfigBuilderSupplier("The total number of classes unloaded since the Java virtual machine has started execution"));
    }
}
