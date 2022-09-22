package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;

import java.lang.management.RuntimeMXBean;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.util.Objects.requireNonNull;

/**
 * Produces
 * <ul>
 *     <li>startTime - the start time of the Java virtual machine in milliseconds.</li>
 *     <li>uptime.ms - the uptime of the Java virtual machine in milliseconds.</li>
 * </ul>
 * All metrics have a name prefix. By default it is 'Runtime'.<br>
 *<br>
 * Example of usage:
 * <pre>
 *MetricRegistry registry = new DefaultMetricRegistry();
 *new RuntimeMetricsProducer().produceMetrics(registry);
 *PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);
 *System.out.println(exporter.exportMetrics());
 * </pre>
 * Corresponding output:
 * <pre>
 *# HELP Runtime_startTime The start time of the Java virtual machine in milliseconds
 *# TYPE Runtime_startTime gauge
 *Runtime_startTime 1.66249656241E12
 *# HELP Runtime_uptime_ms The uptime of the Java virtual machine in milliseconds
 *# TYPE Runtime_uptime_ms gauge
 *Runtime_uptime_ms 302.0
 * </pre>
 */
public class RuntimeMetricsProducer extends AbstractMetricsProducer {

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("Runtime");

    private final RuntimeMXBean runtimeMxBean;

    public RuntimeMetricsProducer() {
        this(DEFAULT_NAME_PREFIX);
    }

    public RuntimeMetricsProducer(MetricName namePrefix) {
        this(namePrefix, null);
    }

    public RuntimeMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(namePrefix, metricModBuilder, getRuntimeMXBean());
    }

    public RuntimeMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder, RuntimeMXBean runtimeMxBean) {
        super(namePrefix, metricModBuilder);
        this.runtimeMxBean = requireNonNull(runtimeMxBean);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        registry.longVar(
                nameWithSuffix("startTime"),
                runtimeMxBean::getStartTime,
                longVarConfigBuilderSupplier("The start time of the Java virtual machine in milliseconds"));

        registry.longVar(
                nameWithSuffix("uptime", "ms"),
                runtimeMxBean::getUptime,
                longVarConfigBuilderSupplier("The uptime of the Java virtual machine in milliseconds"));
    }
}
