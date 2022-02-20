package com.ringcentral.platform.metrics.defaultImpl.histogram;

import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.TotalsMeasurementType;
import com.ringcentral.platform.metrics.histogram.Histogram.Bucket;

import java.util.Collection;
import java.util.concurrent.atomic.LongAdder;

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
    private final LongAdder[] intervalCounters;
    private final LongAdder[] intervalSumAdders;
    private final LongAdder[] intervalUpdateCounters;
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
        this.intervalCounters = new LongAdder[bucketCount];

        for (int i = 0; i < bucketCount; ++i) {
            this.intervalCounters[i] = new LongAdder();
        }

        if (withTotalSum) {
            this.intervalSumAdders = new LongAdder[bucketCount];

            for (int i = 0; i < bucketCount; ++i) {
                this.intervalSumAdders[i] = new LongAdder();
            }

            if (totalsMeasurementType == TotalsMeasurementType.CONSISTENT) {
                this.intervalUpdateCounters = new LongAdder[bucketCount];

                for (int i = 0; i < bucketCount; ++i) {
                    this.intervalUpdateCounters[i] = new LongAdder();
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
        intervalCounters[i].increment();

        if (intervalSumAdders != null) {
            intervalSumAdders[i].add(value);

            if (intervalUpdateCounters != null) {
                intervalUpdateCounters[i].increment();
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
                        intervalUpdateCount = intervalUpdateCounters[i].sum();

                        // We must read the intervalCount last to ensure the consistency of the values.
                        intervalSum = intervalSumAdders[i].sum();
                        intervalCount = intervalCounters[i].sum();
                    } while (intervalCount != intervalUpdateCount);
                } else {
                    intervalCount = intervalCounters[i].sum();
                    intervalSum = intervalSumAdders[i].sum();
                }

                snapshotBucketSizes[i] = i > 0 ? snapshotBucketSizes[i - 1] + intervalCount : intervalCount;
                totalSum += intervalSum;
            }
        } else {
            snapshotBucketSizes[0] = intervalCounters[0].sum();

            for (int i = 1; i < bucketCount; ++i) {
                snapshotBucketSizes[i] = snapshotBucketSizes[i - 1] + intervalCounters[i].sum();
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
