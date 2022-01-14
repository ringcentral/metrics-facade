package com.ringcentral.platform.metrics.x.histogram.hdr;

import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.x.histogram.XHistogramImplSnapshot;

public class HdrXHistogramImplSnapshot implements XHistogramImplSnapshot {

    public static final long NO_VALUE = 0L;

    private final long min;
    private final long max;
    private final double mean;
    private final double standardDeviation;
    private final double[] quantiles;
    private final double[] percentileValues;
    private final long[] bucketUpperBounds;
    private final long[] bucketSizes;

    public HdrXHistogramImplSnapshot(
        long min,
        long max,
        double mean,
        double standardDeviation,
        double[] quantiles,
        double[] percentileValues,
        long[] bucketUpperBounds,
        long[] bucketSizes) {

        this.min = min;
        this.max = max;
        this.mean = mean;
        this.standardDeviation = standardDeviation;
        this.quantiles = quantiles;
        this.percentileValues = percentileValues;
        this.bucketUpperBounds = bucketUpperBounds;
        this.bucketSizes = bucketSizes;
    }

    @Override
    public long min() {
        return min;
    }

    @Override
    public long max() {
        return max;
    }

    @Override
    public double mean() {
        return mean;
    }

    @Override
    public double standardDeviation() {
        return standardDeviation;
    }

    @Override
    public double percentileValue(Histogram.Percentile percentile) {
        if (quantiles == null) {
            return NO_VALUE;
        }

        double quantile = percentile.quantile();

        for (int i = 0; i < quantiles.length; ++i) {
            if (quantile <= quantiles[i]) {
                return percentileValues[i];
            }
        }

        return max;
    }

    @Override
    public long bucketSize(Histogram.Bucket bucket) {
        if (bucketUpperBounds == null) {
            return NO_VALUE;
        }

        long upperBound = bucket.upperBoundAsLong();

        for (int i = 0; i < bucketUpperBounds.length; ++i) {
            if (upperBound <= bucketUpperBounds[i]) {
                return bucketSizes[i];
            }
        }

        return bucketSizes[bucketSizes.length - 1];
    }
}
