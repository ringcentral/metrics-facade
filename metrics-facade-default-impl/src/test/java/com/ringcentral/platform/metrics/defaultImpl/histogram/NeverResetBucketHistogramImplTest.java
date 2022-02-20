package com.ringcentral.platform.metrics.defaultImpl.histogram;

import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot.NO_VALUE;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.configs.TotalsMeasurementType.CONSISTENT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NeverResetBucketHistogramImplTest {

    @Test
    public void withCount_WithTotalSum_TotalsConsistent() {
        NeverResetBucketHistogramImpl h = new NeverResetBucketHistogramImpl(
            true,
            true,
            CONSISTENT,
            List.of(MS_10_BUCKET, MS_100_BUCKET, SEC_1_BUCKET));

        // v <= 10 ms
        h.update(MILLISECONDS.toNanos(1));
        h.update(MILLISECONDS.toNanos(5));
        h.update(MILLISECONDS.toNanos(10));

        // 10 ms < v <= 100 ms
        h.update(MILLISECONDS.toNanos(50L));
        h.update(MILLISECONDS.toNanos(95L));

        // 100 ms < v <= 1 sec
        h.update(MILLISECONDS.toNanos(500L));

        HistogramSnapshot snapshot = h.snapshot();
        assertThat(snapshot.count(), is(6L));
        assertThat(snapshot.totalSum(), is(MILLISECONDS.toNanos(1L + 5L + 10L + 50L + 95L + 500L)));
        assertThat(snapshot.bucketSize(MS_10_BUCKET), is(3L));
        assertThat(snapshot.bucketSize(MS_100_BUCKET), is(5L));
        assertThat(snapshot.bucketSize(SEC_1_BUCKET), is(6L));
        assertThat(snapshot.bucketSize(INF_BUCKET), is(6L));

        // 10 ms < v <= 100 ms
        h.update(MILLISECONDS.toNanos(50L));
        h.update(MILLISECONDS.toNanos(95L));

        // 100 ms < v <= 1 sec
        h.update(MILLISECONDS.toNanos(500L));

        // 1 sec < v
        h.update(SECONDS.toNanos(2L));

        snapshot = h.snapshot();
        assertThat(snapshot.count(), is(10L));
        assertThat(snapshot.totalSum(), is(MILLISECONDS.toNanos(1L + 5L + 10L + 50L + 95L + 500L + 50L + 95L + 500L) + SECONDS.toNanos(2L)));
        assertThat(snapshot.bucketSize(MS_10_BUCKET), is(3L));
        assertThat(snapshot.bucketSize(MS_100_BUCKET), is(7L));
        assertThat(snapshot.bucketSize(SEC_1_BUCKET), is(9L));
        assertThat(snapshot.bucketSize(INF_BUCKET), is(10L));
    }

    @Test
    public void withCount_NoTotalSum() {
        NeverResetBucketHistogramImpl h = new NeverResetBucketHistogramImpl(
            true,
            false,
            CONSISTENT,
            List.of(MS_10_BUCKET, MS_100_BUCKET, SEC_1_BUCKET));

        // v <= 10 ms
        h.update(MILLISECONDS.toNanos(1));
        h.update(MILLISECONDS.toNanos(5));
        h.update(MILLISECONDS.toNanos(10));

        // 10 ms < v <= 100 ms
        h.update(MILLISECONDS.toNanos(50L));
        h.update(MILLISECONDS.toNanos(95L));

        // 100 ms < v <= 1 sec
        h.update(MILLISECONDS.toNanos(500L));

        HistogramSnapshot snapshot = h.snapshot();
        assertThat(snapshot.count(), is(6L));
        assertThat(snapshot.totalSum(), is(NO_VALUE));
        assertThat(snapshot.bucketSize(MS_10_BUCKET), is(3L));
        assertThat(snapshot.bucketSize(MS_100_BUCKET), is(5L));
        assertThat(snapshot.bucketSize(SEC_1_BUCKET), is(6L));
        assertThat(snapshot.bucketSize(INF_BUCKET), is(6L));

        // 10 ms < v <= 100 ms
        h.update(MILLISECONDS.toNanos(50L));
        h.update(MILLISECONDS.toNanos(95L));

        // 100 ms < v <= 1 sec
        h.update(MILLISECONDS.toNanos(500L));

        // 1 sec < v
        h.update(SECONDS.toNanos(2L));

        snapshot = h.snapshot();
        assertThat(snapshot.count(), is(10L));
        assertThat(snapshot.totalSum(), is(NO_VALUE));
        assertThat(snapshot.bucketSize(MS_10_BUCKET), is(3L));
        assertThat(snapshot.bucketSize(MS_100_BUCKET), is(7L));
        assertThat(snapshot.bucketSize(SEC_1_BUCKET), is(9L));
        assertThat(snapshot.bucketSize(INF_BUCKET), is(10L));
    }

    @Test
    public void noCount_WithTotalSum() {
        NeverResetBucketHistogramImpl h = new NeverResetBucketHistogramImpl(
            false,
            true,
            CONSISTENT,
            List.of(MS_10_BUCKET, MS_100_BUCKET, SEC_1_BUCKET));

        // v <= 10 ms
        h.update(MILLISECONDS.toNanos(1));
        h.update(MILLISECONDS.toNanos(5));
        h.update(MILLISECONDS.toNanos(10));

        // 10 ms < v <= 100 ms
        h.update(MILLISECONDS.toNanos(50L));
        h.update(MILLISECONDS.toNanos(95L));

        // 100 ms < v <= 1 sec
        h.update(MILLISECONDS.toNanos(500L));

        HistogramSnapshot snapshot = h.snapshot();
        assertThat(snapshot.count(), is(NO_VALUE));
        assertThat(snapshot.totalSum(), is(MILLISECONDS.toNanos(1L + 5L + 10L + 50L + 95L + 500L)));
        assertThat(snapshot.bucketSize(MS_10_BUCKET), is(3L));
        assertThat(snapshot.bucketSize(MS_100_BUCKET), is(5L));
        assertThat(snapshot.bucketSize(SEC_1_BUCKET), is(6L));
        assertThat(snapshot.bucketSize(INF_BUCKET), is(6L));

        // 10 ms < v <= 100 ms
        h.update(MILLISECONDS.toNanos(50L));
        h.update(MILLISECONDS.toNanos(95L));

        // 100 ms < v <= 1 sec
        h.update(MILLISECONDS.toNanos(500L));

        // 1 sec < v
        h.update(SECONDS.toNanos(2L));

        snapshot = h.snapshot();
        assertThat(snapshot.count(), is(NO_VALUE));
        assertThat(snapshot.totalSum(), is(MILLISECONDS.toNanos(1L + 5L + 10L + 50L + 95L + 500L + 50L + 95L + 500L) + SECONDS.toNanos(2L)));
        assertThat(snapshot.bucketSize(MS_10_BUCKET), is(3L));
        assertThat(snapshot.bucketSize(MS_100_BUCKET), is(7L));
        assertThat(snapshot.bucketSize(SEC_1_BUCKET), is(9L));
        assertThat(snapshot.bucketSize(INF_BUCKET), is(10L));
    }

    @Test
    public void noCount_NoTotalSum() {
        NeverResetBucketHistogramImpl h = new NeverResetBucketHistogramImpl(
            false,
            false,
            CONSISTENT,
            List.of(MS_10_BUCKET, MS_100_BUCKET, SEC_1_BUCKET));

        // v <= 10 ms
        h.update(MILLISECONDS.toNanos(1));
        h.update(MILLISECONDS.toNanos(5));
        h.update(MILLISECONDS.toNanos(10));

        // 10 ms < v <= 100 ms
        h.update(MILLISECONDS.toNanos(50L));
        h.update(MILLISECONDS.toNanos(95L));

        // 100 ms < v <= 1 sec
        h.update(MILLISECONDS.toNanos(500L));

        HistogramSnapshot snapshot = h.snapshot();
        assertThat(snapshot.count(), is(NO_VALUE));
        assertThat(snapshot.totalSum(), is(NO_VALUE));
        assertThat(snapshot.bucketSize(MS_10_BUCKET), is(3L));
        assertThat(snapshot.bucketSize(MS_100_BUCKET), is(5L));
        assertThat(snapshot.bucketSize(SEC_1_BUCKET), is(6L));
        assertThat(snapshot.bucketSize(INF_BUCKET), is(6L));

        // 10 ms < v <= 100 ms
        h.update(MILLISECONDS.toNanos(50L));
        h.update(MILLISECONDS.toNanos(95L));

        // 100 ms < v <= 1 sec
        h.update(MILLISECONDS.toNanos(500L));

        // 1 sec < v
        h.update(SECONDS.toNanos(2L));

        snapshot = h.snapshot();
        assertThat(snapshot.count(), is(NO_VALUE));
        assertThat(snapshot.totalSum(), is(NO_VALUE));
        assertThat(snapshot.bucketSize(MS_10_BUCKET), is(3L));
        assertThat(snapshot.bucketSize(MS_100_BUCKET), is(7L));
        assertThat(snapshot.bucketSize(SEC_1_BUCKET), is(9L));
        assertThat(snapshot.bucketSize(INF_BUCKET), is(10L));
    }

    @Test
    public void withCount_WithTotalSum_InfBucket_Only() {
        NeverResetBucketHistogramImpl h = new NeverResetBucketHistogramImpl(
            true,
            true,
            CONSISTENT,
            List.of(INF_BUCKET));

        // v <= 10 ms
        h.update(MILLISECONDS.toNanos(1));
        h.update(MILLISECONDS.toNanos(5));
        h.update(MILLISECONDS.toNanos(10));

        // 10 ms < v <= 100 ms
        h.update(MILLISECONDS.toNanos(50L));
        h.update(MILLISECONDS.toNanos(95L));

        // 100 ms < v <= 1 sec
        h.update(MILLISECONDS.toNanos(500L));

        HistogramSnapshot snapshot = h.snapshot();
        assertThat(snapshot.count(), is(6L));
        assertThat(snapshot.totalSum(), is(MILLISECONDS.toNanos(1L + 5L + 10L + 50L + 95L + 500L)));
        assertThat(snapshot.bucketSize(INF_BUCKET), is(6L));

        // 10 ms < v <= 100 ms
        h.update(MILLISECONDS.toNanos(50L));
        h.update(MILLISECONDS.toNanos(95L));

        // 100 ms < v <= 1 sec
        h.update(MILLISECONDS.toNanos(500L));

        // 1 sec < v
        h.update(SECONDS.toNanos(2L));

        snapshot = h.snapshot();
        assertThat(snapshot.count(), is(10L));
        assertThat(snapshot.totalSum(), is(MILLISECONDS.toNanos(1L + 5L + 10L + 50L + 95L + 500L + 50L + 95L + 500L) + SECONDS.toNanos(2L)));
        assertThat(snapshot.bucketSize(INF_BUCKET), is(10L));
    }
}