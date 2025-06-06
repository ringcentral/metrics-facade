package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.resetOnSnapshot;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImplTest;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;
import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.TotalsMeasurementType;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.scale.LinearScaleBuilder;
import com.ringcentral.platform.metrics.test.time.TestScheduledExecutorService;
import com.ringcentral.platform.metrics.test.time.TestTimeNanosProvider;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot.*;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfigBuilder.scaleImpl;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.scale.LinearScaleBuilder.linearScale;
import static java.lang.Math.sqrt;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ResetOnSnapshotScaleHistogramImplTest extends AbstractHistogramImplTest<ResetOnSnapshotScaleHistogramImpl> {

    static final LinearScaleBuilder SCALE_1 = linearScale().steps(1, 100);
    static final TestTimeNanosProvider timeNanosProvider = new TestTimeNanosProvider();
    static final ScheduledExecutorService executor = new TestScheduledExecutorService(timeNanosProvider);

    @Override
    protected ResetOnSnapshotScaleHistogramImpl makeHistogramImpl(@Nonnull TotalsMeasurementType totalsMeasurementType, @Nonnull Measurable... measurables) {
        return new ResetOnSnapshotScaleHistogramImpl(
            scaleImpl().resetOnSnapshot().totals(totalsMeasurementType).build(),
            Set.of(measurables),
            executor);
    }

    @Test
    public void scale_1_AllMeasurables_NeverResetBuckets() {
        ResetOnSnapshotScaleHistogramImpl h = new ResetOnSnapshotScaleHistogramImpl(
            scaleImpl().resetOnSnapshot().with(SCALE_1).build(),
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

        for (int i = 1; i <= 10; ++i) {
            long expectedTotalSum = 0L;

            for (int v = 1; v <= 100; ++v) {
                h.update(v);
                expectedTotalSum += v;
            }

            double expectedMean = (1.0 * expectedTotalSum) / 100L;
            expectedTotalSum *= i;
            double expectedGeometricDeviationSum = 0.0;

            for (int v = 1; v <= 100; ++v) {
                double deviation = (v * 1.0) - expectedMean;
                expectedGeometricDeviationSum += deviation * deviation;
            }

            double expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 100);

            HistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(100L * i));
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
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L * i));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L * i));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L * i));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L * i));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L * i));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L * i));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(88L * i));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(100L * i));

            snapshot = h.snapshot();
            assertThat(snapshot.count(), is(100L * i));
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
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L * i));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L * i));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L * i));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L * i));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L * i));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L * i));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(88L * i));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(100L * i));
        }

        h.metricInstanceRemoved();
    }

    @Test
    public void scale_1_AllMeasurables_ResettableBuckets() {
        ResetOnSnapshotScaleHistogramImpl h = new ResetOnSnapshotScaleHistogramImpl(
            scaleImpl()
                .resetOnSnapshot()
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
            executor);

        h.metricInstanceAdded();

        for (int i = 1; i <= 10; ++i) {
            long expectedTotalSum = 0L;

            for (int v = 1; v <= 100; ++v) {
                h.update(v);
                expectedTotalSum += v;
            }

            double expectedMean = (1.0 * expectedTotalSum) / 100L;
            expectedTotalSum *= i;
            double expectedGeometricDeviationSum = 0.0;

            for (int v = 1; v <= 100; ++v) {
                double deviation = (v * 1.0) - expectedMean;
                expectedGeometricDeviationSum += deviation * deviation;
            }

            double expectedStandardDeviation = sqrt(expectedGeometricDeviationSum / 100);

            HistogramSnapshot snapshot = h.snapshot();
            assertThat(snapshot.count(), is(100L * i));
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
            assertThat(snapshot.bucketSize(Bucket.of(2)), is(2L));
            assertThat(snapshot.bucketSize(Bucket.of(3)), is(3L));
            assertThat(snapshot.bucketSize(Bucket.of(4)), is(4L));
            assertThat(snapshot.bucketSize(Bucket.of(5)), is(5L));
            assertThat(snapshot.bucketSize(Bucket.of(27)), is(27L));
            assertThat(snapshot.bucketSize(Bucket.of(50)), is(50L));
            assertThat(snapshot.bucketSize(Bucket.of(88)), is(88L));
            assertThat(snapshot.bucketSize(Bucket.of(107)), is(100L));

            snapshot = h.snapshot();
            assertThat(snapshot.count(), is(100L * i));
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
        }

        h.metricInstanceRemoved();
    }
}