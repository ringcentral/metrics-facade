package com.ringcentral.platform.metrics.instrument.executors;

import com.ringcentral.platform.metrics.MetricKey;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.timer.Stopwatch;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.var.Var;
import com.ringcentral.platform.metrics.var.longVar.LongVar;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.*;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.*;
import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder.*;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.*;
import static com.ringcentral.platform.metrics.var.longVar.configs.builders.LongVarConfigBuilder.withLongVar;
import static java.util.stream.Collectors.toList;

public abstract class AbstractMonitoredExecutorService<ES extends ExecutorService> implements ExecutorService {

    protected final ES parent;

    /**
     * Maps to {@link MetricKey} instead of just {@link MetricName} to support cases where the user needs to define complex keys,
     * such as {@link com.ringcentral.platform.metrics.PrefixLabelValuesMetricKey}.
     */
    protected final Function<MetricName, MetricKey> metricKeyProvider;
    protected final LabelValues labelValues;
    protected final List<Label> labels;

    protected final Rate submittedRate;
    protected final Counter runningCounter;
    protected final Rate completedRate;
    protected final Timer idleTimer;
    protected final Timer executionTimer;

    protected AbstractMonitoredExecutorService(
        @Nonnull ES parent,
        @Nonnull MetricRegistry registry,
        @Nonnull Function<MetricName, MetricKey> metricKeyProvider,
        @Nonnull LabelValues labelValues) {

        this.parent = parent;
        this.metricKeyProvider = metricKeyProvider;
        this.labelValues = labelValues;
        this.labels = labelValues.labels();

        this.submittedRate = rate(registry, name("submitted"));
        this.runningCounter = counter(registry, name("running"));
        this.completedRate = rate(registry, name("completed"));
        this.idleTimer = timer(registry, name("idle"));
        this.executionTimer = timer(registry, name("execution"));

        if (parent instanceof ThreadPoolExecutor) {
            addMetricsForThreadPoolExecutor((ThreadPoolExecutor)parent, registry);
        }
    }

    protected void addMetricsForThreadPoolExecutor(ThreadPoolExecutor parent, MetricRegistry registry) {
        longVar(registry, name("pool", "size"), () -> (long)parent.getPoolSize());
        longVar(registry, name("pool", "core"), () -> (long)parent.getCorePoolSize());
        longVar(registry, name("pool", "max"), () -> (long)parent.getMaximumPoolSize());

        BlockingQueue<Runnable> queue = parent.getQueue();
        longVar(registry, name("tasks", "active"), () -> (long)parent.getActiveCount());
        longVar(registry, name("tasks", "completed"), parent::getCompletedTaskCount);
        longVar(registry, name("tasks", "queued"), () -> (long)queue.size());
        longVar(registry, name("tasks", "capacity"), () -> (long)queue.remainingCapacity());
    }

    protected LongVar longVar(MetricRegistry registry, MetricName name, Supplier<Long> valueSupplier) {
        if (labels.isEmpty()) {
            return registry.longVar(metricKey(name), valueSupplier);
        } else {
            LongVar longVar = registry.longVar(metricKey(name), Var.noTotal(), () -> withLongVar().labels(labels));
            longVar.register(valueSupplier, labelValues);
            return longVar;
        }
    }

    protected Counter counter(MetricRegistry registry, MetricName name) {
        return registry.counter(
            metricKey(name),
            () -> labels.isEmpty() ? counterConfigBuilder() : withCounter().labels(labels));
    }

    protected Rate rate(MetricRegistry registry, MetricName name) {
        return registry.rate(
            metricKey(name),
            () -> labels.isEmpty() ? rateConfigBuilder() : withRate().labels(labels));
    }

    protected Histogram histogram(MetricRegistry registry, MetricName name) {
        return registry.histogram(
            metricKey(name),
            () -> labels.isEmpty() ? histogramConfigBuilder() : withHistogram().labels(labels));
    }

    protected Timer timer(MetricRegistry registry, MetricName name) {
        return registry.timer(
            metricKey(name),
            () -> labels.isEmpty() ? timerConfigBuilder() : withTimer().labels(labels));
    }

    protected MetricKey metricKey(MetricName name) {
        return metricKeyProvider.apply(name);
    }

    protected class MonitoredRunnable implements Runnable {

        private final Runnable task;
        private final Stopwatch idleStopwatch;

        protected MonitoredRunnable(Runnable task) {
            this.task = task;
            this.idleStopwatch = idleTimer.stopwatch(labelValues);
        }

        @Override
        public void run() {
            idleStopwatch.stop();
            runningCounter.inc(labelValues);
            Stopwatch executionStopwatch = executionTimer.stopwatch(labelValues);

            try {
                task.run();
            } finally {
                executionStopwatch.stop();
                runningCounter.dec(labelValues);
                completedRate.mark(labelValues);
            }
        }
    }

    protected class MonitoredCallable<T> implements Callable<T> {

        private final Callable<T> task;
        private final Stopwatch idleStopwatch;

        protected MonitoredCallable(Callable<T> task) {
            this.task = task;
            this.idleStopwatch = idleTimer.stopwatch(labelValues);
        }

        @Override
        public T call() throws Exception {
            idleStopwatch.stop();
            runningCounter.inc(labelValues);
            Stopwatch executionStopwatch = executionTimer.stopwatch(labelValues);

            try {
                return task.call();
            } finally {
                executionStopwatch.stop();
                runningCounter.dec(labelValues);
                completedRate.mark(labelValues);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(@Nonnull Runnable task) {
        submittedRate.mark(labelValues);
        parent.execute(new MonitoredRunnable(task));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public <T> Future<T> submit(@Nonnull Callable<T> task) {
        submittedRate.mark(labelValues);
        return parent.submit(new MonitoredCallable<>(task));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public <T> Future<T> submit(@Nonnull Runnable task, T result) {
        submittedRate.mark(labelValues);
        return parent.submit(new MonitoredRunnable(task), result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Future<?> submit(@Nonnull Runnable task) {
        submittedRate.mark(labelValues);
        return parent.submit(new MonitoredRunnable(task));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks) throws InterruptedException {
        submittedRate.mark(tasks.size(), labelValues);
        return parent.invokeAll(monitor(tasks));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        submittedRate.mark(tasks.size(), labelValues);
        return parent.invokeAll(monitor(tasks), timeout, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks) throws ExecutionException, InterruptedException {
        submittedRate.mark(tasks.size(), labelValues);
        return parent.invokeAny(monitor(tasks));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        submittedRate.mark(tasks.size(), labelValues);
        return parent.invokeAny(monitor(tasks), timeout, unit);
    }

    protected <T> Collection<? extends Callable<T>> monitor(Collection<? extends Callable<T>> tasks) {
        return tasks.stream().map(MonitoredCallable<T>::new).collect(toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        parent.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<Runnable> shutdownNow() {
        return parent.shutdownNow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isShutdown() {
        return parent.isShutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminated() {
        return parent.isTerminated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean awaitTermination(long timeout, @Nonnull TimeUnit timeUnit) throws InterruptedException {
        return parent.awaitTermination(timeout, timeUnit);
    }
}
