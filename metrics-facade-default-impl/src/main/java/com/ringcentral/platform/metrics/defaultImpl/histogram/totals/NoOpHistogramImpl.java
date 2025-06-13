package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;

public class NoOpHistogramImpl implements HistogramImpl {

    public static final NoOpHistogramImpl INSTANCE = new NoOpHistogramImpl();

    @Override
    public void update(long value) {}

    @Override
    public HistogramSnapshot snapshot() {
        return NoOpHistogramSnapshot.INSTANCE;
    }
}