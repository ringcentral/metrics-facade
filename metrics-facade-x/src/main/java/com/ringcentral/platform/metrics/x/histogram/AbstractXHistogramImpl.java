package com.ringcentral.platform.metrics.x.histogram;

import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.histogram.Histogram.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.x.histogram.configs.*;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.LongAdder;

import static com.ringcentral.platform.metrics.x.histogram.DefaultXHistogramSnapshot.NO_VALUE;
import static java.util.stream.Collectors.toSet;

public abstract class AbstractXHistogramImpl implements XHistogramImpl {

    public static class MeasurementSpec {

        private final Set<? extends Measurable> measurables;
        private final boolean withCount;
        private final boolean withTotalSum;
        private final boolean withMin;
        private final boolean withMax;
        private final boolean withMean;
        private final boolean withStandardDeviation;
        private final boolean withPercentiles;
        private final double[] quantiles;
        private final double[] percentiles;
        private final boolean withBuckets;
        private final long[] bucketUpperBounds;

        public MeasurementSpec(
            Set<? extends Measurable> measurables,
            boolean withCount,
            boolean withTotalSum,
            boolean withMin,
            boolean withMax,
            boolean withMean,
            boolean withStandardDeviation,
            boolean withPercentiles,
            double[] quantiles,
            double[] percentiles,
            boolean withBuckets,
            long[] bucketUpperBounds) {

            this.measurables = measurables;
            this.withCount = withCount;
            this.withTotalSum = withTotalSum;
            this.withMin = withMin;
            this.withMax = withMax;
            this.withMean = withMean;
            this.withStandardDeviation = withStandardDeviation;
            this.withPercentiles = withPercentiles;
            this.quantiles = quantiles;
            this.percentiles = percentiles;
            this.withBuckets = withBuckets;
            this.bucketUpperBounds = bucketUpperBounds;
        }

        public Set<? extends Measurable> measurables() {
            return measurables;
        }

        public boolean isWithCount() {
            return withCount;
        }

        public boolean isWithTotalSum() {
            return withTotalSum;
        }

        public boolean isWithMin() {
            return withMin;
        }

        public boolean isWithMax() {
            return withMax;
        }

        public boolean isWithMean() {
            return withMean;
        }

        public boolean isWithStandardDeviation() {
            return withStandardDeviation;
        }

        public boolean isWithPercentiles() {
            return withPercentiles;
        }

        public double[] quantiles() {
            return quantiles;
        }

        public double[] percentiles() {
            return percentiles;
        }

        public boolean isWithBuckets() {
            return withBuckets;
        }

