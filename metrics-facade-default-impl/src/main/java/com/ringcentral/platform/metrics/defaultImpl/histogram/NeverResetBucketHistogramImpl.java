package com.ringcentral.platform.metrics.defaultImpl.histogram;

import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.TotalsMeasurementType;
import com.ringcentral.platform.metrics.defaultImpl.histogram.totals.*;
import com.ringcentral.platform.metrics.histogram.Histogram.Bucket;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Supplier;

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
    private final TotalsHistogramImpl[] totalsHistograms;
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

        Supplier<TotalsHistogramImpl> totalsHistogramSupplier = totalsHistogramSupplier(withTotalSum, totalsMeasurementType);
        this.totalsHistograms = new TotalsHistogramImpl[bucketCount];

        for (int i = 0; i < bucketCount; ++i) {
            this.totalsHistograms[i] = totalsHistogramSupplier.get();
        }

        this.snapshotBucketSizes = new long[bucketCount];
    }

    @Nonnull
    private static Supplier<TotalsHistogramImpl> totalsHistogramSupplier(boolean withTotalSum, @Nonnull TotalsMeasurementType totalsMeasurementType) {
        if (withTotalSum) {
            return
                totalsMeasurementType == TotalsMeasurementType.CONSISTENT ?
                ConsistentTotalsHistogramImpl::new :
                EventuallyConsistentTotalsHistogramImpl::new;
        } else {
            return CountHistogramImpl::new;
        }
    }

    @Override
    public void update(long value) {
        int i = bucketCount > 1 ? bucketIndexFor(value) : 0;
        totalsHistograms[i].update(value);
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

    private final MutableTotalsHistogramSnapshot intervalTotals = new MutableTotalsHistogramSnapshot();

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
                totalsHistograms[i].fillSnapshot(intervalTotals);
                intervalCount = intervalTotals.count();
                intervalSum = intervalTotals.totalSum();
                snapshotBucketSizes[i] = i > 0 ? snapshotBucketSizes[i - 1] + intervalCount : intervalCount;
                totalSum += intervalSum;
            }
        } else {
            totalsHistograms[0].fillSnapshot(intervalTotals);
            snapshotBucketSizes[0] = intervalTotals.count();

            for (int i = 1; i < bucketCount; ++i) {
                totalsHistograms[i].fillSnapshot(intervalTotals);
                snapshotBucketSizes[i] = snapshotBucketSizes[i - 1] + intervalTotals.count();
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
