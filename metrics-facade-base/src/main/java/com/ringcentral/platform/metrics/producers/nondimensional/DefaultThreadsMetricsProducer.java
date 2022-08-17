package com.ringcentral.platform.metrics.producers.nondimensional;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractThreadsMetricsProducer;
import com.ringcentral.platform.metrics.producers.DeadlockInfoProvider;

import java.lang.management.ThreadMXBean;
import java.util.Locale;

import static java.lang.management.ManagementFactory.getThreadMXBean;
import static java.util.Objects.requireNonNull;

public class DefaultThreadsMetricsProducer extends AbstractThreadsMetricsProducer {

    public DefaultThreadsMetricsProducer() {
        this(DEFAULT_NAME_PREFIX, null);
    }

    public DefaultThreadsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(namePrefix,
                metricModBuilder,
                getThreadMXBean(),
                new DeadlockInfoProvider(getThreadMXBean()
                )
        );
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
        requireNonNull(registry);
        produceNonDimensional(registry);

        for (Thread.State state : Thread.State.values()) {
            registry.longVar(
                    nameWithSuffix(state.toString().toLowerCase(Locale.ENGLISH), "count"),
                    () -> (long) threadCountFor(state),
                    longVarConfigBuilderSupplier(STATE_COUNT_DESCRIPTION)
            );
        }
    }
}
