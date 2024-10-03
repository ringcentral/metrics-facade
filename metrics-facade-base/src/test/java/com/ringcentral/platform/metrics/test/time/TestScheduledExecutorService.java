package com.ringcentral.platform.metrics.test.time;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TestScheduledExecutorService extends ScheduledExecutorServiceAdapter {

    private final TestTimeNanosProvider timeNanosProvider;

    public TestScheduledExecutorService(TestTimeNanosProvider timeNanosProvider) {
        this.timeNanosProvider = timeNanosProvider;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit timeUnit) {
        long delayNanos = timeUnit.toNanos(delay);
        TestRunnableScheduledFuture future = new TestRunnableScheduledFuture();

        if (delayNanos > 0L) {
            timeNanosProvider.addListener(t -> {
                command.run();
                future.run();
            }, delayNanos);
        } else {
            command.run();
            future.run();
        }

        return future;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit timeUnit) {
        TestTimeNanosProvider.PeriodicListener listener = new TestTimeNanosProvider.PeriodicListener() {

            @Override
            public long periodNanos() {
                return timeUnit.toNanos(delay);
            }

            @Override
            public void whenTimeNanos(long timeNanos) {
                command.run();
            }
        };

        timeNanosProvider.addListener(listener, timeUnit.toNanos(initialDelay));
        return new TestRunnableScheduledFuture();
    }

    static class TestRunnableScheduledFuture extends RunnableScheduledFutureAdapter<Void> {

        private boolean done;
        private boolean cancelled;

        @Override
        public synchronized boolean isDone() {
            return done;
        }

        @Override
        public synchronized void run() {
            if (!cancelled) {
                done = true;
            }
        }

        @Override
        public synchronized boolean isCancelled() {
            return cancelled;
        }

        @Override
        public synchronized boolean cancel(boolean mayInterruptIfRunning) {
            if (done || cancelled) {
                return false;
            } else {
                return cancelled = true;
            }
        }
    }
}
