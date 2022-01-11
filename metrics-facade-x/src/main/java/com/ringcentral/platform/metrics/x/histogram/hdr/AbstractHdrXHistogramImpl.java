package com.ringcentral.platform.metrics.x.histogram.hdr;

import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.*;
import org.HdrHistogram.*;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.x.histogram.hdr.HdrXHistogramImplSnapshot.NO_VALUE;
import static java.lang.Math.sqrt;
import static java.util.Arrays.fill;

public abstract class AbstractHdrXHistogramImpl extends AbstractXHistogramImpl implements HdrXHistogramImpl {

    private final long highestTrackableValue;
    private final OverflowBehavior overflowBehavior;
    private final long expectedUpdateInterval;

    private final boolean withMin;
    private final boolean withMax;
    private final boolean withMean;
    private final boolean withStandardDeviation;
    private final double[] quantiles;
    private final double[] percentiles;
    private final double[] bucketUpperBounds;

    protected ScheduledExecutorService executor;

    public AbstractHdrXHistogramImpl(
        HdrXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        super(measurables);

        this.highestTrackableValue = config.highestTrackableValue().orElse(Long.MAX_VALUE);
        this.overflowBehavior = config.overflowBehavior().orElse(null);
        this.expectedUpdateInterval = config.expectedUpdateInterval().orElse(0L);

        this.withMin = measurables.stream().anyMatch(m -> m instanceof Histogram.Min);
        this.withMax = measurables.stream().anyMatch(m -> m instanceof Histogram.Max);
        this.withMean = measurables.stream().anyMatch(m -> m instanceof Histogram.Mean);
        this.withStandardDeviation = measurables.stream().anyMatch(m -> m instanceof Histogram.StandardDeviation);

        if (measurables.stream().anyMatch(m -> m instanceof Histogram.Percentile)) {
            this.quantiles = measurables.stream()
                .filter(m -> m instanceof Histogram.Percentile)
                .mapToDouble(m -> ((Histogram.Percentile)m).quantile())
                .sorted()
                .toArray();

            this.percentiles = measurables.stream()
                .filter(m -> m instanceof Histogram.Percentile)
                .mapToDouble(m -> ((Histogram.Percentile)m).percentile())
                .sorted()
                .toArray();
        } else {
            this.quantiles = null;
            this.percentiles = null;
        }

        if (measurables.stream().anyMatch(m -> m instanceof Histogram.Bucket)) {
            double[] bounds = measurables.stream()
                .filter(m -> m instanceof Histogram.Bucket)
                .mapToDouble(m -> ((Histogram.Bucket)m).upperBound())
                .sorted()
                .toArray();

            if (bounds[bounds.length - 1] != Double.POSITIVE_INFINITY) {
                double[] boundsWithInf = new double[bounds.length + 1];
                System.arraycopy(bounds, 0, boundsWithInf, 0, bounds.length);
                boundsWithInf[bounds.length] = Double.POSITIVE_INFINITY;
                bounds = boundsWithInf;
            }

            this.bucketUpperBounds = bounds;
        } else {
            this.bucketUpperBounds = null;
        }

        this.executor = executor;
    }

    protected static Recorder makeRecorder(HdrXHistogramImplConfig config) {
        if (config.highestTrackableValue().isPresent()) {
            return
                config.lowestDiscernibleValue().isPresent() ?
                new Recorder(config.lowestDiscernibleValue().get(), config.highestTrackableValue().get(), config.significantDigitCount()) :
                new Recorder(config.highestTrackableValue().get(), config.significantDigitCount());
        } else {
            return new Recorder(config.significantDigitCount());
        }
    }

    @Override
    protected void updateImpl(long value) {
        if (value > highestTrackableValue) {
            if (overflowBehavior == OverflowBehavior.REDUCE_TO_HIGHEST_TRACKABLE) {
                value = highestTrackableValue;
            } else {
                return;
            }
        }

        updateWithExpectedInterval(value, expectedUpdateInterval);
    }

