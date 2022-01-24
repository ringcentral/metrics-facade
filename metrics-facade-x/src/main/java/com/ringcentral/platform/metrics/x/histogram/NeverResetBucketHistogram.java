package com.ringcentral.platform.metrics.x.histogram;

import com.ringcentral.platform.metrics.histogram.Histogram.Bucket;

import java.util.Collection;
import java.util.concurrent.atomic.LongAdder;

import static com.ringcentral.platform.metrics.utils.CollectionUtils.copyLongArray;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static com.ringcentral.platform.metrics.x.histogram.DefaultXHistogramSnapshot.NO_VALUE;
import static java.lang.System.arraycopy;

public class NeverResetBucketHistogram {

    private final boolean withCount;
    private final boolean withTotalSum;
    private final boolean withBuckets;

    private final long[] bucketUpperBounds;
    private final int bucketCount;

    private final LongAdder[] bucketSizes;
    private final long[] snapshotBucketSizes;

    private final LongAdder[] totalSumAdders;
    private final long[] snapshotTotalSums;

    public NeverResetBucketHistogram(
        boolean withCount,
        boolean withTotalSum,
        Collection<? extends Bucket> buckets) {

        this.withCount = withCount;
        this.withTotalSum = withTotalSum;
        this.withBuckets = buckets != null && !buckets.isEmpty();

        checkArgument(
            withCount || withTotalSum || withBuckets,
            "No measurables");

        if (withBuckets) {
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
        } else {
            this.bucketUpperBounds = new long[] { Long.MAX_VALUE };
        }

        this.bucketCount = this.bucketUpperBounds.length;

        if (withCount || withBuckets) {
            this.bucketSizes = new LongAdder[this.bucketCount];

            for (int i = 0; i < this.bucketCount; ++i) {
                this.bucketSizes[i] = new LongAdder();
            }

            this.snapshotBucketSizes = new long[this.bucketCount];
        } else {
            this.bucketSizes = null;
            this.snapshotBucketSizes = null;
        }

        if (withTotalSum) {
            this.totalSumAdders = new LongAdder[this.bucketCount];

            for (int i = 0; i < this.bucketCount; ++i) {
                this.totalSumAdders[i] = new LongAdder();
            }

            this.snapshotTotalSums = new long[this.bucketCount];
        } else {
            this.totalSumAdders = null;
            this.snapshotTotalSums = null;
        }
    }

    public void update(long value) {
        int i = bucketCount > 1 ? bucketIndex(value) : 0;

        if (bucketSizes != null) {
            bucketSizes[i].increment();
        }

        if (totalSumAdders != null) {
            totalSumAdders[i].add(value);
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

    @SuppressWarnings("ConstantConditions")
    public synchronized XHistogramSnapshot snapshot() {
        long count = NO_VALUE;
        long totalSum = NO_VALUE;

        if (bucketCount > 1) {
            if (withTotalSum) {
                long currSize;
                long currTotalSum;

                for (int i = 0; i < bucketCount; ++i) {
                    do {
                        currSize = bucketSizes[i].sum();
                        currTotalSum = totalSumAdders[i].sum();
                    } while (currSize != bucketSizes[i].sum());

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
        } else {
            if (bucketSizes != null && withTotalSum) {
                do {
                    count = bucketSizes[0].sum();
                    totalSum = totalSumAdders[0].sum();
                } while (count != bucketSizes[0].sum());
            } else if (bucketSizes != null) {
                count = bucketSizes[0].sum();
            } else if (withTotalSum) {
                totalSum = totalSumAdders[0].sum();
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
                withBuckets ? bucketUpperBounds : null,
                withBuckets ? new long[] { count } : null);
        }
    }
}
