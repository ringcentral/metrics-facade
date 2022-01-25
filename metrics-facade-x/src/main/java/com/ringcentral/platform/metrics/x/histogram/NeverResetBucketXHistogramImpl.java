package com.ringcentral.platform.metrics.x.histogram;

import com.ringcentral.platform.metrics.histogram.Histogram.Bucket;

import java.util.Collection;
import java.util.concurrent.atomic.LongAdder;

import static com.ringcentral.platform.metrics.utils.CollectionUtils.copyLongArray;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static com.ringcentral.platform.metrics.x.histogram.DefaultXHistogramSnapshot.NO_VALUE;
import static java.lang.System.arraycopy;

public class NeverResetBucketXHistogramImpl implements XHistogramImpl {

    private final boolean withCount;
    private final boolean withTotalSum;

    private final long[] bucketUpperBounds;
    private final int bucketCount;
    private final LongAdder[] bucketSizes;
    private final long[] snapshotBucketSizes;

    private final LongAdder[] totalSumAdders;
    private final long[] snapshotTotalSums;

    private final LongAdder[] updateCounters;

    public NeverResetBucketXHistogramImpl(
        boolean withCount,
        boolean withTotalSum,
        Collection<? extends Bucket> buckets) {

        checkArgument(buckets != null && !buckets.isEmpty(), "No buckets");
        this.withCount = withCount;
        this.withTotalSum = withTotalSum;

        long[] bounds = buckets.stream()
            .mapToLong(Bucket::upperBoundAsLong)
            .sorted()
            .toArray();

        if (bounds[bounds.length - 1] != Long.MAX_VALUE) {
            long[] boundsWithInf = new long[bounds.length + 1];
            arraycopy(bounds, 0, boundsWithInf, 0, bounds.length);
            boundsWithInf[bounds.length] = Long.MAX_VALUE;
            bounds = boundsWithInf;
        }

        this.bucketUpperBounds = bounds;
        this.bucketCount = this.bucketUpperBounds.length;
        this.bucketSizes = new LongAdder[this.bucketCount];

        for (int i = 0; i < this.bucketCount; ++i) {
            this.bucketSizes[i] = new LongAdder();
        }

        this.snapshotBucketSizes = new long[this.bucketCount];

        if (withTotalSum) {
            this.totalSumAdders = new LongAdder[this.bucketCount];

            for (int i = 0; i < this.bucketCount; ++i) {
                this.totalSumAdders[i] = new LongAdder();
            }

            this.snapshotTotalSums = new long[this.bucketCount];
            this.updateCounters = new LongAdder[this.bucketCount];

            for (int i = 0; i < this.bucketCount; ++i) {
                this.updateCounters[i] = new LongAdder();
            }
        } else {
            this.totalSumAdders = null;
            this.snapshotTotalSums = null;
            this.updateCounters = null;
        }
    }

    @Override
    public void update(long value) {
        int i = bucketCount > 1 ? bucketIndex(value) : 0;
        bucketSizes[i].increment();

        if (totalSumAdders != null) {
            totalSumAdders[i].add(value);
            updateCounters[i].increment();
        }
    }

    int bucketIndex(long value) {
        int low = 0;
        int high = bucketCount - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;

            if (value > bucketUpperBounds[mid]) {
                low = mid + 1;
            } else if (value < bucketUpperBounds[mid]) {
                high = mid - 1;
            } else {
                return mid;
            }
        }

        return low;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public synchronized XHistogramSnapshot snapshot() {
        long count = NO_VALUE;
        long totalSum = NO_VALUE;

        if (withTotalSum) {
            long currSize;
            long currTotalSum;

            for (int i = 0; i < bucketCount; ++i) {
                long updateCount;

                do {
                    updateCount = updateCounters[i].sum();

                    // We must read the size last to ensure the consistency of the values.
                    currTotalSum = totalSumAdders[i].sum();
                    currSize = bucketSizes[i].sum();
                } while (currSize != updateCount);

                snapshotBucketSizes[i] = currSize;
                snapshotTotalSums[i] = currTotalSum;
            }

            for (int i = 1; i < bucketCount; ++i) {
                snapshotBucketSizes[i] += snapshotBucketSizes[i - 1];
            }

            if (withCount) {
                count = snapshotBucketSizes[bucketCount - 1];
            }

            totalSum = 0L;

            for (int i = 0; i < bucketCount; ++i) {
                totalSum += snapshotTotalSums[i];
            }
        } else {
            for (int i = 0; i < bucketCount; ++i) {
                snapshotBucketSizes[i] = bucketSizes[i].sum();
            }

            for (int i = 1; i < bucketCount; ++i) {
                snapshotBucketSizes[i] += snapshotBucketSizes[i - 1];
            }

            if (withCount) {
                count = snapshotBucketSizes[bucketCount - 1];
            }
        }

        return new DefaultXHistogramSnapshot(
            count,
            totalSum,
            NO_VALUE,
            NO_VALUE,
            NO_VALUE,
            NO_VALUE,
            null,
            null,
            bucketUpperBounds,
            copyLongArray(snapshotBucketSizes));
    }
}
