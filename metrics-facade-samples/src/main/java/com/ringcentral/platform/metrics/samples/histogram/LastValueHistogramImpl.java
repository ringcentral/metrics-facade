package com.ringcentral.platform.metrics.samples.histogram;

import com.ringcentral.platform.metrics.defaultImpl.histogram.*;

public class LastValueHistogramImpl implements HistogramImpl {

    private long lastValue;

    @Override
    public synchronized void update(long value) {
        lastValue = value;
    }

    @Override
    public synchronized HistogramSnapshot snapshot() {
        return new DefaultHistogramSnapshot(
            lastValue,
            lastValue,
            lastValue,
            lastValue,
            lastValue,
            lastValue,
            null,
            null,
            null,
            null);
    }
}
