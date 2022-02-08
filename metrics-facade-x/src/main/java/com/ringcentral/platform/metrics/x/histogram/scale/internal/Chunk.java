package com.ringcentral.platform.metrics.x.histogram.scale.internal;

import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.x.histogram.*;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig;
import com.ringcentral.platform.metrics.x.histogram.scale.internal.ScaleTree.*;

import java.util.concurrent.atomic.LongAdder;

import static com.ringcentral.platform.metrics.x.histogram.XHistogramSnapshot.NO_VALUE;

public abstract class Chunk {

    private static final long NO_SNAPSHOT_TOTAL_SUM = -1L;

    protected final int maxLazyTreeLevel;
    protected final int upperLazyTreeLevel;

    protected final boolean withMin;
    protected final boolean withMax;
    protected final boolean withMean;
    protected final boolean withStandardDeviation;
    protected final boolean withPercentiles;
    protected final double[] quantiles;
    protected final boolean withBuckets;
    protected final long[] bucketUpperBounds;

    protected final ScaleTree tree;
    private final long[] lazySubtreeUpdateCounts;

    private final LongAdder totalSumAdder;
    private volatile long snapshotTotalSum = NO_SNAPSHOT_TOTAL_SUM;

    private final StandardDeviationCalculator standardDeviationCalculator;

    protected Chunk(
        ScaleXHistogramImplConfig config,
        MeasurementSpec measurementSpec,
        boolean resettable) {

        this.maxLazyTreeLevel = config.maxLazyTreeLevel();
        this.upperLazyTreeLevel = this.maxLazyTreeLevel >= 0 ? this.maxLazyTreeLevel + 1 : -1;

        this.withMin = measurementSpec.isWithMin();
        this.withMax = measurementSpec.isWithMax();
        this.withMean = measurementSpec.isWithMean();
        this.withStandardDeviation = measurementSpec.isWithStandardDeviation();
        this.withPercentiles = measurementSpec.isWithPercentiles();
        this.quantiles = measurementSpec.quantiles();
        this.withBuckets = measurementSpec.isWithBuckets();
        this.bucketUpperBounds = measurementSpec.bucketUpperBounds();

        this.tree = ScaleTree.of(
            config.scale(),
            this.bucketUpperBounds,
            this.maxLazyTreeLevel,
            resettable,
            Long.MIN_VALUE + 1L);

        this.lazySubtreeUpdateCounts =
            this.maxLazyTreeLevel >= 0 ?
            new long[(2 << (this.maxLazyTreeLevel + 1)) - 1] :
            null;

        this.totalSumAdder =
            (this.withMean || this.withStandardDeviation) ?
            new LongAdder() :
            null;

        this.standardDeviationCalculator = new StandardDeviationCalculator();
    }

    public ScaleTree tree() {
        return tree;
    }

    public void startUpdateEpoch() {
        tree.startUpdateEpoch();
    }

    public void update(long value) {
        update(value, tree.snapshotInProgress, tree.snapshotNum);
    }

    public void update(long value, boolean snapshotInProgress, long snapshotNum) {
        ScaleTreeNode node = tree.nodeForValue(value);
        long point = node.point;
        updateTree(node, snapshotInProgress, snapshotNum);
        updateTotalSum(point, snapshotInProgress);
    }

    protected abstract void updateTree(ScaleTreeNode node, boolean snapshotInProgress, long snapshotNum);

    protected void updateTotalSum(long point, boolean snapshotInProgress) {
        if (totalSumAdder != null) {
            if (snapshotInProgress && snapshotTotalSum == NO_SNAPSHOT_TOTAL_SUM) {
                long totalSum = totalSumAdder.sum();

                if (snapshotTotalSum == NO_SNAPSHOT_TOTAL_SUM) {
                    snapshotTotalSum = totalSum;
                }
            }

            totalSumAdder.add(point);
        }
    }

    public void startSnapshot() {
        tree.startSnapshot();
    }

    public void endSnapshot() {
        tree.endSnapshot();
    }

    public void calcLazySubtreeUpdateCounts() {
        if (maxLazyTreeLevel >= 0) {
            tree.traversePostOrder(this::calcLazySubtreeUpdateCountFor, maxLazyTreeLevel);
        }
    }

    private void calcLazySubtreeUpdateCountFor(ScaleTreeNode node) {
        lazySubtreeUpdateCounts[node.levelOrderIndex] =
            subtreeUpdateCountFor(node, false)
            + subtreeUpdateCountFor(node.left, true)
            + subtreeUpdateCountFor(node.right, true);
    }

    protected long lazySubtreeUpdateCountFor(ScaleTreeNode node) {
        return lazySubtreeUpdateCounts[node.levelOrderIndex];
    }

    public XHistogramSnapshot snapshot() {
        long min = withMin ? min() : NO_VALUE;
        long max = withMax ? max() : NO_VALUE;

        double mean = NO_VALUE;
        double standardDeviation = NO_VALUE;

        if (withMean || withStandardDeviation) {
            long treeUpdateCount = treeUpdateCount();

            if (treeUpdateCount > 0L) {
                long totalSum = totalSum();
                mean = (1.0 * totalSum) / treeUpdateCount;
                standardDeviationCalculator.reset(mean);

                if (withStandardDeviation) {
                    standardDeviation = tree.standardDeviation(
                        this::subtreeUpdateCountFor,
                        standardDeviationCalculator,
                        treeUpdateCount,
                        mean);
                }
            }
        }

        long[] bucketSizes = withBuckets ? tree.bucketSizes(bucketUpperBounds, this::subtreeUpdateCountFor) : null;
        double[] percentileValues = withPercentiles ? tree.percentileValues(quantiles, this::subtreeUpdateCountFor) : null;

        return new DefaultXHistogramSnapshot(
            NO_VALUE,
            NO_VALUE,
            min,
            max,
            mean,
            standardDeviation,
            quantiles,
            percentileValues,
            bucketUpperBounds,
            bucketSizes);
    }

    public long min() {
        return tree.min(this::subtreeUpdateCountFor);
    }

    public long max() {
        return tree.max(this::subtreeUpdateCountFor);
    }

    public long totalSum() {
        long totalSum = totalSumAdder.sum();

        if (snapshotTotalSum != NO_SNAPSHOT_TOTAL_SUM) {
            totalSum = snapshotTotalSum;
        }

        return totalSum;
    }

    public void resetTotalSum() {
        if (totalSumAdder != null) {
            totalSumAdder.add(-totalSumAdder.sum());
            resetSnapshotTotalSum();
        }
    }

    public void resetSnapshotTotalSum() {
        snapshotTotalSum = NO_SNAPSHOT_TOTAL_SUM;
    }

    public void addBucketSizesTo(long[] bucketSizes) {
        tree.addBucketSizesTo(bucketSizes, bucketUpperBounds, this::subtreeUpdateCountFor);
    }

    public boolean isNonEmpty() {
        return treeUpdateCount() > 0L;
    }

    public long treeUpdateCount() {
        return subtreeUpdateCountFor(tree.root);
    }

    public void traverseUpdateCount(NodeUpdateCountConsumer consumer) {
        tree.traverseUpdateCount(this::subtreeUpdateCountFor, consumer);
    }

    public long subtreeUpdateCountFor(ScaleTreeNode node) {
        return subtreeUpdateCountFor(node, true);
    }

    public abstract long subtreeUpdateCountFor(ScaleTreeNode node, boolean useLazy);
}
