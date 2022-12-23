package com.ringcentral.platform.metrics.samples.histogram;

import com.ringcentral.platform.metrics.defaultImpl.histogram.DefaultHistogramSnapshot;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;

public class CountAndTotalSumScalingHistogramImpl implements HistogramImpl {

    private final long factor;
    private long count;
    private long totalSum;

    public CountAndTotalSumScalingHistogramImpl(long factor) {
        this.factor = factor;
    }

    @Override
    public synchronized void update(long value) {
        ++count;
        totalSum += value;
    }

    @Override
    public synchronized HistogramSnapshot snapshot() {
        return new DefaultHistogramSnapshot(
            count * factor,
            totalSum * factor,
            0L,
            0L,
            0.0,
            0.0,
            null,
            null,
            null,
            null);
    }
}
