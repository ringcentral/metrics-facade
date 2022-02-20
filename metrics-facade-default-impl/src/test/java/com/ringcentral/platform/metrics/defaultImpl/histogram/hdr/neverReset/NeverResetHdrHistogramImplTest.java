package com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.neverReset;

import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;
import com.ringcentral.platform.metrics.test.time.*;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.configs.HdrHistogramImplConfigBuilder.hdrImpl;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static java.lang.Math.sqrt;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NeverResetHdrHistogramImplTest {

    static final TestTimeNanosProvider timeNanosProvider = new TestTimeNanosProvider();
    static final ScheduledExecutorService executor = new TestScheduledExecutorService(timeNanosProvider);

    @Test
    public void allMeasurables_NeverResetBuckets() {
        NeverResetHdrHistogramImpl h = new NeverResetHdrHistogramImpl(
            hdrImpl()
                .neverReset()
                .significantDigits(5)
                .build(),
            Set.of(
                COUNT,
                TOTAL_SUM,
                MIN,
                MAX,
                MEAN,
                STANDARD_DEVIATION,

                // percentiles
                PERCENTILE_1,
                PERCENTILE_5,
                PERCENTILE_15,
                PERCENTILE_25,
                PERCENTILE_35,
                PERCENTILE_45,
                PERCENTILE_50,
                PERCENTILE_55,
                PERCENTILE_70,
                PERCENTILE_75,
                PERCENTILE_80,
                PERCENTILE_95,
                PERCENTILE_99,
                PERCENTILE_999,

                // buckets
                Bucket.of(-5),
                Bucket.of(1),
                Bucket.of(2),
                Bucket.of(3),
                Bucket.of(4),
                Bucket.of(5),
                Bucket.of(27),
                Bucket.of(50),
                Bucket.of(88),
                Bucket.of(107)),
            executor);

        h.metricInstanceAdded();
        long expectedTotalSum = 0L;

        for (int v = 1; v <= 100; ++v) {
            h.update(v);
            expectedTotalSum += v;
        }

        double expectedMean = (1.0 * expectedTotalSum) / 100L;
        double expectedGeometricDeviationSum = 0.0;

        for (int v = 1; v <= 100; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation;
        }

        double expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 100);

        HistogramSnapshot snapshot = h.snapshot();
        assertThat(snapshot.count(), is(100L));
        assertThat(snapshot.totalSum(), is(expectedTotalSum));
        assertThat(snapshot.min(), is(1L));
        assertThat(snapshot.max(), is(100L));
        assertThat(snapshot.mean(), is(expectedMean));
        assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

        assertThat(snapshot.percentileValue(PERCENTILE_1), is(1.0));
        assertThat(snapshot.percentileValue(PERCENTILE_5), is(5.0));
        assertThat(snapshot.percentileValue(PERCENTILE_15), is(15.0));
        assertThat(snapshot.percentileValue(PERCENTILE_25), is(25.0));
        assertThat(snapshot.percentileValue(PERCENTILE_35), is(35.0));
        assertThat(snapshot.percentileValue(PERCENTILE_45), is(45.0));
        assertThat(snapshot.percentileValue(PERCENTILE_50), is(50.0));
        assertThat(snapshot.percentileValue(PERCENTILE_55), is(55.0));
        assertThat(snapshot.percentileValue(PERCENTILE_70), is(70.0));
        assertThat(snapshot.percentileValue(PERCENTILE_75), is(75.0));
        assertThat(snapshot.percentileValue(PERCENTILE_80), is(80.0));
        assertThat(snapshot.percentileValue(PERCENTILE_95), is(95.0));
        assertThat(snapshot.percentileValue(PERCENTILE_99), is(99.0));
        assertThat(snapshot.percentileValue(PERCENTILE_999), is(100.0));

        assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
        assertThat(snapshot.bucketSize(Bucket.of(1)), is(1L));
        assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
        assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
        assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
        assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
        assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L));
        assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L));
        assertThat(snapshot.bucketSize(Bucket.of(88)), is(88L));
        assertThat(snapshot.bucketSize(Bucket.of(107)), is(100L));

        for (int v = 1; v <= 100; ++v) {
            h.update(v);
            expectedTotalSum += v;
        }

        expectedMean = (1.0 * expectedTotalSum) / 200L;
        expectedGeometricDeviationSum = 0.0;

        for (int v = 1; v <= 100; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation * 2L;
        }

        expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 200L);

        snapshot = h.snapshot();
        assertThat(snapshot.count(), is(200L));
        assertThat(snapshot.totalSum(), is(expectedTotalSum));
        assertThat(snapshot.min(), is(1L));
        assertThat(snapshot.max(), is(100L));
        assertThat(snapshot.mean(), is(expectedMean));
        assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

        assertThat(snapshot.percentileValue(PERCENTILE_1), is(1.0));
        assertThat(snapshot.percentileValue(PERCENTILE_5), is(5.0));
        assertThat(snapshot.percentileValue(PERCENTILE_15), is(15.0));
        assertThat(snapshot.percentileValue(PERCENTILE_25), is(25.0));
        assertThat(snapshot.percentileValue(PERCENTILE_35), is(35.0));
        assertThat(snapshot.percentileValue(PERCENTILE_45), is(45.0));
        assertThat(snapshot.percentileValue(PERCENTILE_50), is(50.0));
        assertThat(snapshot.percentileValue(PERCENTILE_55), is(55.0));
        assertThat(snapshot.percentileValue(PERCENTILE_70), is(70.0));
        assertThat(snapshot.percentileValue(PERCENTILE_75), is(75.0));
        assertThat(snapshot.percentileValue(PERCENTILE_80), is(80.0));
        assertThat(snapshot.percentileValue(PERCENTILE_95), is(95.0));
        assertThat(snapshot.percentileValue(PERCENTILE_99), is(99.0));
        assertThat(snapshot.percentileValue(PERCENTILE_999), is(100.0));

        assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
        assertThat(snapshot.bucketSize(Bucket.of(1)), is(1L + 1L));
        assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L + 2L));
        assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L + 3L));
        assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L + 4L));
        assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L + 5L));
        assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L + 27L));
        assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L + 50L));
        assertThat(snapshot.bucketSize(Bucket.of(88)), is(88L + 88L));
        assertThat(snapshot.bucketSize(Bucket.of(107)), is(100L + 100L));

        for (int v = 1; v <= 100; ++v) {
            h.update(v);
            expectedTotalSum += v;
        }

        expectedMean = (1.0 * expectedTotalSum) / 300L;
        expectedGeometricDeviationSum = 0.0;

        for (int v = 1; v <= 100; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation * 3L;
        }

        expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 300L);

        snapshot = h.snapshot();
        assertThat(snapshot.count(), is(300L));
        assertThat(snapshot.totalSum(), is(expectedTotalSum));
        assertThat(snapshot.min(), is(1L));
        assertThat(snapshot.max(), is(100L));
        assertThat(snapshot.mean(), is(expectedMean));
        assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

        assertThat(snapshot.percentileValue(PERCENTILE_1), is(1.0));
        assertThat(snapshot.percentileValue(PERCENTILE_5), is(5.0));
        assertThat(snapshot.percentileValue(PERCENTILE_15), is(15.0));
        assertThat(snapshot.percentileValue(PERCENTILE_25), is(25.0));
        assertThat(snapshot.percentileValue(PERCENTILE_35), is(35.0));
        assertThat(snapshot.percentileValue(PERCENTILE_45), is(45.0));
        assertThat(snapshot.percentileValue(PERCENTILE_50), is(50.0));
        assertThat(snapshot.percentileValue(PERCENTILE_55), is(55.0));
        assertThat(snapshot.percentileValue(PERCENTILE_70), is(70.0));
        assertThat(snapshot.percentileValue(PERCENTILE_75), is(75.0));
        assertThat(snapshot.percentileValue(PERCENTILE_80), is(80.0));
        assertThat(snapshot.percentileValue(PERCENTILE_95), is(95.0));
        assertThat(snapshot.percentileValue(PERCENTILE_99), is(99.0));
        assertThat(snapshot.percentileValue(PERCENTILE_999), is(100.0));

        assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
        assertThat(snapshot.bucketSize(Bucket.of(1)), is(1L + 1L + 1L));
        assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L + 2L + 2L));
        assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L + 3L + 3L));
        assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L + 4L + 4L));
        assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L + 5L + 5L));
        assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L + 27L + 27L));
        assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L + 50L + 50L));
        assertThat(snapshot.bucketSize(Bucket.of(88)), is(88L + 88L + 88L));
        assertThat(snapshot.bucketSize(Bucket.of(107)), is(100L + 100L + 100L));

        h.metricInstanceRemoved();
    }

    @Test
    public void allMeasurables_ResettableBuckets() {
        NeverResetHdrHistogramImpl h = new NeverResetHdrHistogramImpl(
            hdrImpl()
                .neverReset()
                .significantDigits(5)
                .resettableBuckets()
                .build(),
            Set.of(
                COUNT,
                TOTAL_SUM,
                MIN,
                MAX,
                MEAN,
                STANDARD_DEVIATION,

                // percentiles
                PERCENTILE_1,
                PERCENTILE_5,
                PERCENTILE_15,
                PERCENTILE_25,
                PERCENTILE_35,
                PERCENTILE_45,
                PERCENTILE_50,
                PERCENTILE_55,
                PERCENTILE_70,
                PERCENTILE_75,
                PERCENTILE_80,
                PERCENTILE_95,
                PERCENTILE_99,
                PERCENTILE_999,

                // buckets
                Bucket.of(-5),
                Bucket.of(1),
                Bucket.of(2),
                Bucket.of(3),
                Bucket.of(4),
                Bucket.of(5),
                Bucket.of(27),
                Bucket.of(50),
                Bucket.of(88),
                Bucket.of(107)),
            executor);

        h.metricInstanceAdded();
        long expectedTotalSum = 0L;

        for (int v = 1; v <= 100; ++v) {
            h.update(v);
            expectedTotalSum += v;
        }

        double expectedMean = (1.0 * expectedTotalSum) / 100L;
        double expectedGeometricDeviationSum = 0.0;

        for (int v = 1; v <= 100; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation;
        }

        double expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 100);

        HistogramSnapshot snapshot = h.snapshot();
        assertThat(snapshot.count(), is(100L));
        assertThat(snapshot.totalSum(), is(expectedTotalSum));
        assertThat(snapshot.min(), is(1L));
        assertThat(snapshot.max(), is(100L));
        assertThat(snapshot.mean(), is(expectedMean));
        assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

        assertThat(snapshot.percentileValue(PERCENTILE_1), is(1.0));
        assertThat(snapshot.percentileValue(PERCENTILE_5), is(5.0));
        assertThat(snapshot.percentileValue(PERCENTILE_15), is(15.0));
        assertThat(snapshot.percentileValue(PERCENTILE_25), is(25.0));
        assertThat(snapshot.percentileValue(PERCENTILE_35), is(35.0));
        assertThat(snapshot.percentileValue(PERCENTILE_45), is(45.0));
        assertThat(snapshot.percentileValue(PERCENTILE_50), is(50.0));
        assertThat(snapshot.percentileValue(PERCENTILE_55), is(55.0));
        assertThat(snapshot.percentileValue(PERCENTILE_70), is(70.0));
        assertThat(snapshot.percentileValue(PERCENTILE_75), is(75.0));
        assertThat(snapshot.percentileValue(PERCENTILE_80), is(80.0));
        assertThat(snapshot.percentileValue(PERCENTILE_95), is(95.0));
        assertThat(snapshot.percentileValue(PERCENTILE_99), is(99.0));
        assertThat(snapshot.percentileValue(PERCENTILE_999), is(100.0));

        assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
        assertThat(snapshot.bucketSize(Bucket.of(1)), is(1L));
        assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
        assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
        assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
        assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
        assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L));
        assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L));
        assertThat(snapshot.bucketSize(Bucket.of(88)), is(88L));
        assertThat(snapshot.bucketSize(Bucket.of(107)), is(100L));

        for (int v = 1; v <= 100; ++v) {
            h.update(v);
            expectedTotalSum += v;
        }

        expectedMean = (1.0 * expectedTotalSum) / 200L;
        expectedGeometricDeviationSum = 0.0;

        for (int v = 1; v <= 100; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation * 2L;
        }

        expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 200L);

        snapshot = h.snapshot();
        assertThat(snapshot.count(), is(200L));
        assertThat(snapshot.totalSum(), is(expectedTotalSum));
        assertThat(snapshot.min(), is(1L));
        assertThat(snapshot.max(), is(100L));
        assertThat(snapshot.mean(), is(expectedMean));
        assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

        assertThat(snapshot.percentileValue(PERCENTILE_1), is(1.0));
        assertThat(snapshot.percentileValue(PERCENTILE_5), is(5.0));
        assertThat(snapshot.percentileValue(PERCENTILE_15), is(15.0));
        assertThat(snapshot.percentileValue(PERCENTILE_25), is(25.0));
        assertThat(snapshot.percentileValue(PERCENTILE_35), is(35.0));
        assertThat(snapshot.percentileValue(PERCENTILE_45), is(45.0));
        assertThat(snapshot.percentileValue(PERCENTILE_50), is(50.0));
        assertThat(snapshot.percentileValue(PERCENTILE_55), is(55.0));
        assertThat(snapshot.percentileValue(PERCENTILE_70), is(70.0));
        assertThat(snapshot.percentileValue(PERCENTILE_75), is(75.0));
        assertThat(snapshot.percentileValue(PERCENTILE_80), is(80.0));
        assertThat(snapshot.percentileValue(PERCENTILE_95), is(95.0));
        assertThat(snapshot.percentileValue(PERCENTILE_99), is(99.0));
        assertThat(snapshot.percentileValue(PERCENTILE_999), is(100.0));

        assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
        assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L + 2L));
        assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L + 3L));
        assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L + 4L));
        assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L + 5L));
        assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L + 27L));
        assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L + 50L));
        assertThat(snapshot.bucketSize(Bucket.of(88)), is(88L + 88L));
        assertThat(snapshot.bucketSize(Bucket.of(107)), is(100L + 100L));

        for (int v = 1; v <= 100; ++v) {
            h.update(v);
            expectedTotalSum += v;
        }

        expectedMean = (1.0 * expectedTotalSum) / 300L;
        expectedGeometricDeviationSum = 0.0;

        for (int v = 1; v <= 100; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation * 3L;
        }

        expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 300L);

        snapshot = h.snapshot();
        assertThat(snapshot.count(), is(300L));
        assertThat(snapshot.totalSum(), is(expectedTotalSum));
        assertThat(snapshot.min(), is(1L));
        assertThat(snapshot.max(), is(100L));
        assertThat(snapshot.mean(), is(expectedMean));
        assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

        assertThat(snapshot.percentileValue(PERCENTILE_1), is(1.0));
        assertThat(snapshot.percentileValue(PERCENTILE_5), is(5.0));
        assertThat(snapshot.percentileValue(PERCENTILE_15), is(15.0));
        assertThat(snapshot.percentileValue(PERCENTILE_25), is(25.0));
        assertThat(snapshot.percentileValue(PERCENTILE_35), is(35.0));
        assertThat(snapshot.percentileValue(PERCENTILE_45), is(45.0));
        assertThat(snapshot.percentileValue(PERCENTILE_50), is(50.0));
        assertThat(snapshot.percentileValue(PERCENTILE_55), is(55.0));
        assertThat(snapshot.percentileValue(PERCENTILE_70), is(70.0));
        assertThat(snapshot.percentileValue(PERCENTILE_75), is(75.0));
        assertThat(snapshot.percentileValue(PERCENTILE_80), is(80.0));
        assertThat(snapshot.percentileValue(PERCENTILE_95), is(95.0));
        assertThat(snapshot.percentileValue(PERCENTILE_99), is(99.0));
        assertThat(snapshot.percentileValue(PERCENTILE_999), is(100.0));

        assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
        assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L + 2L + 2L));
        assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L + 3L + 3L));
        assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L + 4L + 4L));
        assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L + 5L + 5L));
        assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L + 27L + 27L));
        assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L + 50L + 50L));
        assertThat(snapshot.bucketSize(Bucket.of(88)), is(88L + 88L + 88L));
        assertThat(snapshot.bucketSize(Bucket.of(107)), is(100L + 100L + 100L));

        h.metricInstanceRemoved();
    }
}