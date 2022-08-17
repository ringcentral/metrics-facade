package com.ringcentral.platform.metrics.producers.dimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractThreadsMetricsProducer;
import com.ringcentral.platform.metrics.producers.DeadlockInfoProvider;
import com.ringcentral.platform.metrics.var.Var;

import java.lang.management.ThreadMXBean;
import java.util.Locale;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionUtils.STATE_DIMENSION;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.dimensionValues;
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
        for (Thread.State state : Thread.State.values()) {
            final var stateName = state.toString().toLowerCase(Locale.ENGLISH);
            final var dimensionValues = dimensionValues(STATE_DIMENSION.value(stateName));
            stateCount.register(() -> (long) threadCountFor(state), dimensionValues);
        }
    }

}
