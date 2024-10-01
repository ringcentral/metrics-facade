package com.ringcentral.platform.metrics.instrument.executors;

import com.ringcentral.platform.metrics.MetricKey;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.timer.Stopwatch;

import javax.annotation.Nonnull;
import java.util.concurrent.*;
import java.util.function.Function;

import static com.ringcentral.platform.metrics.names.MetricName.name;

/**
 * A {@link ScheduledExecutorService} wrapper that provides the following metrics for scheduled tasks:
 * <ul>
 *   <li>{@link com.ringcentral.platform.metrics.rate.Rate} for tasks scheduled to run once, with the name: {@code metricKeyProvider.apply(name("scheduled", "once"))}</li>
 *   <li>{@link com.ringcentral.platform.metrics.rate.Rate} for tasks scheduled to run repetitively: {@code metricKeyProvider.apply(name("scheduled", "repetitively"))}</li>
 *   <li>{@link com.ringcentral.platform.metrics.rate.Rate} for tasks that overrun their scheduled period: {@code metricKeyProvider.apply(name("scheduled", "overrun"))}</li>
 *   <li>{@link com.ringcentral.platform.metrics.histogram.Histogram} for the percentage of the scheduled period used by task execution:
 *       {@code metricKeyProvider.apply(name("scheduled", "period", "percent"))}</li>
 * </ul>
 *
 * This class also provides the core metrics available in {@link MonitoredExecutorService}:
 * <ul>
 *   <li>{@link com.ringcentral.platform.metrics.rate.Rate} for submitted tasks, with the name {@code metricKeyProvider.apply(name("submitted"))}</li>
 *   <li>{@link com.ringcentral.platform.metrics.counter.Counter} for running tasks: {@code metricKeyProvider.apply(name("running"))}</li>
 *   <li>{@link com.ringcentral.platform.metrics.rate.Rate} for completed tasks: {@code metricKeyProvider.apply(name("completed"))}</li>
 *   <li>{@link com.ringcentral.platform.metrics.timer.Timer} to measure the time between task submission and the start of its execution:
 *       {@code metricKeyProvider.apply(name("idle"))}</li>
 *   <li>{@link com.ringcentral.platform.metrics.timer.Timer} for task execution time: {@code metricKeyProvider.apply(name("execution"))}</li>
 * </ul>
 *
 * To create this wrapper, use the {@link MonitoredScheduledExecutorServiceBuilder}:
 * <pre>
 * {@code
 * // Example 1: Default naming (based in a sequence of positive integer number)
 * // executor_service_submitted_total{name="1",} 6.0
 * monitoredScheduledExecutorService(parent, registry).build();
 *
 * // Example 2: Custom name
 * // executor_service_submitted_total{name="customName",} 6.0
 * monitoredScheduledExecutorService(parent, registry)
 *   .name("customName")
 *   .build();
 *
 * // Example 3: Exclude name from labels and make it a part of the name
 * // executor_service_nameNotLabel_submitted_total 6.0
 * monitoredScheduledExecutorService(parent, registry)
 *   .name("nameNotLabel")
 *   .nameAsLabel(false)
 *   .build();
 *
 * // Example 4: Custom name prefix and additional labels
 * // customMetricNamePrefix_submitted_total{sample="MonitoredExecutorService", name="customMetricNamePrefixAndAdditionalLabels",} 6.0
 * monitoredScheduledExecutorService(parent, registry)
 *   .name("customMetricNamePrefixAndAdditionalLabels")
 *   .metricNamePrefix(name("customMetricNamePrefix"))
 *   .prefixLabelValues(labelValues(SAMPLE.value("MonitoredExecutorService")))
 *   .additionalLabelValues(labelValues(SERVICE.value("myService")))
 *   .build();
 * }
 * </pre>
 *
 * For more details, see the relevant example in {@code MonitoredScheduledExecutorServiceSample} within the metrics-facade-samples submodule:
 * <a href="https://github.com/ringcentral/metrics-facade/tree/master/metrics-facade-samples">https://github.com/ringcentral/metrics-facade/tree/master/metrics-facade-samples</a>
 */
public class MonitoredScheduledExecutorService extends AbstractMonitoredExecutorService<ScheduledExecutorService> implements ScheduledExecutorService {

    private final Rate scheduledOnceRate;
    private final Rate scheduledRepetitivelyRate;
    private final Rate scheduledOverrunRate;
    private final Histogram periodPercentHistogram;

    /**
     * See also {@link MonitoredScheduledExecutorServiceBuilder}
     */
    public MonitoredScheduledExecutorService(
        @Nonnull ScheduledExecutorService parent,
        @Nonnull MetricRegistry registry,
        @Nonnull Function<MetricName, MetricKey> metricKeyProvider,
        @Nonnull LabelValues labelValues) {

        super(parent, registry, metricKeyProvider, labelValues);

        this.scheduledOnceRate = rate(registry, name("scheduled", "once"));
        this.scheduledRepetitivelyRate = rate(registry, name("scheduled", "repetitively"));
        this.scheduledOverrunRate = rate(registry, name("scheduled", "overrun"));
        this.periodPercentHistogram = histogram(registry, name("scheduled", "period", "percent"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public ScheduledFuture<?> schedule(@Nonnull Runnable task, long delay, @Nonnull TimeUnit unit) {
        scheduledOnceRate.mark(labelValues);
        return parent.schedule(new MonitoredRunnable(task), delay, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public <V> ScheduledFuture<V> schedule(@Nonnull Callable<V> task, long delay, @Nonnull TimeUnit unit) {
        scheduledOnceRate.mark(labelValues);
        return parent.schedule(new MonitoredCallable<>(task), delay, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public ScheduledFuture<?> scheduleAtFixedRate(@Nonnull Runnable task, long initialDelay, long period, @Nonnull TimeUnit unit) {
        scheduledRepetitivelyRate.mark(labelValues);
        return parent.scheduleAtFixedRate(new MonitoredPeriodicRunnable(task, period, unit), initialDelay, period, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public ScheduledFuture<?> scheduleWithFixedDelay(@Nonnull Runnable task, long initialDelay, long delay, @Nonnull TimeUnit unit) {
        scheduledRepetitivelyRate.mark(labelValues);
        return parent.scheduleWithFixedDelay(new MonitoredRunnable(task), initialDelay, delay, unit);
    }

    private class MonitoredPeriodicRunnable implements Runnable {

        private final Runnable task;
        private final long period;

        MonitoredPeriodicRunnable(Runnable task, long period, TimeUnit unit) {
            this.task = task;
            this.period = unit.toNanos(period);
        }

        @Override
        public void run() {
            runningCounter.inc(labelValues);
            Stopwatch executionStopwatch = executionTimer.stopwatch(labelValues);

            try {
                task.run();
            } finally {
                long elapsed = executionStopwatch.stop();
                runningCounter.dec(labelValues);
                completedRate.mark(labelValues);

                if (elapsed > period) {
                    scheduledOverrunRate.mark(labelValues);
                }

                periodPercentHistogram.update((100L * elapsed) / period, labelValues);
            }
        }
    }
}