    protected abstract void updateWithExpectedInterval(long value, long expectedInterval);

    @Override
    public synchronized HdrXHistogramImplSnapshot snapshot() {
        org.HdrHistogram.Histogram h = hdrHistogramForSnapshot();

        long min = withMin ? h.getMinValue() : NO_VALUE;
        long max = withMax ? h.getMaxValue() : NO_VALUE;
        double mean = NO_VALUE;
        double standardDeviation = NO_VALUE;
        double[] percentileValues = percentiles != null ? new double[percentiles.length] : null;
        long[] bucketSizes = bucketUpperBounds != null ? new long[bucketUpperBounds.length] : null;

        if (!(withMean || withStandardDeviation || percentileValues != null || bucketSizes != null)) {
            return new HdrXHistogramImplSnapshot(
                min,
                max,
                mean,
                standardDeviation,
                quantiles,
                percentileValues,
                bucketUpperBounds,
                bucketSizes);
        }

        if (h.getTotalCount() > 0L) {
            double totalForMean = 0.0;
            int percentileIndex = 0;
            int bucketIndex = 0;
            RecordedValuesIterator valuesIter = new RecordedValuesIterator(h);
            HistogramIterationValue iterValue = null;

            while (valuesIter.hasNext()) {
                iterValue = valuesIter.next();

                // mean and standard deviation
                if (withMean || withStandardDeviation) {
                    totalForMean += h.medianEquivalentValue(iterValue.getValueIteratedTo()) * (double)iterValue.getCountAtValueIteratedTo();
                }

                // percentiles
                if (percentileValues != null) {
                    double p = iterValue.getPercentile();
                    long v = iterValue.getValueIteratedTo();

                    while (percentileIndex < percentileValues.length && percentiles[percentileIndex] <= p) {
                        percentileValues[percentileIndex] = v;
                        ++percentileIndex;
                    }
                }

                // buckets
                if (bucketSizes != null) {
                    double v = iterValue.getValueIteratedTo();

                    while (bucketIndex < bucketSizes.length && bucketUpperBounds[bucketIndex] <= v) {
                        bucketSizes[bucketIndex] = iterValue.getTotalCountToThisValue();
                        ++bucketIndex;
                    }
                }
            }

            // mean and standard deviation
            if (withMean || withStandardDeviation) {
                mean = totalForMean / h.getTotalCount();

                if (withStandardDeviation) {
                    double geometricDeviationTotal = 0.0;
                    RecordedValuesIterator valuesIter2 = new RecordedValuesIterator(h);

                    while (valuesIter2.hasNext()) {
                        HistogramIterationValue iterValue2 = valuesIter2.next();
                        double deviation = (h.medianEquivalentValue(iterValue2.getValueIteratedTo()) * 1.0) - mean;
                        geometricDeviationTotal += (deviation * deviation) * iterValue2.getCountAddedInThisIterationStep();
                    }

                    standardDeviation = sqrt(geometricDeviationTotal / h.getTotalCount());
                }
            }

            // percentiles
            if (percentileValues != null) {
                while (percentileIndex < percentiles.length) {
                    percentileValues[percentileIndex] = iterValue != null ? iterValue.getValueIteratedTo() : NO_VALUE;
                    ++percentileIndex;
                }
            }

            // buckets
            if (bucketSizes != null) {
                while (bucketIndex < bucketSizes.length) {
                    bucketSizes[bucketIndex] = iterValue != null ? iterValue.getTotalCountToThisValue() : NO_VALUE;
                    ++bucketIndex;
                }
            }
        } else {
            if (percentileValues != null) {
                fill(percentileValues, NO_VALUE);
            }

            if (bucketSizes != null) {
                fill(bucketSizes, NO_VALUE);
            }
        }

        return new HdrXHistogramImplSnapshot(
            min,
            max,
            mean,
            standardDeviation,
            quantiles,
            percentileValues,
            bucketUpperBounds,
            bucketSizes);
    }

    protected abstract org.HdrHistogram.Histogram hdrHistogramForSnapshot();
}
