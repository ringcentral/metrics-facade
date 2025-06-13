package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

public class MutableTotalsHistogramSnapshot extends HistogramSnapshotAdapter {

    private long count;
    private long totalSum;

    @Override
    public long count() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public long totalSum() {
        return totalSum;
    }

    public void setTotalSum(long totalSum) {
        this.totalSum = totalSum;
    }
}
