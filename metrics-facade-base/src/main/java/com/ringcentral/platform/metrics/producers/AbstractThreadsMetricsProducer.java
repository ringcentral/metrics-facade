package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import static java.util.Objects.requireNonNull;

public abstract class AbstractThreadsMetricsProducer extends AbstractMetricsProducer implements ThreadsMetricsProducer {

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("Threads");

    protected static final String STATE_COUNT_DESCRIPTION = "The current number of threads in the corresponding state";
    protected static final String LIVE_COUNT_DESCRIPTION = "The current number of live threads including both daemon and non-daemon threads";
    protected static final String LIVE_DAEMON_COUNT_DESCRIPTION = "The current number of live daemon threads";
    protected static final String PEAK_THREAD_COUNT_DESCRIPTION = "The peak live thread count since the Java virtual machine started or peak was reset";
    protected static final String TOTAL_STARTED_COUNT_DESCRIPTION = "The total number of threads created and also started since the Java virtual machine started";
    protected static final String DEADLOCK_COUNT_DESCRIPTION = "The current number of threads that are in deadlock waiting to acquire object monitors or ownable synchronizers";
    protected static final String DEADLOCKS_DETAILS_DESCRIPTION = "Deadlocks' details";

    protected final ThreadMXBean threadMxBean;
    protected final DeadlockInfoProvider deadlockInfoProvider;

    public AbstractThreadsMetricsProducer(
            MetricName namePrefix,
            MetricModBuilder metricModBuilder,
            ThreadMXBean threadMxBean,
            DeadlockInfoProvider deadlockInfoProvider) {

        super(namePrefix, metricModBuilder);

        this.threadMxBean = requireNonNull(threadMxBean);
        this.deadlockInfoProvider = requireNonNull(deadlockInfoProvider);
    }

    protected void produceNonDimensional(MetricRegistry registry) {
        requireNonNull(registry);

        registry.longVar(
                nameWithSuffix("count"),
                () -> (long) threadMxBean.getThreadCount(),
                longVarConfigBuilderSupplier(LIVE_COUNT_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("daemon", "count"),
                () -> (long) threadMxBean.getDaemonThreadCount(),
                longVarConfigBuilderSupplier(LIVE_DAEMON_COUNT_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("peak", "count"),
                () -> (long) threadMxBean.getPeakThreadCount(),
                longVarConfigBuilderSupplier(PEAK_THREAD_COUNT_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("totalStarted", "count"),
                threadMxBean::getTotalStartedThreadCount,
                longVarConfigBuilderSupplier(TOTAL_STARTED_COUNT_DESCRIPTION));

        registry.longVar(
                nameWithSuffix("deadlock", "count"),
                () -> (long)deadlockInfoProvider.deadlockedThreadTextInfos().size(),
                longVarConfigBuilderSupplier(DEADLOCK_COUNT_DESCRIPTION));

        registry.objectVar(
                nameWithSuffix("deadlocks"),
                deadlockInfoProvider::deadlockedThreadTextInfos,
                objectVarConfigBuilderSupplier(DEADLOCKS_DETAILS_DESCRIPTION));
    }

    protected int threadCountFor(Thread.State state) {
        ThreadInfo[] infos = threadMxBean.getThreadInfo(threadMxBean.getAllThreadIds(), 0);
        int count = 0;

        for (ThreadInfo info : infos) {
            if (info != null && info.getThreadState() == state) {
                ++count;
            }
        }

        return count;
    }
}
