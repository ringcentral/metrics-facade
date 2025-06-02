package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

public class TotalsHistogramSnapshot extends HistogramSnapshotAdapter {

    private final long count;
    private final long totalSum;

    public TotalsHistogramSnapshot(long count, long totalSum) {
        this.count = count;
        this.totalSum = totalSum;
    }

    @Override
    public long count() {
        return count;
    }

    @Override
    public long totalSum() {
        return totalSum;
    }
}
