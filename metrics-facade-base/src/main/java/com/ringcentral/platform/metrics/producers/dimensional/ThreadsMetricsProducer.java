package com.ringcentral.platform.metrics.producers.dimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractMetricsProducer;
import com.ringcentral.platform.metrics.producers.DeadlockInfoProvider;
import com.ringcentral.platform.metrics.var.Var;
import com.ringcentral.platform.metrics.var.longVar.LongVar;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Locale;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.dimensionValues;
import static com.ringcentral.platform.metrics.var.longVar.configs.builders.LongVarConfigBuilder.withLongVar;
import static com.ringcentral.platform.metrics.var.objectVar.configs.builders.ObjectVarConfigBuilder.withObjectVar;
import static java.lang.management.ManagementFactory.getThreadMXBean;
import static java.util.Objects.requireNonNull;

public class ThreadsMetricsProducer extends AbstractMetricsProducer {
    private final static MetricDimension STATE_DIMENSION = new MetricDimension("state");

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
        final var stateCount = registry.longVar(
                nameWithSuffix("state", "count"),
                Var.noTotal(),
                // TODO use longVarConfigBuilderSupplier
                () -> withLongVar()
                        .description("The current number of threads in the corresponding state")
                        .dimensions(STATE_DIMENSION)
        );

        for (Thread.State state : Thread.State.values()) {
            final var stateName = state.toString().toLowerCase(Locale.ENGLISH);
            final var dimensionValues = dimensionValues(STATE_DIMENSION.value(stateName));
            stateCount.register(() -> (long) threadCountFor(state), dimensionValues);
        }

        registry.longVar(
                nameWithSuffix("count"),
                () -> (long) threadMxBean.getThreadCount(),
                // TODO use longVarConfigBuilderSupplier
                () -> withLongVar().description("The current number of live threads including both daemon and non-daemon threads")
        );
        registry.longVar(
                nameWithSuffix("daemon", "count"),
                () -> (long) threadMxBean.getDaemonThreadCount(),
                // TODO use longVarConfigBuilderSupplier
                () -> withLongVar().description("The current number of live daemon threads")
        );
        registry.longVar(
                nameWithSuffix("peak", "count"),
                () -> (long) threadMxBean.getPeakThreadCount(),
                // TODO use longVarConfigBuilderSupplier
                () -> withLongVar().description("The peak live thread count since the Java virtual machine started or peak was reset")
        );
        registry.longVar(
                nameWithSuffix("totalStarted", "count"),
                threadMxBean::getTotalStartedThreadCount,
                () -> withLongVar().description("The total number of threads created and also started since the Java virtual machine started")
        );
        registry.longVar(
                nameWithSuffix("deadlock", "count"),
                () -> (long) deadlockInfoProvider.deadlockedThreadTextInfos().size(),
                // TODO use longVarConfigBuilderSupplier
                () -> withLongVar().description("The current number of threads that are in deadlock waiting to acquire object monitors or ownable synchronizers")
        );
        registry.objectVar(
                nameWithSuffix("deadlocks"),
                deadlockInfoProvider::deadlockedThreadTextInfos,
                // TODO use longVarConfigBuilderSupplier
                () -> withObjectVar().description("Deadlocks' descriptions")
        );
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
