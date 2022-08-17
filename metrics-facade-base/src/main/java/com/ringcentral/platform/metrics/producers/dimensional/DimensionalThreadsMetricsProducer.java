package com.ringcentral.platform.metrics.producers.dimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractThreadsMetricsProducer;
import com.ringcentral.platform.metrics.producers.DeadlockInfoProvider;
import com.ringcentral.platform.metrics.var.Var;

import java.lang.management.ThreadMXBean;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionUtils.STATE_DIMENSION;
import static java.lang.management.ManagementFactory.getThreadMXBean;
import static java.util.Objects.requireNonNull;

public class DimensionalThreadsMetricsProducer extends AbstractThreadsMetricsProducer {

    public DimensionalThreadsMetricsProducer() {
        this(DEFAULT_NAME_PREFIX, null);
    }

    public DimensionalThreadsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(namePrefix,
                metricModBuilder,
                getThreadMXBean(),
                new DeadlockInfoProvider(getThreadMXBean()));
    }

    public DimensionalThreadsMetricsProducer(
            MetricName namePrefix,
            MetricModBuilder metricModBuilder,
            ThreadMXBean threadMxBean,
            DeadlockInfoProvider deadlockInfoProvider) {
        super(namePrefix, metricModBuilder, threadMxBean, deadlockInfoProvider);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        requireNonNull(registry);
        produceNonDimensional(registry);
        final var stateCount = registry.longVar(
                nameWithSuffix("state", "count"),
                Var.noTotal(), longVarConfigBuilderSupplier(STATE_COUNT_DESCRIPTION, STATE_DIMENSION)
        );
    }

}
