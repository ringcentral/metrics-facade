package com.ringcentral.platform.metrics.samples.histogram;

import com.ringcentral.platform.metrics.x.histogram.*;

public class LastValueXHistogramImpl implements XHistogramImpl {

    private long lastValue;

    @Override
    public synchronized void update(long value) {
        lastValue = value;
    }

    @Override
    public synchronized XHistogramSnapshot snapshot() {
        return new DefaultXHistogramSnapshot(
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
