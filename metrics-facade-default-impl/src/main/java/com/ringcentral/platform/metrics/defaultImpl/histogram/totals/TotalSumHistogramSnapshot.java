package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

public class TotalSumHistogramSnapshot extends HistogramSnapshotAdapter {

    private final long totalSum;

    public TotalSumHistogramSnapshot(long totalSum) {
        this.totalSum = totalSum;
    }

    @Override
    public long totalSum() {
        return totalSum;
    }
}
