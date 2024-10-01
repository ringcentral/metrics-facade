package com.ringcentral.platform.metrics.instrument.executors;

import com.ringcentral.platform.metrics.MetricKey;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.var.longVar.LongVar;

import javax.annotation.Nonnull;
import java.util.concurrent.*;
import java.util.function.Function;

import static com.ringcentral.platform.metrics.names.MetricName.name;

/**
 * An {@link ExecutorService} wrapper that provides the following metrics:
 * <ul>
 *   <li>{@link Rate} for submitted tasks, with the name {@code metricKeyProvider.apply(name("submitted"))}</li>
 *   <li>{@link Counter} for running tasks: {@code metricKeyProvider.apply(name("running"))}</li>
 *   <li>{@link Rate} for completed tasks: {@code metricKeyProvider.apply(name("completed"))}</li>
 *   <li>{@link Timer} to measure the time between task submission and the start of its execution:
 *       {@code metricKeyProvider.apply(name("idle"))}</li>
 *   <li>{@link Timer} for task execution time: {@code metricKeyProvider.apply(name("execution"))}</li>
 * </ul>
 *
 * If the underlying (parent) {@link ExecutorService} is a {@link ThreadPoolExecutor},
 * it additionally provides the following {@link LongVar} metrics:
 * <ul>
 *   <li>{@code metricKeyProvider.apply(name("pool", "size"))}: parent.getPoolSize()}</li>
 *   <li>{@code metricKeyProvider.apply(name("pool", "core"))}: parent.getCorePoolSize()}</li>
 *   <li>{@code metricKeyProvider.apply(name("pool", "max"))}: parent.getMaximumPoolSize()}</li>
 *   <li>{@code metricKeyProvider.apply(name("tasks", "active"))}: parent.getActiveCount()}</li>
 *   <li>{@code metricKeyProvider.apply(name("tasks", "completed"))}: parent.getCompletedTaskCount()}</li>
 *   <li>{@code metricKeyProvider.apply(name("tasks", "queued"))}: parent.getQueue().size()}</li>
 *   <li>{@code metricKeyProvider.apply(name("tasks", "capacity"))}: parent.getQueue().remainingCapacity()}</li>
 * </ul>
 *
 * If the underlying (parent) {@link ExecutorService} is a {@link ForkJoinPool}, it additionally provides the following {@link LongVar} metrics:
 * <ul>
 *   <li>{@code metricKeyProvider.apply(name("tasks", "stolen"))}: parent.getStealCount()</li>
 *   <li>{@code metricKeyProvider.apply(name("tasks", "queued"))}: parent.getQueuedTaskCount()</li>
 *   <li>{@code metricKeyProvider.apply(name("threads", "active"))}: parent.getActiveThreadCount()</li>
 *   <li>{@code metricKeyProvider.apply(name("threads", "running"))}: parent.getRunningThreadCount()</li>
 * </ul>
 *
 * To create this wrapper, use the {@link MonitoredExecutorServiceBuilder}:
 * <pre>
 * {@code
 * // Example 1: Default naming (based in a sequence of positive integer number)
 * // executor_service_submitted_total{name="1",} 6.0
 * monitoredExecutorService(parent, registry).build();
 *
 * // Example 2: Custom name
 * // executor_service_submitted_total{name="customName",} 6.0
 * monitoredExecutorService(parent, registry)
 *   .name("customName")
 *   .build();
 *
 * // Example 3: Exclude name from labels and make it a part of the name
 * // executor_service_nameNotLabel_submitted_total 6.0
 * monitoredExecutorService(parent, registry)
 *   .name("nameNotLabel")
 *   .nameAsLabel(false)
 *   .build();
 *
 * // Example 4: Custom name prefix and additional labels
 * // customMetricNamePrefix_submitted_total{sample="MonitoredExecutorService", name="customMetricNamePrefixAndAdditionalLabels",} 6.0
 * monitoredExecutorService(parent, registry)
 *   .name("customMetricNamePrefixAndAdditionalLabels")
 *   .metricNamePrefix(name("customMetricNamePrefix"))
 *   .prefixLabelValues(labelValues(SAMPLE.value("MonitoredExecutorService")))
 *   .additionalLabelValues(labelValues(SERVICE.value("myService")))
 *   .build();
 * }
 *
 * // Example 5: Include the executor class name as a "class" label
 * // classLabel_submitted_total{class="FinalizableDelegatedExecutorService",name="classLabel",} 6.0
 * monitoredExecutorService(executor, registry)
 *   .name("classLabel")
 *   .metricNamePrefix(name("classLabel"))
 *   .withExecutorServiceClass(true, true)
 *   .build();
 * </pre>
 *
 * For more details, see the relevant example in {@code MonitoredExecutorServiceSample} within the metrics-facade-samples submodule:
 * <a href="https://github.com/ringcentral/metrics-facade/tree/master/metrics-facade-samples">https://github.com/ringcentral/metrics-facade/tree/master/metrics-facade-samples</a>
 */
public class MonitoredExecutorService extends AbstractMonitoredExecutorService<ExecutorService> {

    /**
     * See also {@link MonitoredExecutorServiceBuilder}
     */
    public MonitoredExecutorService(
        @Nonnull ExecutorService parent,
        @Nonnull MetricRegistry registry,
        @Nonnull Function<MetricName, MetricKey> metricKeyProvider,
        @Nonnull LabelValues labelValues) {

        super(parent, registry, metricKeyProvider, labelValues);

        if (parent instanceof ForkJoinPool) {
            addMetricsForForkJoinPool((ForkJoinPool)parent, registry);
        }
    }

    private void addMetricsForForkJoinPool(ForkJoinPool parent, MetricRegistry registry) {;
        longVar(registry, name("tasks", "stolen"), parent::getStealCount);
        longVar(registry, name("tasks", "queued"), parent::getQueuedTaskCount);
        longVar(registry, name("threads", "active"), () -> (long)parent.getActiveThreadCount());
        longVar(registry, name("threads", "running"), () -> (long)parent.getRunningThreadCount());
    }
}
