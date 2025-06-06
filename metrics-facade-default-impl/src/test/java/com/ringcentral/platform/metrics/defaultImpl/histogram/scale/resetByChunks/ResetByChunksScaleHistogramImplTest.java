package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.resetByChunks;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImplTest;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;
import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.TotalsMeasurementType;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.scale.LinearScaleBuilder;
import com.ringcentral.platform.metrics.scale.Scale;
import com.ringcentral.platform.metrics.test.time.*;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot.*;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfigBuilder.scaleImpl;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.scale.LinearScaleBuilder.linearScale;
import static com.ringcentral.platform.metrics.scale.SpecificScaleBuilder.points;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Math.sqrt;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResetByChunksScaleHistogramImplTest extends AbstractHistogramImplTest<ResetByChunksScaleHistogramImpl> {

    static final int CHUNK_COUNT = 5;
    public static final long CHUNK_RESET_PERIOD_MS = 1000L;
    static final long ALL_CHUNKS_RESET_PERIOD_MS = CHUNK_RESET_PERIOD_MS * CHUNK_COUNT;

    static final LinearScaleBuilder SCALE_1 = linearScale().steps(1, 100);

    static final Scale SCALE_2 = points(
        MILLISECONDS,
        5, 10, 25, 50, 75, 100, 250, 500, 750, 1000,
        2500, 5000, 7500, 10000, MAX_VALUE).build();

    static final TestTimeNanosProvider timeNanosProvider = new TestTimeNanosProvider();
    static final TestTimeMsProvider timeMsProvider = new TestTimeMsProvider(timeNanosProvider);
    static final ScheduledExecutorService executor = new TestScheduledExecutorService(timeNanosProvider);

    @Override
    protected ResetByChunksScaleHistogramImpl makeHistogramImpl(@Nonnull TotalsMeasurementType totalsMeasurementType, @Nonnull Measurable... measurables) {
        return new ResetByChunksScaleHistogramImpl(
            scaleImpl().resetByChunks().totals(totalsMeasurementType).build(),
            Set.of(measurables),
            executor);
    }

    @Test
    public void scale_1_AllMeasurables_NeverResetBuckets() {
        ResetByChunksScaleHistogramImpl h = new ResetByChunksScaleHistogramImpl(
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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

    @Test
    public void scale_1_AllMeasurables_ResettableBuckets() {
        ResetByChunksScaleHistogramImpl h = new ResetByChunksScaleHistogramImpl(
            scaleImpl()
                .resetByChunks(CHUNK_COUNT, Duration.ofMillis(ALL_CHUNKS_RESET_PERIOD_MS))
                .with(SCALE_1)
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            HistogramSnapshot snapshot = h.snapshot();
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
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(17L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(40L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(40L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(40L));

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
            HistogramSnapshot snapshot = h.snapshot();
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
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(7L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(30L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(30L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(30L));

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
            HistogramSnapshot snapshot = h.snapshot();
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
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(20L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(20L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(20L));

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
            HistogramSnapshot snapshot = h.snapshot();
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
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(0L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(10L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(10L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(10L));

            timeNanosProvider.increaseMs(100 - 10);
        }

        timeNanosProvider.increaseMs(100);

        // chunk 5 reset
        for (int i = 0; i < 10; ++i) {
            HistogramSnapshot snapshot = h.snapshot();
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

            assertThat(snapshot.bucketSize(Bucket.of(-5)), is(NO_VALUE));
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(NO_VALUE));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(NO_VALUE));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(NO_VALUE));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(NO_VALUE));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(NO_VALUE));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(NO_VALUE));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(NO_VALUE));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(NO_VALUE));

            timeNanosProvider.increaseMs(100 - 10);
        }

        // 1 chunk
        for (int v = 1; v <= 10; ++v) {
            h.update(v);
        }

        for (int i = 0; i < 10; ++i) {
            HistogramSnapshot snapshot = h.snapshot();
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
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(10L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(10L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(10L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(10L));
        }

        h.metricInstanceRemoved();
    }

    @Test
    public void scale_2_ResettableBuckets_ResetPeriodically() {
        ResetByChunksScaleHistogramImpl h = new ResetByChunksScaleHistogramImpl(
            scaleImpl()
                .resetPeriodically(Duration.ofMillis(CHUNK_RESET_PERIOD_MS))
                .with(SCALE_2)
                .resettableBuckets()
                .build(),
            Set.of(
                COUNT,
                TOTAL_SUM,
                MIN,
                MAX,
                MEAN,

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
                Buckets.of(SCALE_2)),
            executor,
            timeMsProvider);

        h.metricInstanceAdded();

        List<Integer> updateValues = List.of(1, 3, 3, 25, 27, 27, 27, 32, 75, 77, 125, 235, 48, 778, 778, 778, 275, 500, 500, 8000);
        updateValues.forEach(v -> h.update(ms(v)));

        long expectedTotalSum = ms(updateValues.stream().reduce(0, Integer::sum));
        double expectedMean = (1.0 * expectedTotalSum) / 20;

        for (int i = 0; i < 10; ++i) {
            HistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(20L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(ms(5)));
            assertThat(snapshot.max(), is(ms(10000)));
            assertThat(snapshot.mean(), is(expectedMean));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(percentileValueMs(5)));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(percentileValueMs(5)));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(percentileValueMs(5)));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(percentileValueMs(50)));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(percentileValueMs(50)));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(percentileValueMs(50)));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(percentileValueMs(75)));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(percentileValueMs(100)));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(percentileValueMs(500)));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(percentileValueMs(500)));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(percentileValueMs(500)));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(percentileValueMs(1000)));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(percentileValueMs(10000)));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(percentileValueMs(10000)));

            assertThat(snapshot.bucketSize(Bucket.of(5, MILLISECONDS)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(10, MILLISECONDS)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(25, MILLISECONDS)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(50, MILLISECONDS)), is(9L));
            assertThat(snapshot.bucketSize(Bucket.of(75, MILLISECONDS)), is(10L));
            assertThat(snapshot.bucketSize(Bucket.of(100, MILLISECONDS)), is(11L));
            assertThat(snapshot.bucketSize(Bucket.of(250, MILLISECONDS)), is(13L));
            assertThat(snapshot.bucketSize(Bucket.of(500, MILLISECONDS)), is(16L));
            assertThat(snapshot.bucketSize(Bucket.of(750, MILLISECONDS)), is(16L));
            assertThat(snapshot.bucketSize(Bucket.of(1000, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(2500, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(5000, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(7500, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(10000, MILLISECONDS)), is(20L));
            assertThat(snapshot.bucketSize(INF_BUCKET), is(20L));
        }

        timeNanosProvider.increaseMs(CHUNK_RESET_PERIOD_MS - 1);

        for (int i = 0; i < 10; ++i) {
            HistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(20L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(ms(5)));
            assertThat(snapshot.max(), is(ms(10000)));
            assertThat(snapshot.mean(), is(expectedMean));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(percentileValueMs(5)));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(percentileValueMs(5)));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(percentileValueMs(5)));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(percentileValueMs(50)));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(percentileValueMs(50)));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(percentileValueMs(50)));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(percentileValueMs(75)));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(percentileValueMs(100)));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(percentileValueMs(500)));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(percentileValueMs(500)));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(percentileValueMs(500)));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(percentileValueMs(1000)));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(percentileValueMs(10000)));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(percentileValueMs(10000)));

            assertThat(snapshot.bucketSize(Bucket.of(5, MILLISECONDS)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(10, MILLISECONDS)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(25, MILLISECONDS)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(50, MILLISECONDS)), is(9L));
            assertThat(snapshot.bucketSize(Bucket.of(75, MILLISECONDS)), is(10L));
            assertThat(snapshot.bucketSize(Bucket.of(100, MILLISECONDS)), is(11L));
            assertThat(snapshot.bucketSize(Bucket.of(250, MILLISECONDS)), is(13L));
            assertThat(snapshot.bucketSize(Bucket.of(500, MILLISECONDS)), is(16L));
            assertThat(snapshot.bucketSize(Bucket.of(750, MILLISECONDS)), is(16L));
            assertThat(snapshot.bucketSize(Bucket.of(1000, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(2500, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(5000, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(7500, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(10000, MILLISECONDS)), is(20L));
            assertThat(snapshot.bucketSize(INF_BUCKET), is(20L));
        }

        timeNanosProvider.increaseMs(1);

        HistogramSnapshot snapshot = h.snapshot();
        assertThat(snapshot.count(), is(20L));
        assertThat(snapshot.totalSum(), is(expectedTotalSum));
        assertThat(snapshot.min(), is(NO_VALUE));
        assertThat(snapshot.max(), is(NO_VALUE));
        assertThat(snapshot.mean(), is(NO_VALUE_DOUBLE));

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

        assertThat(snapshot.bucketSize(Bucket.of(5, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(10, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(25, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(50, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(75, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(100, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(250, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(500, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(750, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(1000, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(2500, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(5000, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(7500, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(10000, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(INF_BUCKET), is(NO_VALUE));

        h.update(ms(1));
        h.update(ms(3));
        h.update(ms(3));
        h.update(ms(25));
        h.update(ms(27));
        h.update(ms(27));
        h.update(ms(27));
        h.update(ms(32));
        h.update(ms(75));
        h.update(ms(77));
        h.update(ms(125));
        h.update(ms(235));
        h.update(ms(48));
        h.update(ms(778));
        h.update(ms(778));
        h.update(ms(778));
        h.update(ms(275));
        h.update(ms(500));
        h.update(ms(500));
        h.update(ms(8000));

        expectedTotalSum *= 2;

        for (int i = 0; i < 10; ++i) {
            snapshot = h.snapshot();
            assertThat(snapshot.count(), is(40L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(ms(5)));
            assertThat(snapshot.max(), is(ms(10000)));
            assertThat(snapshot.mean(), is(expectedMean));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(percentileValueMs(5)));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(percentileValueMs(5)));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(percentileValueMs(5)));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(percentileValueMs(50)));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(percentileValueMs(50)));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(percentileValueMs(50)));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(percentileValueMs(75)));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(percentileValueMs(100)));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(percentileValueMs(500)));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(percentileValueMs(500)));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(percentileValueMs(500)));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(percentileValueMs(1000)));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(percentileValueMs(10000)));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(percentileValueMs(10000)));

            assertThat(snapshot.bucketSize(Bucket.of(5, MILLISECONDS)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(10, MILLISECONDS)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(25, MILLISECONDS)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(50, MILLISECONDS)), is(9L));
            assertThat(snapshot.bucketSize(Bucket.of(75, MILLISECONDS)), is(10L));
            assertThat(snapshot.bucketSize(Bucket.of(100, MILLISECONDS)), is(11L));
            assertThat(snapshot.bucketSize(Bucket.of(250, MILLISECONDS)), is(13L));
            assertThat(snapshot.bucketSize(Bucket.of(500, MILLISECONDS)), is(16L));
            assertThat(snapshot.bucketSize(Bucket.of(750, MILLISECONDS)), is(16L));
            assertThat(snapshot.bucketSize(Bucket.of(1000, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(2500, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(5000, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(7500, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(10000, MILLISECONDS)), is(20L));
            assertThat(snapshot.bucketSize(INF_BUCKET), is(20L));
        }

        timeNanosProvider.increaseMs(CHUNK_RESET_PERIOD_MS - 1);

        for (int i = 0; i < 10; ++i) {
            snapshot = h.snapshot();
            assertThat(snapshot.count(), is(40L));
            assertThat(snapshot.totalSum(), is(expectedTotalSum));
            assertThat(snapshot.min(), is(ms(5)));
            assertThat(snapshot.max(), is(ms(10000)));
            assertThat(snapshot.mean(), is(expectedMean));

            assertThat(snapshot.percentileValue(PERCENTILE_1), is(percentileValueMs(5)));
            assertThat(snapshot.percentileValue(PERCENTILE_5), is(percentileValueMs(5)));
            assertThat(snapshot.percentileValue(PERCENTILE_15), is(percentileValueMs(5)));
            assertThat(snapshot.percentileValue(PERCENTILE_25), is(percentileValueMs(50)));
            assertThat(snapshot.percentileValue(PERCENTILE_35), is(percentileValueMs(50)));
            assertThat(snapshot.percentileValue(PERCENTILE_45), is(percentileValueMs(50)));
            assertThat(snapshot.percentileValue(PERCENTILE_50), is(percentileValueMs(75)));
            assertThat(snapshot.percentileValue(PERCENTILE_55), is(percentileValueMs(100)));
            assertThat(snapshot.percentileValue(PERCENTILE_70), is(percentileValueMs(500)));
            assertThat(snapshot.percentileValue(PERCENTILE_75), is(percentileValueMs(500)));
            assertThat(snapshot.percentileValue(PERCENTILE_80), is(percentileValueMs(500)));
            assertThat(snapshot.percentileValue(PERCENTILE_95), is(percentileValueMs(1000)));
            assertThat(snapshot.percentileValue(PERCENTILE_99), is(percentileValueMs(10000)));
            assertThat(snapshot.percentileValue(PERCENTILE_999), is(percentileValueMs(10000)));

            assertThat(snapshot.bucketSize(Bucket.of(5, MILLISECONDS)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(10, MILLISECONDS)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(25, MILLISECONDS)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(50, MILLISECONDS)), is(9L));
            assertThat(snapshot.bucketSize(Bucket.of(75, MILLISECONDS)), is(10L));
            assertThat(snapshot.bucketSize(Bucket.of(100, MILLISECONDS)), is(11L));
            assertThat(snapshot.bucketSize(Bucket.of(250, MILLISECONDS)), is(13L));
            assertThat(snapshot.bucketSize(Bucket.of(500, MILLISECONDS)), is(16L));
            assertThat(snapshot.bucketSize(Bucket.of(750, MILLISECONDS)), is(16L));
            assertThat(snapshot.bucketSize(Bucket.of(1000, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(2500, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(5000, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(7500, MILLISECONDS)), is(19L));
            assertThat(snapshot.bucketSize(Bucket.of(10000, MILLISECONDS)), is(20L));
            assertThat(snapshot.bucketSize(INF_BUCKET), is(20L));
        }

        timeNanosProvider.increaseMs(1);

        snapshot = h.snapshot();
        assertThat(snapshot.count(), is(40L));
        assertThat(snapshot.totalSum(), is(expectedTotalSum));
        assertThat(snapshot.min(), is(NO_VALUE));
        assertThat(snapshot.max(), is(NO_VALUE));
        assertThat(snapshot.mean(), is(NO_VALUE_DOUBLE));

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

        assertThat(snapshot.bucketSize(Bucket.of(5, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(10, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(25, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(50, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(75, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(100, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(250, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(500, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(750, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(1000, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(2500, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(5000, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(7500, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(Bucket.of(10000, MILLISECONDS)), is(NO_VALUE));
        assertThat(snapshot.bucketSize(INF_BUCKET), is(NO_VALUE));

        h.metricInstanceRemoved();
    }

    /**
     * Test for bug <a href="https://github.com/ringcentral/metrics-facade/issues/44">Scale histogram: prevent overflow when calculating TOTAL_SUM and other measurables #44</a>
     */
    @Test
    public void issue_44_TotalSumShouldAddUpUpdateValuesInsteadOfBucketUpperBounds() {
        ResetByChunksScaleHistogramImpl h = new ResetByChunksScaleHistogramImpl(
            scaleImpl()
                .with(linearScale().from(0).steps(1, 2).withInf())
                .build(),
            Set.of(TOTAL_SUM, MEAN),
            executor,
            timeMsProvider);

        h.metricInstanceAdded();

        h.update(4);
        h.update(5);

        HistogramSnapshot snapshot = h.snapshot();
        assertThat(snapshot.totalSum(), is(9L));

        h.metricInstanceRemoved();
    }

    double percentileValueMs(long amount) {
        return (double)(ms(amount));
    }

    long ms(long amount) {
        return MILLISECONDS.toNanos(amount);
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
            return a[Math.max(index - 1, 0)];
        }
    }
}