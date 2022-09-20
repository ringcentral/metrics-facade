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
 *         Dimensions:<br>
 *         state = {"waiting", "runnable", "timed_waiting", "terminated", "new", "blocked"}<br>
 *     </li>
 * </ul>
 *
 * All metrics have a name prefix. By default it is 'Threads'.<br>
 * <br>
 * Example of usage:
 * <pre>
 * MetricRegistry registry = new DefaultMetricRegistry();
 * new DefaultThreadsMetricsProducer().produceMetrics(registry);
 * PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);
 * System.out.println(exporter.exportMetrics());
 * </pre>
 * Corresponding output:
 * <pre>
 * # HELP Threads_daemon_count The current number of live daemon threads
 * # TYPE Threads_daemon_count gauge
 * Threads_daemon_count 5.0
 * # HELP Threads_timed_waiting_count The current number of threads in the corresponding state
 * # TYPE Threads_timed_waiting_count gauge
 * Threads_timed_waiting_count 1.0
 * # HELP Threads_peak_count The peak live thread count since the Java virtual machine started or peak was reset
 * # TYPE Threads_peak_count gauge
 * Threads_peak_count 6.0
 * # HELP Threads_waiting_count The current number of threads in the corresponding state
 * # TYPE Threads_waiting_count gauge
 * Threads_waiting_count 1.0
 * # HELP Threads_count The current number of live threads including both daemon and non-daemon threads
 * # TYPE Threads_count gauge
 * Threads_count 6.0
 * # HELP Threads_blocked_count The current number of threads in the corresponding state
 * # TYPE Threads_blocked_count gauge
 * Threads_blocked_count 0.0
 * # HELP Threads_terminated_count The current number of threads in the corresponding state
 * # TYPE Threads_terminated_count gauge
 * Threads_terminated_count 0.0
 * # HELP Threads_totalStarted_count The total number of threads created and also started since the Java virtual machine started
 * # TYPE Threads_totalStarted_count gauge
 * Threads_totalStarted_count 6.0
 * # HELP Threads_new_count The current number of threads in the corresponding state
 * # TYPE Threads_new_count gauge
 * Threads_new_count 0.0
 * # HELP Threads_runnable_count The current number of threads in the corresponding state
 * # TYPE Threads_runnable_count gauge
 * Threads_runnable_count 4.0
 * # HELP Threads_deadlock_count The current number of threads that are in deadlock waiting to acquire object monitors or ownable synchronizers
 * # TYPE Threads_deadlock_count gauge
 * Threads_deadlock_count 0.0
 * </pre>
 */
public class DefaultThreadsMetricsProducer extends AbstractThreadsMetricsProducer {

    public DefaultThreadsMetricsProducer() {
        this(DEFAULT_NAME_PREFIX, null);
    }

    public DefaultThreadsMetricsProducer(MetricName namePrefix, MetricModBuilder metricModBuilder) {
        this(namePrefix,
                metricModBuilder,
                getThreadMXBean(),
                new DeadlockInfoProvider(getThreadMXBean()));
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
                    longVarConfigBuilderSupplier(STATE_COUNT_DESCRIPTION));
        }
    }
}
