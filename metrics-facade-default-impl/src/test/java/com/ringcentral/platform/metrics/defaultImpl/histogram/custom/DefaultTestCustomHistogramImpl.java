package com.ringcentral.platform.metrics.defaultImpl.histogram.custom;

import com.ringcentral.platform.metrics.defaultImpl.histogram.DefaultHistogramSnapshot;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;

public class DefaultTestCustomHistogramImpl implements HistogramImpl {

    private final long measurableValue;

    public DefaultTestCustomHistogramImpl(long measurableValue) {
        this.measurableValue = measurableValue;
    }

    @Override
    public void update(long value) {}

    @Override
    public HistogramSnapshot snapshot() {
        return new DefaultHistogramSnapshot(
            measurableValue,
            measurableValue,
            measurableValue,
            measurableValue,
            measurableValue,
            measurableValue,
            null,
            null,
            null,
            null);
    }
}
