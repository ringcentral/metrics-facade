package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

public class CountHistogramSnapshot extends HistogramSnapshotAdapter {

    private final long count;

    public CountHistogramSnapshot(long count) {
        this.count = count;
    }

    @Override
    public long count() {
        return count;
    }
}
