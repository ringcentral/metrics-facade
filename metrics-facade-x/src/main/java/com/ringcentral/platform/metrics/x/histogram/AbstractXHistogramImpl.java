package com.ringcentral.platform.metrics.x.histogram;

import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.histogram.Histogram.TotalSum;
import com.ringcentral.platform.metrics.measurables.Measurable;

import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

public abstract class AbstractXHistogramImpl implements XHistogramImpl {

    protected final LongAdder counter;
    protected final LongAdder totalSum;

    protected AbstractXHistogramImpl(Set<? extends Measurable> measurables) {
        this.counter =
            measurables.stream().anyMatch(m -> m instanceof Count) ?
            new LongAdder() :
            null;

        this.totalSum =
            measurables.stream().anyMatch(m -> m instanceof TotalSum) ?
            new LongAdder() :
            null;
    }

    @Override
    public void update(long value) {
        if (counter != null) {
            counter.increment();
        }

        if (totalSum != null) {
            totalSum.add(value);
        }

        updateImpl(value);
    }

    protected abstract void updateImpl(long value);

    @Override
    public long count() {
        return counter.sum();
    }

    @Override
    public long totalSum() {
        return totalSum.sum();
    }
}
