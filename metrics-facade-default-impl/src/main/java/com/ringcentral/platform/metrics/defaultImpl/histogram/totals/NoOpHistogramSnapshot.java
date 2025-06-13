package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

public class NoOpHistogramSnapshot extends HistogramSnapshotAdapter {
    public static final NoOpHistogramSnapshot INSTANCE = new NoOpHistogramSnapshot();
}
