package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;

import java.util.concurrent.atomic.LongAdder;

public class CountHistogramImpl implements HistogramImpl {

    private final LongAdder counter;

    public CountHistogramImpl() {
        this.counter = new LongAdder();
    }

    @Override
    public void update(long value) {
        counter.increment();
    }

    @Override
    public HistogramSnapshot snapshot() {
        return new CountHistogramSnapshot(counter.sum());
    }
}