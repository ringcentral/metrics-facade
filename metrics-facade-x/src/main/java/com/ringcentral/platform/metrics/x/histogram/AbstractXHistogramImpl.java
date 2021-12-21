package com.ringcentral.platform.metrics.x.histogram;

import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.measurables.Measurable;

import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

public abstract class AbstractXHistogramImpl implements XHistogramImpl {

    protected final LongAdder counter;

    protected AbstractXHistogramImpl(Set<? extends Measurable> measurables) {
        this.counter =
            measurables.stream().anyMatch(m -> m instanceof Count) ?
            new LongAdder() :
            null;
    }

    @Override
    public void update(long value) {
        if (counter != null) {
            counter.increment();
        }

        updateImpl(value);
    }

    protected abstract void updateImpl(long count);

    @Override
    public long count() {
        return counter.sum();
    }
}
