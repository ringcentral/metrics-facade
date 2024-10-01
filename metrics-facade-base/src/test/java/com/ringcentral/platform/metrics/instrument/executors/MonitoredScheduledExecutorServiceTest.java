package com.ringcentral.platform.metrics.instrument.executors;

import com.ringcentral.platform.metrics.MetricKey;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.test.time.TestScheduledExecutorService;
import com.ringcentral.platform.metrics.test.time.TestTimeNanosProvider;
import com.ringcentral.platform.metrics.timer.Stopwatch;
import com.ringcentral.platform.metrics.timer.Timer;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import static com.ringcentral.platform.metrics.instrument.executors.AbstractMonitoredExecutorServiceBuilder.*;
import static com.ringcentral.platform.metrics.instrument.executors.MonitoredScheduledExecutorServiceBuilder.monitoredScheduledExecutorService;
import static com.ringcentral.platform.metrics.labels.LabelValues.labelValues;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class MonitoredScheduledExecutorServiceTest {

    static final LabelValues LABEL_VALUES = labelValues(
        CLASS.value(ScheduledExecutorServiceImpl.class.getSimpleName()),
        NAME.value("underTest"));

    MetricRegistry registry = mock(MetricRegistry.class);
    TestTimeNanosProvider timeNanosProvider = new TestTimeNanosProvider();
    ScheduledExecutorService parent = new TestScheduledExecutorService(timeNanosProvider);
    ScheduledExecutorService monitoredExecutor;

    // Common
    Rate submittedRate = mock(Rate.class);
    Counter runningCounter = mock(Counter.class);
    Rate completedRate = mock(Rate.class);
    Timer idleTimer = mock(Timer.class);
    Stopwatch idleStopwatch = mock(Stopwatch.class);
    Timer executionTimer = mock(Timer.class);
    Stopwatch executionStopwatch = mock(Stopwatch.class);

    // Scheduled
    Rate scheduledOnceRate = mock(Rate.class);
    Rate scheduledRepetitivelyRate = mock(Rate.class);
    Rate scheduledOverrunRate = mock(Rate.class);
    Histogram periodPercentHistogram = mock(Histogram.class);

    @Before
    public void before() {
        parent = new ScheduledExecutorServiceImpl();

        when(idleTimer.stopwatch(any())).thenReturn(idleStopwatch);
        when(executionTimer.stopwatch(any())).thenReturn(executionStopwatch);

        when(registry.rate(any(), any())).thenAnswer(inv -> {
            MetricKey metricKey = inv.getArgument(0, MetricKey.class);
            String name = metricKey.name().toString();

            if (name.contains("submitted")) {
                return submittedRate;
            }

            if (name.contains("completed")) {
                return completedRate;
            }

            if (name.contains("scheduled.once")) {
                return scheduledOnceRate;
            }

            if (name.contains("scheduled.repetitively")) {
                return scheduledRepetitivelyRate;
            }

            if (name.contains("scheduled.overrun")) {
                return scheduledOverrunRate;
            }

            throw new IllegalArgumentException();
        });

        when(registry.counter(any(), any())).thenAnswer(inv -> {
            MetricKey metricKey = inv.getArgument(0, MetricKey.class);
            String name = metricKey.name().toString();

            if (name.contains("running")) {
                return runningCounter;
            }

            throw new IllegalArgumentException();
        });

        when(registry.timer(any(), any())).thenAnswer(inv -> {
            MetricKey metricKey = inv.getArgument(0, MetricKey.class);
            String name = metricKey.name().toString();

            if (name.contains("idle")) {
                return idleTimer;
            }

            if (name.contains("execution")) {
                return executionTimer;
            }

            throw new IllegalArgumentException();
        });

        when(registry.histogram(any(), any())).thenAnswer(invocation -> {
            MetricKey metricKey = invocation.getArgument(0, MetricKey.class);
            String name = metricKey.name().toString();

            if (name.contains("scheduled.period.percent")) {
                return periodPercentHistogram;
            }

            throw new IllegalArgumentException();
        });

        monitoredExecutor = monitoredScheduledExecutorService(parent, registry)
            .name("underTest")
            .withExecutorServiceClass(true, true)
            .build();
    }

    @Test
    public void execute() {
        // given
        verifyNoMoreMetricInteractions();
        Runnable task = mock(Runnable.class);

        // when
        monitoredExecutor.execute(task);

        // then
        verifyNoMoreMetricInteractions();
    }

    @Test
    public void submit() throws Exception {
        // given
        verifyNoMoreMetricInteractions();
        Callable<Integer> task = mock(Callable.class);
        when(task.call()).thenReturn(1);

        // when
        assertThat(monitoredExecutor.submit(task).get(), is(1));

        // then
        verifyNoMoreMetricInteractions();
    }

    @Test
    public void schedule() {
        // given
        verifyNoMoreMetricInteractions();
        Runnable task = mock(Runnable.class);

        // when
        monitoredExecutor.schedule(task, 10, SECONDS);

        // then
        verifyNoMoreMetricInteractions();

        // when
        timeNanosProvider.increaseSec(9);

        // then
        verifyNoMoreMetricInteractions();

        // when
        timeNanosProvider.increaseSec(1);

        // then
        verify(idleStopwatch).stop();
        verify(executionTimer).stopwatch(LABEL_VALUES);
        verify(runningCounter).inc(LABEL_VALUES);
        verify(runningCounter).dec(LABEL_VALUES);
        verify(completedRate).mark(LABEL_VALUES);
        verify(executionStopwatch).stop();

        verifyNoMoreMetricInteractions();
    }

    @Test
    public void scheduleAtFixedRate() {
        // given
        verifyNoMoreMetricInteractions();
        when(executionStopwatch.stop()).thenReturn(SECONDS.toNanos(10L));
        Runnable task = mock(Runnable.class);

        // when
        monitoredExecutor.scheduleAtFixedRate(task, 10, 10, SECONDS);

        // then
        verifyNoMoreMetricInteractions();

        // when
        timeNanosProvider.increaseSec(9);

        // then
        verifyNoMoreMetricInteractions();

        // when
        timeNanosProvider.increaseSec(1);

        // then
        verify(executionTimer).stopwatch(LABEL_VALUES);
        verify(runningCounter).inc(LABEL_VALUES);
        verify(runningCounter).dec(LABEL_VALUES);
        verify(completedRate).mark(LABEL_VALUES);
        verify(executionStopwatch).stop();
        verify(periodPercentHistogram).update(100L, LABEL_VALUES);
        // verify(scheduledOverrunRate).mark(LABEL_VALUES);
        verifyNoMoreMetricInteractions();
    }

    @Test
    public void scheduleAtFixedRate_Overrun() {
        // given
        verifyNoMoreMetricInteractions();
        when(executionStopwatch.stop()).thenReturn(SECONDS.toNanos(15L));
        Runnable task = mock(Runnable.class);

        // when
        monitoredExecutor.scheduleAtFixedRate(task, 10, 10, SECONDS);

        // then
        verifyNoMoreMetricInteractions();

        // when
        timeNanosProvider.increaseSec(9);

        // then
        verifyNoMoreMetricInteractions();

        // when
        timeNanosProvider.increaseSec(1);

        // then
        verify(executionTimer).stopwatch(LABEL_VALUES);
        verify(runningCounter).inc(LABEL_VALUES);
        verify(runningCounter).dec(LABEL_VALUES);
        verify(completedRate).mark(LABEL_VALUES);
        verify(executionStopwatch).stop();
        verify(periodPercentHistogram).update(150L, LABEL_VALUES);
        verify(scheduledOverrunRate).mark(LABEL_VALUES);

        verifyNoMoreMetricInteractions();
    }

    @Test
    public void invokeAll() throws Exception {
        // given
        verifyNoMoreMetricInteractions();

        Callable<Integer> task_1 = mock(Callable.class);
        when(task_1.call()).thenReturn(1);

        Callable<Integer> task_2 = mock(Callable.class);
        when(task_2.call()).thenReturn(2);

        // when
        assertThat(monitoredExecutor.invokeAll(List.of(task_1, task_2)).stream().map(MonitoredScheduledExecutorServiceTest::value).collect(toList()), is(List.of(1, 2)));

        // then
        verifyNoMoreMetricInteractions();
    }

    private void verifyNoMoreMetricInteractions() {
        verifyNoMoreInteractions(submittedRate, runningCounter, completedRate, idleTimer, idleStopwatch, executionTimer, executionStopwatch);
        verifyNoMoreInteractions(scheduledOnceRate, scheduledRepetitivelyRate, scheduledOverrunRate, periodPercentHistogram);
    }

    private static <T> CompletableFuture<T> completedFutureFor(Callable<T> task) {
        try {
            return completedFuture(task.call());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Integer value(Future<Integer> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    class ScheduledExecutorServiceImpl extends TestScheduledExecutorService {

        public ScheduledExecutorServiceImpl() {
            super(MonitoredScheduledExecutorServiceTest.this.timeNanosProvider);
        }

        @Override
        public void execute(Runnable task) {
            verify(submittedRate).mark(LABEL_VALUES);
            verify(idleTimer).stopwatch(LABEL_VALUES);
            verifyNoMoreMetricInteractions();

            task.run();

            verify(idleStopwatch).stop();
            verify(executionTimer).stopwatch(LABEL_VALUES);
            verify(runningCounter).inc(LABEL_VALUES);
            verify(runningCounter).dec(LABEL_VALUES);
            verify(completedRate).mark(LABEL_VALUES);
            verify(executionStopwatch).stop();
        }

        @Override
        @Nonnull
        public <T> Future<T> submit(@Nonnull Callable<T> task) {
            verify(submittedRate).mark(LABEL_VALUES);
            verify(idleTimer).stopwatch(LABEL_VALUES);
            verifyNoMoreMetricInteractions();

            Future<T> future = completedFutureFor(task);

            verify(idleStopwatch).stop();
            verify(executionTimer).stopwatch(LABEL_VALUES);
            verify(runningCounter).inc(LABEL_VALUES);
            verify(runningCounter).dec(LABEL_VALUES);
            verify(completedRate).mark(LABEL_VALUES);
            verify(executionStopwatch).stop();

            return future;
        }

        @Override
        @Nonnull
        public ScheduledFuture<?> schedule(@Nonnull Runnable command, long delay, @Nonnull TimeUnit timeUnit) {
            verify(scheduledOnceRate).mark(LABEL_VALUES);
            verify(idleTimer).stopwatch(LABEL_VALUES);
            verifyNoMoreMetricInteractions();

            return super.schedule(command, delay, timeUnit);
        }

        @Override
        @Nonnull
        public ScheduledFuture<?> scheduleAtFixedRate(@Nonnull Runnable command, long initialDelay, long delay, @Nonnull TimeUnit timeUnit) {
            verify(scheduledRepetitivelyRate).mark(LABEL_VALUES);
            verifyNoMoreMetricInteractions();

            return super.scheduleWithFixedDelay(command, initialDelay, delay, timeUnit);
        }

        @Override
        @Nonnull
        public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks) {
            verify(submittedRate).mark(2L, LABEL_VALUES);
            verify(idleTimer, times(2)).stopwatch(LABEL_VALUES);
            verifyNoMoreMetricInteractions();

            List<Future<T>> collect = tasks.stream().map(MonitoredScheduledExecutorServiceTest::completedFutureFor).collect(toList());

            verify(idleStopwatch, times(2)).stop();
            verify(executionTimer, times(2)).stopwatch(LABEL_VALUES);
            verify(runningCounter, times(2)).inc(LABEL_VALUES);
            verify(runningCounter, times(2)).dec(LABEL_VALUES);
            verify(completedRate, times(2)).mark(LABEL_VALUES);
            verify(executionStopwatch, times(2)).stop();

            return collect;
        }
    }
}