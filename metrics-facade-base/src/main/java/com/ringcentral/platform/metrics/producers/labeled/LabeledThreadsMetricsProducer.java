package com.ringcentral.platform.metrics.producers.labeled;

import com.ringcentral.platform.metrics.MetricModBuilder;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.producers.AbstractThreadsMetricsProducer;
import com.ringcentral.platform.metrics.producers.DeadlockInfoProvider;
import com.ringcentral.platform.metrics.var.Var;

import java.lang.management.ThreadMXBean;
import java.util.Locale;

import static com.ringcentral.platform.metrics.labels.LabelValues.labelValues;
import static java.lang.management.ManagementFactory.getThreadMXBean;
import static java.util.Objects.requireNonNull;

/**
 * Produces<br>
 * <ul>
 *     <li><i>count</i> - the current number of live threads including both daemon and non-daemon threads.<br></li>
 *     <li><i>totalStarted.count</i> - the total number of threads created and also started since the Java virtual machine started.<br></li>
 *     <li><i>daemon.count</i> - the current number of live daemon threads.<br></li>
 *     <li><i>peak.count</i> - the peak live thread count since the Java virtual machine started or peak was reset.<br></li>
 *     <li><i>deadlock.count</i> - the current number of threads that are in deadlock waiting to acquire object monitors or ownable synchronizers.<br></li>
 *     <li>
 *         <i>state.count</i> - the current number of threads in the corresponding state.<br>
 *         Labels:<br>
 *         state = {"waiting", "runnable", "timed_waiting", "terminated", "new", "blocked"}<br>
 *     </li>
 * </ul>
 *
 * All metrics have a name prefix. By default it is 'Threads'.<br>
 * <br>
 * Example of usage:
 * <pre>
 * MetricRegistry registry = new DefaultMetricRegistry();
 * new LabeledThreadsMetricsProducer().produceMetrics(registry);
 * PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);
 * System.out.println(exporter.exportMetrics());
 * </pre>
 * Corresponding output:
 * <pre>
 * # HELP Threads_count The current number of live threads including both daemon and non-daemon threads
 * # TYPE Threads_count gauge
 * Threads_count 7.0
 * # HELP Threads_totalStarted_count The total number of threads created and also started since the Java virtual machine started
 * # TYPE Threads_totalStarted_count gauge
 * Threads_totalStarted_count 7.0
 * # HELP Threads_daemon_count The current number of live daemon threads
 * # TYPE Threads_daemon_count gauge
 * Threads_daemon_count 6.0
 * # HELP Threads_state_count The current number of threads in the corresponding state
 * # TYPE Threads_state_count gauge
 * Threads_state_count{state="waiting",} 2.0
 * Threads_state_count{state="runnable",} 4.0
 * Threads_state_count{state="timed_waiting",} 1.0
 * Threads_state_count{state="terminated",} 0.0
 * Threads_state_count{state="new",} 0.0
 * Threads_state_count{state="blocked",} 0.0
 * # HELP Threads_peak_count The peak live thread count since the Java virtual machine started or peak was reset
 * # TYPE Threads_peak_count gauge
 * Threads_peak_count 7.0
 * # HELP Threads_deadlock_count The current number of threads that are in deadlock waiting to acquire object monitors or ownable synchronizers
 * # TYPE Threads_deadlock_count gauge
 * Threads_deadlock_count 0.0
 * </pre>
 */
public class LabeledThreadsMetricsProducer extends AbstractThreadsMetricsProducer {

    private static final Label STATE_LABEL = new Label("state");

    public LabeledThreadsMetricsProducer() {
        this(DEFAULT_NAME_PREFIX, null);
    }

    public LabeledThreadsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(
            namePrefix,
            metricModBuilder,
            getThreadMXBean(),
            new DeadlockInfoProvider(getThreadMXBean()));
    }

    public LabeledThreadsMetricsProducer(
        MetricName namePrefix,
        MetricModBuilder metricModBuilder,
        ThreadMXBean threadMxBean,
        DeadlockInfoProvider deadlockInfoProvider) {

        super(namePrefix, metricModBuilder, threadMxBean, deadlockInfoProvider);
    }

    @Override
    public void produceMetrics(MetricRegistry registry) {
        requireNonNull(registry);
        produceUnlabeled(registry);

        final var stateCount = registry.longVar(
            nameWithSuffix("state", "count"),
            Var.noTotal(), longVarConfigBuilderSupplier(STATE_COUNT_DESCRIPTION, STATE_LABEL));

        for (Thread.State state : Thread.State.values()) {
            final var stateName = state.toString().toLowerCase(Locale.ENGLISH);
            final var labelValues = labelValues(STATE_LABEL.value(stateName));
            stateCount.register(() -> (long)threadCountFor(state), labelValues);
        }
    }
}
