package com.ringcentral.platform.metrics.producers.nondimensional;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractMetricsProducer;
import com.ringcentral.platform.metrics.producers.DeadlockInfoProvider;

import java.lang.management.*;
import java.util.Locale;

import static java.lang.management.ManagementFactory.getThreadMXBean;
import static java.util.Objects.requireNonNull;

public class ThreadsMetricsProducer extends AbstractMetricsProducer {

    public static final MetricName DEFAULT_NAME_PREFIX = MetricName.of("Threads");

    private final ThreadMXBean threadMxBean;
    private final DeadlockInfoProvider deadlockInfoProvider;

    public ThreadsMetricsProducer() {
        this(DEFAULT_NAME_PREFIX, null);
    }

    public ThreadsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(
            namePrefix,
            metricModBuilder,
            getThreadMXBean(),
            new DeadlockInfoProvider(getThreadMXBean()));
    }

    public ThreadsMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        ThreadMXBean threadMxBean,
        DeadlockInfoProvider deadlockInfoProvider) {

        super(namePrefix, metricModBuilder);

        this.threadMxBean = requireNonNull(threadMxBean);
        this.deadlockInfoProvider = requireNonNull(deadlockInfoProvider);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        for (Thread.State state : Thread.State.values()) {
            registry.longVar(
                nameWithSuffix(state.toString().toLowerCase(Locale.ENGLISH), "count"),
                () -> (long)threadCountFor(state), longVarConfigBuilderSupplier());
        }

        registry.longVar(nameWithSuffix("count"), () -> (long)threadMxBean.getThreadCount(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("daemon", "count"), () -> (long)threadMxBean.getDaemonThreadCount(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("peak", "count"), () -> (long)threadMxBean.getPeakThreadCount(), longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("totalStarted", "count"), threadMxBean::getTotalStartedThreadCount, longVarConfigBuilderSupplier());
        registry.longVar(nameWithSuffix("deadlock", "count"), () -> (long)deadlockInfoProvider.deadlockedThreadTextInfos().size(), longVarConfigBuilderSupplier());
        registry.objectVar(nameWithSuffix("deadlocks"), deadlockInfoProvider::deadlockedThreadTextInfos, objectVarConfigBuilderSupplier());
    }

    private int threadCountFor(Thread.State state) {
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