        public long[] bucketUpperBounds() {
            return bucketUpperBounds;
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static class ExtendedImplInfo {

        private final boolean supportsTotals;
        private final BucketsMeasurementType bucketsMeasurementType;

        public ExtendedImplInfo(
            boolean supportsTotals,
            BucketsMeasurementType bucketsMeasurementType) {

            this.supportsTotals = supportsTotals;
            this.bucketsMeasurementType = bucketsMeasurementType;
        }

        public boolean supportsTotals() {
            return supportsTotals;
        }

        public BucketsMeasurementType bucketsMeasurementType() {
            return bucketsMeasurementType;
        }
    }

    public interface ExtendedImplMaker {
        XHistogramImpl makeExtendedImpl(MeasurementSpec measurementSpec);
    }

    private final XHistogramImpl parent;
    protected ScheduledExecutorService executor;

    protected AbstractXHistogramImpl(
        XHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ExtendedImplInfo extendedImplInfo,
        ExtendedImplMaker extendedImplMaker,
        ScheduledExecutorService executor) {

        measurables = new HashSet<>(measurables);
        boolean withCount = measurables.stream().anyMatch(m -> m instanceof Count);
        boolean withTotalSum = measurables.stream().anyMatch(m -> m instanceof TotalSum);
        boolean withMin = measurables.stream().anyMatch(m -> m instanceof Min);
        boolean withMax = measurables.stream().anyMatch(m -> m instanceof Max);
        boolean withMean = measurables.stream().anyMatch(m -> m instanceof Mean);
        boolean withStandardDeviation = measurables.stream().anyMatch(m -> m instanceof StandardDeviation);
        boolean withPercentiles;
        double[] quantiles;
        double[] percentiles;
        boolean withBuckets;
        long[] bucketUpperBounds;

        if (measurables.stream().anyMatch(m -> m instanceof Percentile)) {
            withPercentiles = true;

            quantiles = measurables.stream()
                .filter(m -> m instanceof Percentile)
                .mapToDouble(m -> ((Percentile)m).quantile())
                .sorted()
                .toArray();

            percentiles = measurables.stream()
                .filter(m -> m instanceof Percentile)
                .mapToDouble(m -> ((Percentile)m).percentile())
                .sorted()
                .toArray();
        } else {
            withPercentiles = false;
            quantiles = null;
            percentiles = null;
        }

        Set<Bucket> buckets = measurables.stream()
            .filter(m -> m instanceof Bucket)
            .map(m -> (Bucket)m)
            .collect(toSet());

        XHistogramImpl basic = null;

        boolean extendedRequired =
            withMin
            || withMax
            || withMean
            || withStandardDeviation
            || withPercentiles;

        if (buckets.isEmpty()) {
            if (!extendedRequired || !extendedImplInfo.supportsTotals()) {
                basic = makeTotalsImpl(config, withCount, withTotalSum);
                withCount = false;
                withTotalSum = false;
            }

            withBuckets = false;
            bucketUpperBounds = null;
        } else if (config.bucketsMeasurementType() == BucketsMeasurementType.RESETTABLE) {
            if (!extendedImplInfo.supportsTotals()) {
                basic = makeTotalsImpl(config, withCount, withTotalSum);
            }

            withBuckets = true;
            bucketUpperBounds = upperBoundsOf(buckets);
        } else {
            if (!extendedRequired || extendedImplInfo.bucketsMeasurementType() != BucketsMeasurementType.NEVER_RESET) {
                basic = new NeverResetBucketXHistogramImpl(
                    withCount && (!extendedRequired || !extendedImplInfo.supportsTotals()),
                    withTotalSum && (!extendedRequired || !extendedImplInfo.supportsTotals()),
                    config.totalsMeasurementType(),
                    buckets);

                withBuckets = false;
                bucketUpperBounds = null;
            } else {
                if (!extendedImplInfo.supportsTotals()) {
                    basic = makeTotalsImpl(config, withCount, withTotalSum);
                    withCount = false;
                    withTotalSum = false;
                }

                withBuckets = true;
                bucketUpperBounds = upperBoundsOf(buckets);
            }
        }

        XHistogramImpl extended = null;

        if (extendedRequired || withBuckets) {
            MeasurementSpec measurementSpec = new MeasurementSpec(
                Set.copyOf(measurables),
                extendedImplInfo.supportsTotals && withCount,
                extendedImplInfo.supportsTotals && withTotalSum,
                withMin,
                withMax,
                withMean,
                withStandardDeviation,
                withPercentiles,
                quantiles,
                percentiles,
                withBuckets,
                bucketUpperBounds);

            extended = extendedImplMaker.makeExtendedImpl(measurementSpec);
        }

        if (basic == null) {
            this.parent = extended;
        } else if (extended != null) {
            boolean bucketsFromBasic = config.bucketsMeasurementType() == BucketsMeasurementType.NEVER_RESET
                && extendedImplInfo.bucketsMeasurementType() != BucketsMeasurementType.NEVER_RESET;

            this.parent = new CombinedImpl(
                basic,
                extended,
                !extendedImplInfo.supportsTotals(),
                bucketsFromBasic);
        } else {
            this.parent = basic;
        }

        this.executor = executor;
    }

    private long[] upperBoundsOf(Set<Bucket> buckets) {
        long[] bounds = buckets.stream()
            .mapToLong(Bucket::upperBoundAsLong)
            .sorted()
            .toArray();

        if (bounds[bounds.length - 1] != Long.MAX_VALUE) {
            long[] boundsWithInf = new long[bounds.length + 1];
            System.arraycopy(bounds, 0, boundsWithInf, 0, bounds.length);
            boundsWithInf[bounds.length] = Long.MAX_VALUE;
            bounds = boundsWithInf;
        }

        return bounds;
    }

    private XHistogramImpl makeTotalsImpl(XHistogramImplConfig config, boolean withCount, boolean withTotalSum) {
        return
            config.totalsMeasurementType() == TotalsMeasurementType.CONSISTENT ?
            new ConsistentTotalsImpl(withCount, withTotalSum) :
            new EventuallyConsistentTotalsImpl(withCount, withTotalSum);
    }

    @Override
    public void update(long value) {
        parent.update(value);
    }

    @Override
    public synchronized XHistogramSnapshot snapshot() {
        return parent.snapshot();
    }

    @Override
    public void metricInstanceAdded() {
        parent.metricInstanceAdded();
    }

    @Override
    public void metricInstanceRemoved() {
        parent.metricInstanceRemoved();
    }

    @SuppressWarnings("ConstantConditions")
    private static class ConsistentTotalsImpl implements XHistogramImpl {

        final LongAdder counter;
        final LongAdder totalSumAdder;
        final LongAdder updateCounter;

        ConsistentTotalsImpl(boolean withCount, boolean withTotalSum) {
            this.counter = withCount ? new LongAdder() : null;

            if (withTotalSum) {
                this.totalSumAdder = new LongAdder();
                this.updateCounter = new LongAdder();
            } else {
                this.totalSumAdder = null;
                this.updateCounter = null;
            }
        }

        @Override
        public void update(long value) {
            if (counter != null) {
                counter.increment();
            }

            if (totalSumAdder != null) {
                totalSumAdder.add(value);
                updateCounter.increment();
            }
        }

        @Override
        public XHistogramSnapshot snapshot() {
            long count = NO_VALUE;
            long totalSum = NO_VALUE;

            if (counter != null && totalSumAdder != null) {
                long updateCount;

                do {
                    updateCount = updateCounter.sum();

                    // We must read the counter last to ensure the consistency of the totals.
                    totalSum = totalSumAdder.sum();
                    count = counter.sum();
                } while (count != updateCount);
            } else if (counter != null) {
                count = counter.sum();
            } else if (totalSumAdder != null) {
                totalSum = totalSumAdder.sum();
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
                null,
                null);
        }
    }

    private static class EventuallyConsistentTotalsImpl implements XHistogramImpl {

        final LongAdder counter;
        final LongAdder totalSumAdder;

        EventuallyConsistentTotalsImpl(boolean withCount, boolean withTotalSum) {
            this.counter = withCount ? new LongAdder() : null;
            this.totalSumAdder = withTotalSum ? new LongAdder() : null;
        }

        @Override
        public void update(long value) {
            if (counter != null) {
                counter.increment();
            }

            if (totalSumAdder != null) {
                totalSumAdder.add(value);
            }
        }

        @Override
        public XHistogramSnapshot snapshot() {
            return new DefaultXHistogramSnapshot(
                counter != null ? counter.sum() : NO_VALUE,
                totalSumAdder != null ? totalSumAdder.sum() : NO_VALUE,
                NO_VALUE,
                NO_VALUE,
                NO_VALUE,
                NO_VALUE,
                null,
                null,
                null,
                null);
        }
    }

    private static class CombinedImpl implements XHistogramImpl {

        final XHistogramImpl basic;
        final XHistogramImpl extended;

        final boolean totalsFromBasic;
        final boolean bucketsFromBasic;

        CombinedImpl(
            XHistogramImpl basic,
            XHistogramImpl extended,
            boolean totalsFromBasic,
            boolean bucketsFromBasic) {

            this.basic = basic;
            this.extended = extended;

            this.totalsFromBasic = totalsFromBasic;
            this.bucketsFromBasic = bucketsFromBasic;
        }

        @Override
        public void update(long value) {
            basic.update(value);
            extended.update(value);
        }

        @Override
        public XHistogramSnapshot snapshot() {
            XHistogramSnapshot extendedSnapshot = extended.snapshot();
            XHistogramSnapshot basicSnapshot = basic.snapshot();

            return new CombinedSnapshot(
                basicSnapshot,
                extendedSnapshot,
                totalsFromBasic,
                bucketsFromBasic);
        }

        @Override
        public void metricInstanceAdded() {
            basic.metricInstanceAdded();
            extended.metricInstanceAdded();
        }

        @Override
        public void metricInstanceRemoved() {
            basic.metricInstanceRemoved();
            extended.metricInstanceRemoved();
        }

        static class CombinedSnapshot implements XHistogramSnapshot {

            final XHistogramSnapshot basicSnapshot;
            final XHistogramSnapshot extendedSnapshot;

            final boolean totalsFromBasic;
            final boolean bucketsFromBasic;

            CombinedSnapshot(
                XHistogramSnapshot basicSnapshot,
                XHistogramSnapshot extendedSnapshot,
                boolean totalsFromBasic,
                boolean bucketsFromBasic) {

                this.basicSnapshot = basicSnapshot;
                this.extendedSnapshot = extendedSnapshot;

                this.totalsFromBasic = totalsFromBasic;
                this.bucketsFromBasic = bucketsFromBasic;
            }

            @Override
            public long count() {
                return (totalsFromBasic ? basicSnapshot : extendedSnapshot).count();
            }

            @Override
            public long totalSum() {
                return (totalsFromBasic ? basicSnapshot : extendedSnapshot).totalSum();
            }

            @Override
            public long min() {
                return extendedSnapshot.min();
            }

            @Override
            public long max() {
                return extendedSnapshot.max();
            }

            @Override
            public double mean() {
                return extendedSnapshot.mean();
            }

            @Override
            public double standardDeviation() {
                return extendedSnapshot.standardDeviation();
            }

            @Override
            public double percentileValue(Percentile percentile) {
                return extendedSnapshot.percentileValue(percentile);
            }

            @Override
            public long bucketSize(Bucket bucket) {
                return (bucketsFromBasic ? basicSnapshot : extendedSnapshot).bucketSize(bucket);
            }
        }
    }
}
