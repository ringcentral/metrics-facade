package com.ringcentral.platform.metrics.x.histogram.scale;

import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.x.histogram.XHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig;
import com.ringcentral.platform.metrics.x.histogram.scale.internal.MultiNode;
import com.ringcentral.platform.metrics.x.histogram.scale.internal.ScaleTree.StandardDeviationCalculator;
import org.HdrHistogram.WriterReaderPhaser;

import static com.ringcentral.platform.metrics.x.histogram.XHistogramSnapshot.NO_VALUE;

public abstract class AbstractExtendedScaleXHistogramImpl implements XHistogramImpl {

    protected final ScaleXHistogramImplConfig config;
    protected final int maxLazyTreeLevel;
    protected final int upperLazyTreeLevel;

    protected final MeasurementSpec measurementSpec;
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

    protected AbstractExtendedScaleXHistogramImpl(ScaleXHistogramImplConfig config, MeasurementSpec measurementSpec) {
        this.config = config;

        this.measurementSpec = measurementSpec;
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
        phaser.flipPhase(10000L);
    }

    protected long calcPercentile(double quantile, MultiNode node) {
        long nodeCount = node.subtreeUpdateCount();

        if (nodeCount == 0L) {
            return NO_VALUE;
        }

        long percentileCount = Math.min(Math.max((long)(nodeCount * quantile), 0L), nodeCount);

        while (!node.isNull()) {
            long leftCount = node.leftSubtreeUpdateCount();
            long rightCount = node.rightSubtreeUpdateCount();

            if ((nodeCount - rightCount) == percentileCount) {
                return node.point();
            }

            if (percentileCount <= leftCount) {
                if (node.hasLeft()) {
                    node.toLeft();
                    nodeCount = leftCount;
                } else {
                    return node.point();
                }
            } else {
                if (node.hasRight()) {
                    node.toRight();
                    nodeCount = rightCount;
                    percentileCount -= leftCount;
                } else {
                    return node.point();
                }
            }
        }

        return NO_VALUE;
    }
}
