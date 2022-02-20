package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.resetByChunks;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.defaultImpl.histogram.*;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.AbstractExtendedScaleHistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfig;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal.*;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ResetByChunksExtendedScaleHistogramImpl extends AbstractExtendedScaleHistogramImpl {

    private final long chunkResetPeriodMs;
    private final TimeMsProvider timeMsProvider;
    private final ScheduledExecutorService executor;
    private final long creationTimeMs;
    private volatile boolean removed;

    private final Item[] items;
    private volatile Item currItem;
    private final List<Item> snapshotItems;
    private final List<ResetByChunksChunk> nonEmptySnapshotChunks;

    public ResetByChunksExtendedScaleHistogramImpl(
        ScaleHistogramImplConfig config,
        MeasurementSpec measurementSpec,
        ScheduledExecutorService executor,
        TimeMsProvider timeMsProvider) {

        super(config, measurementSpec);

        this.chunkResetPeriodMs = config.chunkResetPeriodMs();
        this.timeMsProvider = timeMsProvider;
        this.executor = executor;
        this.creationTimeMs = timeMsProvider.stableTimeMs();
        int chunkCount = config.chunkCount();
        this.items = new Item[chunkCount];

        for (int i = 0; i < chunkCount; ++i) {
            long chunkStartTimeMs =
                i > 0 ?
                -(config.chunkResetPeriodMs() * config.chunkCount() + 1L) :
                this.creationTimeMs;

            this.items[i] = new Item(config, measurementSpec, chunkStartTimeMs);
        }

        this.currItem = this.items[0];
        this.snapshotItems = new ArrayList<>(chunkCount);
        this.nonEmptySnapshotChunks = new ArrayList<>(chunkCount);
    }

    @Override
    public synchronized void metricInstanceAdded() {
        executor.schedule(this::roll, config.chunkResetPeriodMs(), MILLISECONDS);
    }

    private synchronized void roll() {
        if (removed) {
            return;
        }

        phaser.readerLock();

        try {
            doRoll();
        } finally {
            phaser.readerUnlock();
        }
    }

    private void doRoll() {
        long nowMs = timeMsProvider.stableTimeMs();

        if (nowMs - currItem.startTimeMs >= chunkResetPeriodMs) {
            doRoll(nowMs);
            executor.schedule(this::roll, chunkResetPeriodMs, MILLISECONDS);
        } else {
            executor.schedule(
                this::roll,
                chunkResetPeriodMs - (nowMs - currItem.startTimeMs) + 1L,
                MILLISECONDS);
        }
    }

    private void doRoll(long nowMs) {
        int newItemIndex = (int)(((nowMs - creationTimeMs) / chunkResetPeriodMs) % items.length);
        Item newItem = items[newItemIndex];
        newItem.reset(nowMs, newItem == currItem);
        currItem = newItem;
    }

    @Override
    public synchronized void metricInstanceRemoved() {
        removed = true;
    }

    @Override
    public void update(long value) {
        long criticalValueAtEnter = phaser.writerCriticalSectionEnter();

        try {
            currItem.activeChunk.update(value);
        } finally {
            phaser.writerCriticalSectionExit(criticalValueAtEnter);
        }
    }

    @Override
    public synchronized HistogramSnapshot snapshot() {
        phaser.readerLock();

        try {
            return doSnapshot();
        } finally {
            phaser.readerUnlock();
        }
    }

    private HistogramSnapshot doSnapshot() {
        long nowMs = timeMsProvider.stableTimeMs();
        snapshotItems.clear();
        nonEmptySnapshotChunks.clear();

        for (Item item : items) {
            boolean needsToBeSnapshoted = item.needsToBeSnapshoted(nowMs);

            if (withCount || withTotalSum || needsToBeSnapshoted) {
                if (item == currItem) {
                    item.inactiveChunk.startSnapshot();
                    item.flipChunks();
                } else {
                    item.activeChunk.startSnapshot();
                }

                item.inactiveChunk.startSnapshot();

                if (hasLazyTreeLevels()) {
                    if (item == currItem || !item.lazySubtreeUpdateCountsCalculated) {
                        item.calcLazySubtreeUpdateCounts();

                        if (item != currItem) {
                            item.lazySubtreeUpdateCountsCalculated = true;
                        }
                    }
                }

                if (needsToBeSnapshoted) {
                    snapshotItems.add(item);

                    if (item.activeChunk.isNonEmpty()) {
                        nonEmptySnapshotChunks.add(item.activeChunk);
                    }

                    if (item.inactiveChunk.isNonEmpty()) {
                        nonEmptySnapshotChunks.add(item.inactiveChunk);
                    }
                }
            }
        }

        HistogramSnapshot snapshot = takeSnapshot();

        if (withCount || withTotalSum) {
            for (Item item : items) {
                endSnapshotFor(item);
            }
        } else {
            for (Item item : snapshotItems) {
                endSnapshotFor(item);
            }
        }

        return snapshot;
    }

    private void endSnapshotFor(Item item) {
        if (item == currItem) {
            item.inactiveChunk.endSnapshot();
            item.inactiveChunk.resetSnapshotSum();
            item.flipChunks();
        } else {
            item.activeChunk.endSnapshot();
            item.activeChunk.resetSnapshotSum();
        }

        item.inactiveChunk.endSnapshot();
        item.inactiveChunk.resetSnapshotSum();
    }

    @SuppressWarnings("ConstantConditions")
    private HistogramSnapshot takeSnapshot() {
        long count = NO_VALUE;

        if (withCount) {
            count = 0L;

            for (Item item : items) {
                count += item.count();
            }
        }

        long totalSum = NO_VALUE;

        if (withTotalSum) {
            totalSum = 0L;

            for (Item item : items) {
                totalSum += item.totalSum();
            }
        }

        if (nonEmptySnapshotChunks.isEmpty()) {
            return new DefaultHistogramSnapshot(
                count,
                totalSum,
                NO_VALUE,
                NO_VALUE,
                NO_VALUE,
                NO_VALUE,
                null,
                null,
                null,
                null);
        }

        long min = NO_VALUE;

        if (withMin) {
            min = Long.MAX_VALUE;

            for (ResetByChunksChunk chunk : nonEmptySnapshotChunks) {
                long chunkMin = chunk.min();

                if (chunkMin < min) {
                    min = chunkMin;
                }
            }
        }

        long max = NO_VALUE;

        if (withMax) {
            max = Long.MIN_VALUE;

            for (ResetByChunksChunk chunk : nonEmptySnapshotChunks) {
                long chunkMax = chunk.max();

                if (chunkMax > max) {
                    max = chunkMax;
                }
            }
        }

        double mean = NO_VALUE_DOUBLE;
        double standardDeviation = NO_VALUE_DOUBLE;

        if (withMean || withStandardDeviation) {
            long countForMean = 0L;
            long totalSumForMean = 0L;

            for (ResetByChunksChunk chunk : nonEmptySnapshotChunks) {
                long chunkUpdateCount = chunk.treeUpdateCount();

                if (chunkUpdateCount > 0L) {
                    countForMean += chunkUpdateCount;
                    totalSumForMean += chunk.sum();
                }
            }

            if (countForMean > 0L) {
                mean = (1.0 * totalSumForMean) / countForMean;

                if (withStandardDeviation) {
                    standardDeviationCalculator.reset(mean);

                    for (ResetByChunksChunk chunk : nonEmptySnapshotChunks) {
                        chunk.traverseUpdateCount(standardDeviationCalculator);
                    }

                    standardDeviation = standardDeviationCalculator.standardDeviation(countForMean);
                }
            }
        }

        long[] bucketSizes = null;

        if (withBuckets) {
            bucketSizes = new long[bucketUpperBounds.length];

            for (ResetByChunksChunk chunk : nonEmptySnapshotChunks) {
                chunk.addBucketSizesTo(bucketSizes);
            }
        }

        double[] percentileValues = null;

        if (withPercentiles) {
            percentileValues = new double[quantiles.length];

            for (int i = 0; i < quantiles.length; i++) {
                MultiNode multiNode =
                    nonEmptySnapshotChunks.size() == 2 ?
                    new DoubleNode(nonEmptySnapshotChunks.get(0), nonEmptySnapshotChunks.get(1)) :
                    new DefaultMultiNode(nonEmptySnapshotChunks);

                percentileValues[i] = calcPercentile(quantiles[i], multiNode);
            }
        }

        return new DefaultHistogramSnapshot(
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

    private class Item {

        final long allChunksResetPeriodMs;
        volatile long startTimeMs;
        volatile ResetByChunksChunk activeChunk;
        volatile ResetByChunksChunk inactiveChunk;
        boolean lazySubtreeUpdateCountsCalculated;
        long retainedCount;
        long retainedTotalSum;

        Item(
            ScaleHistogramImplConfig config,
            MeasurementSpec measurementSpec,
            long startTimeMs) {

            this.allChunksResetPeriodMs = config.chunkResetPeriodMs() * config.chunkCount();
            this.startTimeMs = startTimeMs;
            this.activeChunk = new ResetByChunksChunk(config, measurementSpec);
            this.inactiveChunk = new ResetByChunksChunk(config, measurementSpec);
        }

        void calcLazySubtreeUpdateCounts() {
            activeChunk.calcLazySubtreeUpdateCounts();
            inactiveChunk.calcLazySubtreeUpdateCounts();
        }

        boolean needsToBeSnapshoted(long nowMs) {
            return nowMs - startTimeMs < allChunksResetPeriodMs;
        }

        void reset(long startTimeMs, boolean inPlace) {
            this.startTimeMs = startTimeMs;
            lazySubtreeUpdateCountsCalculated = false;

            retainedCount += inactiveChunk.treeUpdateCount();
            retainedTotalSum += inactiveChunk.sum();

            inactiveChunk.startUpdateEpoch();
            inactiveChunk.resetSum();

            if (inPlace) {
                flipChunks();

                retainedCount += inactiveChunk.treeUpdateCount();
                retainedTotalSum += inactiveChunk.sum();

                inactiveChunk.startUpdateEpoch();
                inactiveChunk.resetSum();
            } else {
                retainedCount += activeChunk.treeUpdateCount();
                retainedTotalSum += activeChunk.sum();

                activeChunk.startUpdateEpoch();
                activeChunk.resetSum();
            }
        }

        void flipChunks() {
            ResetByChunksChunk tempChunk = inactiveChunk;
            inactiveChunk = activeChunk;
            activeChunk = tempChunk;
            flipPhase();
        }

        public long count() {
            return retainedCount + activeChunk.treeUpdateCount() + inactiveChunk.treeUpdateCount();
        }

        public long totalSum() {
            return retainedTotalSum + activeChunk.sum() + inactiveChunk.sum();
        }
    }
}