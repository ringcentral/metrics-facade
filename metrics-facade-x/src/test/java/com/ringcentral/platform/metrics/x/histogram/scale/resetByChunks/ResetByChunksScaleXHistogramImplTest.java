package com.ringcentral.platform.metrics.x.histogram.scale.resetByChunks;

import com.ringcentral.platform.metrics.test.time.*;
import com.ringcentral.platform.metrics.x.histogram.XHistogramSnapshot;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.LinearScaleBuilder;
import org.junit.Test;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.x.histogram.XHistogramSnapshot.*;
import static com.ringcentral.platform.metrics.x.histogram.scale.configs.LinearScaleBuilder.linearScale;
import static com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfigBuilder.scaleImpl;
import static java.lang.Math.sqrt;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResetByChunksScaleXHistogramImplTest {

    static final int CHUNK_COUNT = 5;
    static final int ALL_CHUNKS_RESET_PERIOD_MS = 1000 * CHUNK_COUNT;

    static final LinearScaleBuilder SCALE_1 = linearScale().steps(1, 100);
    static final TestTimeNanosProvider timeNanosProvider = new TestTimeNanosProvider();
    static final TestTimeMsProvider timeMsProvider = new TestTimeMsProvider(timeNanosProvider);
    static final ScheduledExecutorService executor = new TestScheduledExecutorService(timeNanosProvider);

    @Test
    public void scale_1_allMeasurables_neverResetBuckets() {
        ResetByChunksScaleXHistogramImpl h = new ResetByChunksScaleXHistogramImpl(
            scaleImpl()
                .resetByChunks(CHUNK_COUNT, Duration.ofMillis(ALL_CHUNKS_RESET_PERIOD_MS))
                .with(SCALE_1)
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
            executor,
            timeMsProvider);

        h.metricInstanceAdded();

        // 1 chunk
        long expectedTotalSum = 0L;

        for (int v = 1; v <= 10; ++v) {
            h.update(v);
            expectedTotalSum += v;
        }

        double expectedMean = (1.0 * expectedTotalSum) / 10;
        double expectedGeometricDeviationSum = 0.0;

        for (int v = 1; v <= 10; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation;
        }

        double expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 10);

        for (int i = 0; i < 10; ++i) {
            XHistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(10L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(1L));
            assertThat(snapshot.max(), is(10L));
            assertThat(snapshot.mean(), is(expectedMean));
            assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(1.0));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(1.0));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(2.0));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(3.0));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(4.0));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(5.0));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(5.0));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(6.0));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(7.0));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(8.0));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(8.0));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(10.0));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(10.0));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(10.0));

            assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(10L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(10L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(10L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(10L));

            timeNanosProvider.increaseMs(100);
        }

        // 2 chunk
        for (int v = 11; v <= 20; ++v) {
            h.update(v);
            expectedTotalSum += v;
        }

        expectedMean = (1.0 * expectedTotalSum) / 20;
        expectedGeometricDeviationSum = 0.0;

        for (int v = 1; v <= 20; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation;
        }

        expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 20);

        for (int i = 0; i < 10; ++i) {
            XHistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(20L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(1L));
            assertThat(snapshot.max(), is(20L));
            assertThat(snapshot.mean(), is(expectedMean));
            assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(1.0));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(1.0));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(3.0));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(5.0));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(7.0));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(9.0));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(10.0));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(11.0));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(14.0));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(15.0));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(16.0));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(19.0));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(20.0));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(20.0));

            assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(20L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(20L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(20L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(20L));

            timeNanosProvider.increaseMs(100);
        }

        // 3 chunk
        for (int v = 21; v <= 30; ++v) {
            h.update(v);
            expectedTotalSum += v;
        }

        expectedMean = (1.0 * expectedTotalSum) / 30;
        expectedGeometricDeviationSum = 0.0;

        for (int v = 1; v <= 30; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation;
        }

        expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 30);

        for (int i = 0; i < 10; ++i) {
            XHistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(30L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(1L));
            assertThat(snapshot.max(), is(30L));
            assertThat(snapshot.mean(), is(expectedMean));
            assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(1.0));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(2.0));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(5.0));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(8.0));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(11.0));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(14.0));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(15.0));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(17.0));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(21.0));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(23.0));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(24.0));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(29.0));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(30.0));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(30.0));

            assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(30L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(30L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(30L));

            timeNanosProvider.increaseMs(100);
        }

        // 4 chunk
        for (int v = 31; v <= 40; ++v) {
            h.update(v);
            expectedTotalSum += v;
        }

        expectedMean = (1.0 * expectedTotalSum) / 40;
        expectedGeometricDeviationSum = 0.0;

        for (int v = 1; v <= 40; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation;
        }

        expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 40);

        for (int i = 0; i < 10; ++i) {
            XHistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(40L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(1L));
            assertThat(snapshot.max(), is(40L));
            assertThat(snapshot.mean(), is(expectedMean));
            assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(1.0));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(2.0));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(6.0));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(10.0));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(14.0));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(18.0));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(20.0));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(22.0));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(28.0));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(30.0));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(32.0));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(38.0));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(40.0));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(40.0));

            assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(40L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(40L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(40L));

            timeNanosProvider.increaseMs(100);
        }

        // 5 chunk
        for (int v = 41; v <= 50; ++v) {
            h.update(v);
            expectedTotalSum += v;
        }

        expectedMean = (1.0 * expectedTotalSum) / 50;
        expectedGeometricDeviationSum = 0.0;

        for (int v = 1; v <= 50; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation;
        }

        expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 50);

        for (int i = 0; i < 10; ++i) {
            XHistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(50L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(1L));
            assertThat(snapshot.max(), is(50L));
            assertThat(snapshot.mean(), is(expectedMean));
            assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(1.0));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(3.0));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(8.0));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(13.0));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(18.0));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(23.0));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(25.0));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(28.0));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(35.0));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(38.0));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(40.0));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(48.0));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(50.0));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(50.0));

            assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(50L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(50L));

            timeNanosProvider.increaseMs(100 - 10);
        }

        timeNanosProvider.increaseMs(100);

        // chunk 1 reset
        long totalSumForMean = expectedTotalSum;

        for (int v = 1; v <= 10; ++v) {
            totalSumForMean -= v;
        }

        expectedMean = (1.0 * totalSumForMean) / 40;
        expectedGeometricDeviationSum = 0.0;

        for (int v = 11; v <= 50; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation;
        }

        expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 40);

        for (int i = 0; i < 10; ++i) {
            XHistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(50L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(11L));
            assertThat(snapshot.max(), is(50L));
            assertThat(snapshot.mean(), is(expectedMean));
            assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(11.0));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(12.0));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(16.0));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(20.0));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(24.0));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(28.0));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(30.0));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(32.0));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(38.0));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(40.0));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(42.0));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(48.0));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(50.0));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(50.0));

            assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(50L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(50L));

            timeNanosProvider.increaseMs(100 - 10);
        }

        timeNanosProvider.increaseMs(100);

        // chunk 2 reset
        for (int v = 11; v <= 20; ++v) {
            totalSumForMean -= v;
        }

        expectedMean = (1.0 * totalSumForMean) / 30;
        expectedGeometricDeviationSum = 0.0;

        for (int v = 21; v <= 50; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation;
        }

        expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 30);

        for (int i = 0; i < 10; ++i) {
            XHistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(50L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(21L));
            assertThat(snapshot.max(), is(50L));
            assertThat(snapshot.mean(), is(expectedMean));
            assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(21.0));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(22.0));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(25.0));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(28.0));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(31.0));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(34.0));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(35.0));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(37.0));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(41.0));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(43.0));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(44.0));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(49.0));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(50.0));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(50.0));

            assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(50L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(50L));

            timeNanosProvider.increaseMs(100 - 10);
        }

        timeNanosProvider.increaseMs(100);

        // chunk 3 reset
        for (int v = 21; v <= 30; ++v) {
            totalSumForMean -= v;
        }

        expectedMean = (1.0 * totalSumForMean) / 20;
        expectedGeometricDeviationSum = 0.0;

        for (int v = 31; v <= 50; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation;
        }

        expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 20);

        for (int i = 0; i < 10; ++i) {
            XHistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(50L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(31L));
            assertThat(snapshot.max(), is(50L));
            assertThat(snapshot.mean(), is(expectedMean));
            assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(31.0));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(31.0));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(33.0));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(35.0));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(37.0));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(39.0));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(40.0));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(41.0));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(44.0));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(45.0));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(46.0));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(49.0));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(50.0));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(50.0));

            assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(50L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(50L));

            timeNanosProvider.increaseMs(100 - 10);
        }

        timeNanosProvider.increaseMs(100);

        // chunk 4 reset
        for (int v = 31; v <= 40; ++v) {
            totalSumForMean -= v;
        }

        expectedMean = (1.0 * totalSumForMean) / 10;
        expectedGeometricDeviationSum = 0.0;

        for (int v = 41; v <= 50; ++v) {
            double deviation = (v * 1.0) - expectedMean;
            expectedGeometricDeviationSum += deviation * deviation;
        }

        expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 10);

        for (int i = 0; i < 10; ++i) {
            XHistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(50L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(41L));
            assertThat(snapshot.max(), is(50L));
            assertThat(snapshot.mean(), is(expectedMean));
            assertThat(snapshot.standardDeviation(), is(expectedStandardDeviation));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(41.0));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(41.0));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(42.0));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(43.0));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(44.0));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(45.0));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(45.0));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(46.0));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(47.0));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(48.0));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(48.0));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(50.0));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(50.0));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(50.0));

            assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(50L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(50L));

            timeNanosProvider.increaseMs(100 - 10);
        }

        timeNanosProvider.increaseMs(100);

        // chunk 5 reset
        for (int i = 0; i < 10; ++i) {
            XHistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(50L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(NO_VALUE));
            assertThat(snapshot.max(), is(NO_VALUE));
            assertThat(snapshot.mean(), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.standardDeviation(), is(NO_VALUE_DOUBLE));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(NO_VALUE_DOUBLE));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(NO_VALUE_DOUBLE));

            assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(50L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(50L));

            timeNanosProvider.increaseMs(100 - 10);
        }

        // 1 chunk
        for (int v = 1; v <= 10; ++v) {
            h.update(v);
        }

        for (int i = 0; i < 10; ++i) {
            XHistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(60L));
            assertThat(snapshot.min(), is(1L));
            assertThat(snapshot.max(), is(10L));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(1.0));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(1.0));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(2.0));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(3.0));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(4.0));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(5.0));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(5.0));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(6.0));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(7.0));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(8.0));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(8.0));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(10.0));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(10.0));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(10.0));

            assertThat(snapshot.bucketSize(Bucket.of(-5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L + 2L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L + 3L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L + 4L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L + 5L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L + 10L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L + 10L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(50L + 10L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(50L + 10L));
        }

        h.metricInstanceRemoved();
    }

    static class PercentileCalculator {
        public static void main(String[] args) {
            int size = 10;
            long[] a = new long[size];

            for (int i = 1; i <= 10; ++i) {
                a[i - 1] = i;
            }

            System.out.println(percentile(a, 0.01));
            System.out.println(percentile(a, 0.05));
            System.out.println(percentile(a, 0.15));
            System.out.println(percentile(a, 0.25));
            System.out.println(percentile(a, 0.35));
            System.out.println(percentile(a, 0.45));
            System.out.println(percentile(a, 0.5));
            System.out.println(percentile(a, 0.55));
            System.out.println(percentile(a, 0.7));
            System.out.println(percentile(a, 0.75));
            System.out.println(percentile(a, 0.8));
            System.out.println(percentile(a, 0.95));
            System.out.println(percentile(a, 0.99));
            System.out.println(percentile(a, 0.999));
        }

        static long percentile(long[] a, double q) {
            int index = (int)Math.round(q * a.length);
            return a[Math.max(index-1, 0)];
        }
    }
}