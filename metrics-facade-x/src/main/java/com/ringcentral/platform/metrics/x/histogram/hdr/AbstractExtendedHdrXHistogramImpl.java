package com.ringcentral.platform.metrics.x.histogram.hdr;

import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.x.histogram.*;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.*;
import org.HdrHistogram.*;

import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.x.histogram.XHistogramSnapshot.*;
import static java.lang.Math.sqrt;
import static java.util.Arrays.fill;

public abstract class AbstractExtendedHdrXHistogramImpl implements XHistogramImpl {

    protected final boolean withMin;
    protected final boolean withMax;
    protected final boolean withMean;
    protected final boolean withStandardDeviation;
    protected final boolean withPercentiles;
    protected final double[] quantiles;
    protected final double[] percentiles;
    protected final boolean withBuckets;
    protected final long[] bucketUpperBounds;

    protected final long highestTrackableValue;
    protected final OverflowBehavior overflowBehavior;
    protected final long expectedUpdateInterval;

    protected final ScheduledExecutorService executor;

    protected AbstractExtendedHdrXHistogramImpl(
        HdrXHistogramImplConfig config,
        MeasurementSpec measurementSpec,
        ScheduledExecutorService executor) {

        this.withMin = measurementSpec.isWithMin();
        this.withMax = measurementSpec.isWithMax();
        this.withMean = measurementSpec.isWithMean();
        this.withStandardDeviation = measurementSpec.isWithStandardDeviation();
        this.withPercentiles = measurementSpec.isWithPercentiles();
        this.quantiles = measurementSpec.quantiles();
        this.percentiles = measurementSpec.percentiles();
        this.withBuckets = measurementSpec.isWithBuckets();
        this.bucketUpperBounds = measurementSpec.bucketUpperBounds();

        this.highestTrackableValue = config.highestTrackableValue().orElse(Long.MAX_VALUE);
        this.overflowBehavior = config.overflowBehavior().orElse(null);
        this.expectedUpdateInterval = config.expectedUpdateInterval().orElse(0L);
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
    public void update(long value) {
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
    public synchronized XHistogramSnapshot snapshot() {
        org.HdrHistogram.Histogram h = hdrHistogramForSnapshot();
        long min = withMin ? h.getMinValue() : NO_VALUE;
        long max = withMax ? h.getMaxValue() : NO_VALUE;
        double mean = NO_VALUE_DOUBLE;
        double standardDeviation = NO_VALUE_DOUBLE;
        double[] percentileValues = withPercentiles ? new double[percentiles.length] : null;
        long[] bucketSizes = withBuckets ? new long[bucketUpperBounds.length] : null;

        if (!(withMean || withStandardDeviation || percentileValues != null || bucketSizes != null)) {
            return new DefaultXHistogramSnapshot(
                NO_VALUE,
                NO_VALUE,
                min,
                max,
                NO_VALUE,
                NO_VALUE,
                null,
                null,
                null,
                null);
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
                    totalForMean += h.medianEquivalentValue(iterValue.getValueIteratedTo()) * (double) iterValue.getCountAtValueIteratedTo();
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
                    long v = iterValue.getValueIteratedTo();

                    while (bucketIndex < bucketSizes.length) {
                        if (bucketUpperBounds[bucketIndex] > v) {
                            break;
                        }

                        bucketSizes[bucketIndex] = iterValue.getTotalCountToThisValue();

                        if (bucketUpperBounds[bucketIndex] < v) {
                            bucketSizes[bucketIndex] -= iterValue.getCountAddedInThisIterationStep();
                        }

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

    protected abstract Histogram hdrHistogramForSnapshot();
}
