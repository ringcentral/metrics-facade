package com.ringcentral.platform.metrics.producers.dimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractThreadsMetricsProducer;
import com.ringcentral.platform.metrics.producers.DeadlockInfoProvider;
import com.ringcentral.platform.metrics.var.Var;

import java.lang.management.ThreadMXBean;
import java.util.Locale;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.dimensionValues;

public class DimensionalThreadsMetricsProducer extends AbstractThreadsMetricsProducer {
    private final static MetricDimension STATE_DIMENSION = new MetricDimension("state");

    public DimensionalThreadsMetricsProducer() {
        super();
    }

    public DimensionalThreadsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        super(namePrefix, metricModBuilder);
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
        final var stateCount = registry.longVar(
                nameWithSuffix("state", "count"),
                Var.noTotal(), longVarConfigBuilderSupplier(STATE_COUNT_DESCRIPTION, STATE_DIMENSION)
        );

        for (Thread.State state : Thread.State.values()) {
            final var stateName = state.toString().toLowerCase(Locale.ENGLISH);
            final var dimensionValues = dimensionValues(STATE_DIMENSION.value(stateName));
            stateCount.register(() -> (long) threadCountFor(state), dimensionValues);
        }
        registry.longVar(
                nameWithSuffix("count"),
                () -> (long) threadMxBean.getThreadCount(),
                longVarConfigBuilderSupplier(LIVE_COUNT_DESCRIPTION)
        );
        registry.longVar(
                nameWithSuffix("daemon", "count"),
                () -> (long) threadMxBean.getDaemonThreadCount(),
                longVarConfigBuilderSupplier(LIVE_DAEMON_COUNT_DESCRIPTION)
        );
        registry.longVar(
                nameWithSuffix("peak", "count"),
                () -> (long) threadMxBean.getPeakThreadCount(),
                longVarConfigBuilderSupplier(PEAK_THREAD_COUNT_DESCRIPTION)
        );
        registry.longVar(
                nameWithSuffix("totalStarted", "count"),
                threadMxBean::getTotalStartedThreadCount,
                longVarConfigBuilderSupplier(TOTAL_STARTED_COUNT_DESCRIPTION)
        );
        registry.longVar(
                nameWithSuffix("deadlock", "count"),
                () -> (long) deadlockInfoProvider.deadlockedThreadTextInfos().size(),
                longVarConfigBuilderSupplier(DEADLOCK_COUNT_DESCRIPTION)
        );

        registry.objectVar(
                nameWithSuffix("deadlocks"),
                deadlockInfoProvider::deadlockedThreadTextInfos,
                objectVarConfigBuilderSupplier("Deadlocks' descriptions")
        );
    }

}
