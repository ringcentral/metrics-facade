package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;

import java.util.concurrent.atomic.LongAdder;

public class TotalSumHistogramImpl implements HistogramImpl {

    private final LongAdder totalSumAdder;

    public TotalSumHistogramImpl() {
        this.totalSumAdder = new LongAdder();
    }

    @Override
    public void update(long value) {
        totalSumAdder.add(value);
    }

    @Override
    public HistogramSnapshot snapshot() {
        return new TotalSumHistogramSnapshot(totalSumAdder.sum());
    }
}