package com.ringcentral.platform.metrics.defaultImpl.histogram;

import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.TotalsMeasurementType;
import com.ringcentral.platform.metrics.histogram.Histogram.Bucket;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import static com.ringcentral.platform.metrics.defaultImpl.histogram.DefaultHistogramSnapshot.NO_VALUE;
import static com.ringcentral.platform.metrics.utils.CollectionUtils.copyLongArray;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.lang.System.arraycopy;
import static java.util.Objects.requireNonNull;

public class NeverResetBucketHistogramImpl implements HistogramImpl {

    private final boolean withCount;
    private final boolean withTotalSum;

    private final long[] bucketUpperBounds;
    private final int bucketCount;
    private final AtomicLong[] intervalCounters;
    private final AtomicLong[] intervalSumAdders;
    private final AtomicLong[] intervalUpdateCounters;
    private final long[] snapshotBucketSizes;

    public NeverResetBucketHistogramImpl(
        boolean withCount,
        boolean withTotalSum,
        TotalsMeasurementType totalsMeasurementType,
        Collection<? extends Bucket> buckets) {

        checkArgument(buckets != null && !buckets.isEmpty(), "No buckets");
        this.withCount = withCount;
        this.withTotalSum = withTotalSum;
        requireNonNull(totalsMeasurementType);

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
        this.bucketCount = bounds.length;
        this.intervalCounters = new AtomicLong[bucketCount];

        for (int i = 0; i < bucketCount; ++i) {
            this.intervalCounters[i] = new AtomicLong();
        }

        if (withTotalSum) {
            this.intervalSumAdders = new AtomicLong[bucketCount];

            for (int i = 0; i < bucketCount; ++i) {
                this.intervalSumAdders[i] = new AtomicLong();
            }

            if (totalsMeasurementType == TotalsMeasurementType.CONSISTENT) {
                this.intervalUpdateCounters = new AtomicLong[bucketCount];

                for (int i = 0; i < bucketCount; ++i) {
                    this.intervalUpdateCounters[i] = new AtomicLong();
                }
            } else {
                this.intervalUpdateCounters = null;
            }
        } else {
            this.intervalSumAdders = null;
            this.intervalUpdateCounters = null;
        }

        this.snapshotBucketSizes = new long[bucketCount];
    }

    @Override
    public void update(long value) {
        int i = bucketCount > 1 ? bucketIndexFor(value) : 0;
        intervalCounters[i].incrementAndGet();

        if (intervalSumAdders != null) {
            intervalSumAdders[i].addAndGet(value);

            if (intervalUpdateCounters != null) {
                intervalUpdateCounters[i].incrementAndGet();
            }
        }
    }

    int bucketIndexFor(long value) {
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
    public synchronized HistogramSnapshot snapshot() {
        long count = NO_VALUE;
        long totalSum = NO_VALUE;

        if (withTotalSum) {
            totalSum = 0L;
            long intervalCount;
            long intervalSum;

            for (int i = 0; i < bucketCount; ++i) {
                if (intervalUpdateCounters != null) {
                    long intervalUpdateCount;

                    do {
                        // See ConsistentTotalsHistogramImpl for details of the algorithm.
                        intervalUpdateCount = intervalUpdateCounters[i].get();
                        intervalSum = intervalSumAdders[i].get();
                        intervalCount = intervalCounters[i].get();
                    } while (intervalCount != intervalUpdateCount);
                } else {
                    intervalCount = intervalCounters[i].get();
                    intervalSum = intervalSumAdders[i].get();
                }

                snapshotBucketSizes[i] = i > 0 ? snapshotBucketSizes[i - 1] + intervalCount : intervalCount;
                totalSum += intervalSum;
            }
        } else {
            snapshotBucketSizes[0] = intervalCounters[0].get();

            for (int i = 1; i < bucketCount; ++i) {
                snapshotBucketSizes[i] = snapshotBucketSizes[i - 1] + intervalCounters[i].get();
            }
        }

        if (withCount) {
            count = snapshotBucketSizes[bucketCount - 1];
        }

        return new DefaultHistogramSnapshot(
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
