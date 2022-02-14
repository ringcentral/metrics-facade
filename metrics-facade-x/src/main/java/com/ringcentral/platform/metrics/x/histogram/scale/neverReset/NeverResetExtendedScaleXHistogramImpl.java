package com.ringcentral.platform.metrics.x.histogram.scale.neverReset;

import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.x.histogram.*;
import com.ringcentral.platform.metrics.x.histogram.scale.AbstractExtendedScaleXHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig;
import com.ringcentral.platform.metrics.x.histogram.scale.internal.DoubleNode;

import static com.ringcentral.platform.metrics.x.histogram.DefaultXHistogramSnapshot.emptySnapshot;
import static com.ringcentral.platform.metrics.x.histogram.XHistogramSnapshot.*;

public class NeverResetExtendedScaleXHistogramImpl extends AbstractExtendedScaleXHistogramImpl {

    private volatile NeverResetChunk activeChunk;
    private volatile NeverResetChunk inactiveChunk;
    private DoubleNode multiNode;

    public NeverResetExtendedScaleXHistogramImpl(
        ScaleXHistogramImplConfig config,
        MeasurementSpec measurementSpec) {

        super(config, measurementSpec);

        this.activeChunk = new NeverResetChunk(config, measurementSpec);
        this.inactiveChunk = new NeverResetChunk(config, measurementSpec);
    }

    @Override
    public void update(long value) {
        long criticalValueAtEnter = phaser.writerCriticalSectionEnter();

        try {
            activeChunk.update(value);
        } finally {
            phaser.writerCriticalSectionExit(criticalValueAtEnter);
        }
    }

    @Override
    public synchronized XHistogramSnapshot snapshot() {
        phaser.readerLock();

        try {
            inactiveChunk.startSnapshot();
            flipChunks();
            inactiveChunk.startSnapshot();

            activeChunk.calcLazySubtreeUpdateCounts();
            inactiveChunk.calcLazySubtreeUpdateCounts();

            XHistogramSnapshot snapshot = takeSnapshot();

            inactiveChunk.endSnapshot();
            inactiveChunk.resetSnapshotSum();
            flipChunks();
            inactiveChunk.endSnapshot();
            inactiveChunk.resetSnapshotSum();

            return snapshot;
        } finally {
            phaser.readerUnlock();
        }
    }

    private void flipChunks() {
        NeverResetChunk tempChunk = inactiveChunk;
        inactiveChunk = activeChunk;
        activeChunk = tempChunk;
        flipPhase();
    }

    @SuppressWarnings("ConstantConditions")
    private XHistogramSnapshot takeSnapshot() {
        boolean activeChunkNonEmpty = activeChunk.isNonEmpty();
        boolean inactiveChunkNonEmpty = inactiveChunk.isNonEmpty();

        if (!activeChunkNonEmpty && !inactiveChunkNonEmpty) {
            return emptySnapshot();
        }

        long count = NO_VALUE;

        if (withCount) {
            count = 0L;

            if (activeChunkNonEmpty) {
                count += activeChunk.treeUpdateCount();
            }

            if (inactiveChunkNonEmpty) {
                count += inactiveChunk.treeUpdateCount();
            }
        }

        long totalSum = NO_VALUE;

        if (withTotalSum) {
            totalSum = 0L;

            if (activeChunkNonEmpty) {
                totalSum += activeChunk.sum();
            }

            if (inactiveChunkNonEmpty) {
                totalSum += inactiveChunk.sum();
            }
        }

        long min = NO_VALUE;

        if (withMin) {
            min = activeChunkNonEmpty ? activeChunk.min() : Long.MAX_VALUE;

            if (inactiveChunkNonEmpty) {
                long inactiveChunkMin = inactiveChunk.min();

                if (inactiveChunkMin < min) {
                    min = inactiveChunkMin;
                }
            }
        }

        long max = NO_VALUE;

        if (withMax) {
            max = activeChunkNonEmpty ? activeChunk.max() : Long.MIN_VALUE;

            if (inactiveChunkNonEmpty) {
                long inactiveChunkMax = inactiveChunk.max();

                if (inactiveChunkMax > max) {
                    max = inactiveChunkMax;
                }
            }
        }

        double mean = NO_VALUE_DOUBLE;
        double standardDeviation = NO_VALUE_DOUBLE;

        if (withMean || withStandardDeviation) {
            long countForMean = 0L;
            long totalSumForMean = 0L;

            if (activeChunkNonEmpty) {
                long chunkUpdateCount = activeChunk.treeUpdateCount();

                if (chunkUpdateCount > 0L) {
                    countForMean += chunkUpdateCount;
                    totalSumForMean += activeChunk.sum();
                }
            }

            if (inactiveChunkNonEmpty) {
                long chunkUpdateCount = inactiveChunk.treeUpdateCount();

                if (chunkUpdateCount > 0L) {
                    countForMean += chunkUpdateCount;
                    totalSumForMean += inactiveChunk.sum();
                }
            }

            if (countForMean > 0L) {
                mean = (1.0 * totalSumForMean) / countForMean;

                if (withStandardDeviation) {
                    standardDeviationCalculator.reset(mean);

                    if (activeChunkNonEmpty) {
                        activeChunk.traverseUpdateCount(standardDeviationCalculator);
                    }

                    if (inactiveChunkNonEmpty) {
                        inactiveChunk.traverseUpdateCount(standardDeviationCalculator);
                    }

                    standardDeviation = standardDeviationCalculator.standardDeviation(countForMean);
                }
            }
        }

        long[] bucketSizes = null;

        if (withBuckets) {
            bucketSizes = new long[bucketUpperBounds.length];

            if (activeChunkNonEmpty) {
                activeChunk.addBucketSizesTo(bucketSizes);
            }

            if (inactiveChunkNonEmpty) {
                inactiveChunk.addBucketSizesTo(bucketSizes);
            }
        }

        double[] percentileValues = null;

        if (withPercentiles) {
            percentileValues = new double[quantiles.length];

            for (int i = 0; i < quantiles.length; i++) {
                if (multiNode == null) {
                    multiNode = new DoubleNode(activeChunk, inactiveChunk);
                } else {
                    multiNode.reset(activeChunk, inactiveChunk);
                }

                percentileValues[i] = calcPercentile(quantiles[i], multiNode);
            }
        }

        return new DefaultXHistogramSnapshot(
            count,
            totalSum,
            min,
            max,
            mean,
            standardDeviation,
            quantiles,
            percentileValues,
            bucketUpperBounds,
            bucketSizes);
    }
}
