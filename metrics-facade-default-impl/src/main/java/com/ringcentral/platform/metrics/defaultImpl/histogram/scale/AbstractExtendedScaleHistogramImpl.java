package com.ringcentral.platform.metrics.defaultImpl.histogram.scale;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfig;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal.MultiScaleTreeNode;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal.ScaleTree.StandardDeviationCalculator;
import org.HdrHistogram.WriterReaderPhaser;

import static com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot.NO_VALUE_DOUBLE;

public abstract class AbstractExtendedScaleHistogramImpl implements HistogramImpl {

    protected final ScaleHistogramImplConfig config;
    protected final int maxLazyTreeLevel;
    protected final int upperLazyTreeLevel;

    protected final MeasurementSpec measurementSpec;
    protected final boolean withCount;
    protected final boolean withTotalSum;
    protected final boolean withMin;
    protected final boolean withMax;
    protected final boolean withMean;
    protected final boolean withStandardDeviation;
    protected final boolean withPercentiles;
    protected final double[] quantiles;
    protected final double[] percentiles;
    protected final boolean withBuckets;
    protected final long[] bucketUpperBounds;

    protected final StandardDeviationCalculator standardDeviationCalculator = new StandardDeviationCalculator();
    protected final WriterReaderPhaser phaser = new WriterReaderPhaser();

    protected AbstractExtendedScaleHistogramImpl(
        ScaleHistogramImplConfig config,
        MeasurementSpec measurementSpec) {

        this.config = config;

        this.measurementSpec = measurementSpec;
        this.withCount = measurementSpec.isWithCount();
        this.withTotalSum = measurementSpec.isWithTotalSum();
        this.withMin = measurementSpec.isWithMin();
        this.withMax = measurementSpec.isWithMax();
        this.withMean = measurementSpec.isWithMean();
        this.withStandardDeviation = measurementSpec.isWithStandardDeviation();
        this.withPercentiles = measurementSpec.isWithPercentiles();
        this.quantiles = measurementSpec.quantiles();
        this.percentiles = measurementSpec.percentiles();
        this.withBuckets = measurementSpec.isWithBuckets();
        this.bucketUpperBounds = measurementSpec.bucketUpperBounds();

        this.maxLazyTreeLevel = config.maxLazyTreeLevel();
        this.upperLazyTreeLevel = this.maxLazyTreeLevel >= 0 ? this.maxLazyTreeLevel + 1 : -1;
    }

    protected boolean hasLazyTreeLevels() {
        return maxLazyTreeLevel >= 0;
    }

    protected void flipPhase() {
        phaser.flipPhase();
    }

    protected double calcPercentile(double quantile, MultiScaleTreeNode node) {
        long count = node.subtreeUpdateCount();

        if (count == 0L) {
            return NO_VALUE_DOUBLE;
        }

        long percentileCount = Math.min(Math.max(Math.round(count * quantile), 0L), count);

        while (!node.isNull()) {
            long leftSubtreeUpdateCount = node.leftSubtreeUpdateCount();
            long rightSubtreeUpdateCount = node.rightSubtreeUpdateCount();

            if (percentileCount > leftSubtreeUpdateCount
                && percentileCount <= (count - rightSubtreeUpdateCount)
                && (count - leftSubtreeUpdateCount - rightSubtreeUpdateCount) > 0L) {

                return node.point();
            }

            if (percentileCount <= leftSubtreeUpdateCount) {
                if (leftSubtreeUpdateCount > 0L) {
                    node.toLeft();
                    count = leftSubtreeUpdateCount;
                } else if ((count - leftSubtreeUpdateCount - rightSubtreeUpdateCount) > 0L) {
                    return node.point();
                } else {
                    node.toRight();
                    percentileCount -= (count - rightSubtreeUpdateCount);
                    count = rightSubtreeUpdateCount;
                }
            } else if (rightSubtreeUpdateCount > 0L) {
                node.toRight();
                percentileCount -= (count - rightSubtreeUpdateCount);
                count = rightSubtreeUpdateCount;
            } else {
                return node.point();
            }
        }

        return NO_VALUE_DOUBLE;
    }
}
