package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.names.MetricName;

import java.lang.management.RuntimeMXBean;

import static java.lang.management.ManagementFactory.getRuntimeMXBean;
import static java.util.Objects.requireNonNull;

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
        registry.longVar(nameWithSuffix("startTime"), runtimeMxBean::getStartTime, longVarConfigBuilderSupplier());
    }
}
