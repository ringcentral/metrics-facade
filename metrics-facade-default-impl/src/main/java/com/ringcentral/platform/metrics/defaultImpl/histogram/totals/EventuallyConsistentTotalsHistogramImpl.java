package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;
import com.ringcentral.platform.metrics.histogram.Histogram;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.LongAdder;

/**
 * An implementation of {@link HistogramImpl} that ensures eventually consistent reporting of totals:
 * {@link Counter.Count} and {@link Histogram.TotalSum}. No other values are reported.
 * <p>
 * Uses {@link LongAdder} for both counting and summing to provide high throughput under contention.
 * The {@link #snapshot()} method returns the latest {@link Counter.Count} and {@link Histogram.TotalSum},
 * but under concurrent updates these two values may not reflect the exact same set of operations (may differ).
 */
public class EventuallyConsistentTotalsHistogramImpl implements TotalsHistogramImpl {

    private final LongAdder counter;
    private final LongAdder totalSumAdder;

    public EventuallyConsistentTotalsHistogramImpl() {
        this.counter = new LongAdder();
        this.totalSumAdder = new LongAdder();
    }

    @Override
    public void update(long value) {
        counter.increment();
        totalSumAdder.add(value);
    }

    @Override
    public HistogramSnapshot snapshot() {
        return new TotalsHistogramSnapshot(counter.sum(), totalSumAdder.sum());
    }

    @Override
    public void fillSnapshot(@Nonnull MutableTotalsHistogramSnapshot snapshot) {
        snapshot.setCount(counter.sum());
        snapshot.setTotalSum(totalSumAdder.sum());
    }
}
