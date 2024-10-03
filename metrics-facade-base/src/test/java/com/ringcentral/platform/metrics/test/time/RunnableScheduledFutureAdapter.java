package com.ringcentral.platform.metrics.test.time;

import java.util.concurrent.*;

public abstract class RunnableScheduledFutureAdapter<V> implements RunnableScheduledFuture<V> {

    @Override
    public boolean isPeriodic() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public long getDelay(TimeUnit unit) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int compareTo(Delayed o) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isDone() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public V get() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public V get(long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException("Not supported");
    }
}
