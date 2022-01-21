package com.ringcentral.platform.metrics.x.histogram;

import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.histogram.Histogram.TotalSum;
import com.ringcentral.platform.metrics.measurables.Measurable;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.LongAdder;

public abstract class AbstractXHistogramImpl implements XHistogramImpl {

    protected final boolean withMin;
    protected final boolean withMax;
    protected final boolean withMean;
    protected final boolean withStandardDeviation;
    protected final double[] quantiles;
    protected final double[] percentiles;
    protected final long[] bucketUpperBounds;

    protected final LongAdder counter;
    protected final LongAdder totalSum;

    protected ScheduledExecutorService executor;

    protected AbstractXHistogramImpl(Set<? extends Measurable> measurables, ScheduledExecutorService executor) {
        this.withMin = measurables.stream().anyMatch(m -> m instanceof Histogram.Min);
        this.withMax = measurables.stream().anyMatch(m -> m instanceof Histogram.Max);
        this.withMean = measurables.stream().anyMatch(m -> m instanceof Histogram.Mean);
        this.withStandardDeviation = measurables.stream().anyMatch(m -> m instanceof Histogram.StandardDeviation);

        if (measurables.stream().anyMatch(m -> m instanceof Histogram.Percentile)) {
            this.quantiles = measurables.stream()
                .filter(m -> m instanceof Histogram.Percentile)
                .mapToDouble(m -> ((Histogram.Percentile)m).quantile())
                .sorted()
                .toArray();

            this.percentiles = measurables.stream()
                .filter(m -> m instanceof Histogram.Percentile)
                .mapToDouble(m -> ((Histogram.Percentile)m).percentile())
                .sorted()
                .toArray();
        } else {
            this.quantiles = null;
            this.percentiles = null;
        }

        if (measurables.stream().anyMatch(m -> m instanceof Histogram.Bucket)) {
            long[] bounds = measurables.stream()
                .filter(m -> m instanceof Histogram.Bucket)
                .mapToLong(m -> ((Histogram.Bucket)m).upperBoundAsLong())
                .sorted()
                .toArray();

            if (bounds[bounds.length - 1] != Long.MAX_VALUE) {
                long[] boundsWithInf = new long[bounds.length + 1];
                System.arraycopy(bounds, 0, boundsWithInf, 0, bounds.length);
                boundsWithInf[bounds.length] = Long.MAX_VALUE;
                bounds = boundsWithInf;
            }

            this.bucketUpperBounds = bounds;
        } else {
            this.bucketUpperBounds = null;
        }

        this.counter =
            measurables.stream().anyMatch(m -> m instanceof Count) ?
            new LongAdder() :
            null;

        this.totalSum =
            measurables.stream().anyMatch(m -> m instanceof TotalSum) ?
            new LongAdder() :
            null;

        this.executor = executor;
    }

    @Override
    public void update(long value) {
        if (counter != null) {
            counter.increment();
        }

        if (totalSum != null) {
            totalSum.add(value);
        }

        updateImpl(value);
    }

    protected abstract void updateImpl(long value);

    @Override
    public long count() {
        return counter.sum();
    }

    @Override
    public long totalSum() {
        return totalSum.sum();
    }
}
