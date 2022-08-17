package com.ringcentral.platform.metrics.producers.nondimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractThreadsMetricsProducer;
import com.ringcentral.platform.metrics.producers.DeadlockInfoProvider;

import java.lang.management.ThreadMXBean;
import java.util.Locale;

public class DefaultThreadsMetricsProducer extends AbstractThreadsMetricsProducer {

    public DefaultThreadsMetricsProducer() {
        super();
    }

    public DefaultThreadsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        super(namePrefix, metricModBuilder);
    }

    public DefaultThreadsMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        ThreadMXBean threadMxBean,
        DeadlockInfoProvider deadlockInfoProvider) {

        super(namePrefix, metricModBuilder, threadMxBean, deadlockInfoProvider);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        for (Thread.State state : Thread.State.values()) {
            registry.longVar(
                    nameWithSuffix(state.toString().toLowerCase(Locale.ENGLISH), "count"),
                    () -> (long) threadCountFor(state),
                    longVarConfigBuilderSupplier(STATE_COUNT_DESCRIPTION)
            );
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
                () -> (long)deadlockInfoProvider.deadlockedThreadTextInfos().size(),
                longVarConfigBuilderSupplier(DEADLOCK_COUNT_DESCRIPTION)
        );

        registry.objectVar(
                nameWithSuffix("deadlocks"),
                deadlockInfoProvider::deadlockedThreadTextInfos,
                objectVarConfigBuilderSupplier("Deadlocks' descriptions")
        );
    }


}
