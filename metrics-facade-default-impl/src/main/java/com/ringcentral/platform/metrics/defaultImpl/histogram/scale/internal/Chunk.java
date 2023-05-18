package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.defaultImpl.histogram.DefaultHistogramSnapshot;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfig;

import java.util.concurrent.atomic.LongAdder;

import static com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot.NO_VALUE;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot.NO_VALUE_DOUBLE;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal.ScaleTree.*;

public abstract class Chunk {

    private static final long NO_SNAPSHOT_SUM = -1L;

    protected final int maxLazyTreeLevel;
    protected final int upperLazyTreeLevel;

    protected final boolean withCount;
    protected final boolean withTotalSum;
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

    private final LongAdder sumAdder;
    private volatile long snapshotSum = NO_SNAPSHOT_SUM;

    private final StandardDeviationCalculator standardDeviationCalculator;

    protected Chunk(
        ScaleHistogramImplConfig config,
        MeasurementSpec measurementSpec,
        boolean resettable) {

        this.maxLazyTreeLevel = config.maxLazyTreeLevel();
        this.upperLazyTreeLevel = this.maxLazyTreeLevel >= 0 ? this.maxLazyTreeLevel + 1 : -1;

        this.withCount = measurementSpec.isWithCount();
        this.withTotalSum = measurementSpec.isWithTotalSum();
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
            withPercentiles ? config.scaleSplitFactorForPercentiles() : 1,
            this.bucketUpperBounds,
            this.maxLazyTreeLevel,
            resettable,
            INITIAL_SNAPSHOT_NUM + 1L);

        this.lazySubtreeUpdateCounts =
            this.maxLazyTreeLevel >= 0 ?
            new long[(2 << (this.maxLazyTreeLevel + 1)) - 1] :
            null;

        this.sumAdder =
            (this.withTotalSum || this.withMean || this.withStandardDeviation) ?
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
        updateTree(node, snapshotInProgress, snapshotNum);
        updateSum(value, snapshotInProgress);
    }

    protected abstract void updateTree(ScaleTreeNode node, boolean snapshotInProgress, long snapshotNum);

    protected void updateSum(long value, boolean snapshotInProgress) {
        if (sumAdder != null) {
            if (snapshotInProgress && snapshotSum == NO_SNAPSHOT_SUM) {
                long sum = sumAdder.sum();

                if (snapshotSum == NO_SNAPSHOT_SUM) {
                    snapshotSum = sum;
                }
            }

            sumAdder.add(value);
        }
    }

    public void startSnapshot() {
        tree.startSnapshot();
    }

    public void endSnapshot() {
        tree.endSnapshot();
        resetSnapshotSum();
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

    public HistogramSnapshot snapshot(long currCount, long currTotalSum) {
        long treeUpdateCount = treeUpdateCount();

        if (treeUpdateCount == 0L) {
            return new DefaultHistogramSnapshot(
                currCount,
                currTotalSum,
                NO_VALUE,
                NO_VALUE,
                NO_VALUE,
                NO_VALUE,
                null,
                null,
                null,
                null);
        }

        long min = withMin ? min() : NO_VALUE;
        long max = withMax ? max() : NO_VALUE;

        double mean = NO_VALUE_DOUBLE;
        double standardDeviation = NO_VALUE_DOUBLE;
        long sum = NO_VALUE;

        if (withTotalSum || withMean || withStandardDeviation) {
            sum = sum();

            if (withMean || withStandardDeviation) {
                mean = (1.0 * sum) / treeUpdateCount;

                if (withStandardDeviation) {
                    standardDeviationCalculator.reset(mean);

                    standardDeviation = tree.standardDeviation(
                        this::subtreeUpdateCountFor,
                        standardDeviationCalculator,
                        treeUpdateCount,
                        mean);
                }
            }
        }

        long[] bucketSizes =
            withBuckets ?
            tree.bucketSizes(bucketUpperBounds, this::subtreeUpdateCountFor) :
            null;

        double[] percentileValues =
            withPercentiles ?
            tree.percentileValues(quantiles, this::subtreeUpdateCountFor) :
            null;

        return new DefaultHistogramSnapshot(
            withCount ? currCount + treeUpdateCount : NO_VALUE,
            withTotalSum ? currTotalSum + sum : NO_VALUE,
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

    public long sum() {
        long sum = sumAdder.sum();

        if (snapshotSum != NO_SNAPSHOT_SUM) {
            sum = snapshotSum;
        }

        return sum;
    }

    public void resetSum() {
        if (sumAdder != null) {
            sumAdder.add(-sumAdder.sum());
            resetSnapshotSum();
        }
    }

    public void resetSnapshotSum() {
        snapshotSum = NO_SNAPSHOT_SUM;
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
