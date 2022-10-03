package com.ringcentral.platform.metrics.histogram;

import org.junit.Test;

import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings("EqualsWithItself")
public class HistogramTest {

    @Test
    public void sortingBuckets() {
        assertThat(Bucket.of(1).compareTo(Bucket.of(1)), is(0));
        assertThat(Bucket.of(1).compareTo(Bucket.of(2)), is(-1));
        assertThat(Bucket.of(2).compareTo(Bucket.of(1)), is(1));
        assertThat(Bucket.of(1).compareTo(INF_BUCKET), is(-1));
        assertThat(INF_BUCKET.compareTo(Bucket.of(2)), is(1));

        assertThat(Bucket.of(1.5).compareTo(Bucket.of(1.5)), is(0));
        assertThat(Bucket.of(1.5).compareTo(Bucket.of(1.7)), is(-1));
        assertThat(Bucket.of(1.7).compareTo(Bucket.of(1.5)), is(1));
        assertThat(Bucket.of(1.5).compareTo(INF_BUCKET), is(-1));
        assertThat(INF_BUCKET.compareTo(Bucket.of(1.7)), is(1));

        assertThat(INF_BUCKET.compareTo(INF_BUCKET), is(0));
    }

    @Test
    public void sortingPercentiles() {
        assertThat(PERCENTILE_1.compareTo(PERCENTILE_1), is(0));
        assertThat(PERCENTILE_1.compareTo(PERCENTILE_5), is(-1));
        assertThat(PERCENTILE_5.compareTo(PERCENTILE_1), is(1));
    }

    @Test
    public void bucketStringRepresentations() {
        Bucket bucket = Bucket.of(25.5);
        assertThat(bucket.upperBoundAsString(), is("25p5"));
        assertThat(bucket.upperBoundAsStringWithUnit(), is("25p5ns"));
        assertThat(bucket.upperBoundAsNumberString(), is("25.5"));
        assertThat(bucket.upperBoundSecAsNumberString(), is("2.55E-8"));

        bucket = Bucket.of(10);
        assertThat(bucket.upperBoundAsString(), is("10"));
        assertThat(bucket.upperBoundAsStringWithUnit(), is("10ns"));
        assertThat(bucket.upperBoundAsNumberString(), is("10"));
        assertThat(bucket.upperBoundSecAsNumberString(), is("1.0E-8"));

        bucket = Bucket.of(1, MILLISECONDS);
        assertThat(bucket.upperBoundAsString(), is("1000000"));
        assertThat(bucket.upperBoundAsStringWithUnit(), is("1ms"));
        assertThat(bucket.upperBoundAsNumberString(), is("1000000"));
        assertThat(bucket.upperBoundSecAsNumberString(), is("0.001"));

        bucket = Bucket.of(2.5, MILLISECONDS);
        assertThat(bucket.upperBoundAsString(), is("2500000"));
        assertThat(bucket.upperBoundAsStringWithUnit(), is("2p5ms"));
        assertThat(bucket.upperBoundAsNumberString(), is("2500000"));
        assertThat(bucket.upperBoundSecAsNumberString(), is("0.0025"));

        bucket = Bucket.of(Double.POSITIVE_INFINITY);
        assertThat(bucket.upperBoundAsString(), is("inf"));
        assertThat(bucket.upperBoundAsStringWithUnit(), is("inf"));
        assertThat(bucket.upperBoundAsNumberString(), is("Infinity"));
        assertThat(bucket.upperBoundSecAsNumberString(), is("Infinity"));

        bucket = Bucket.of(Double.NEGATIVE_INFINITY);
        assertThat(bucket.upperBoundAsString(), is("negativeInf"));
        assertThat(bucket.upperBoundAsStringWithUnit(), is("negativeInf"));
        assertThat(bucket.upperBoundAsNumberString(), is("-Infinity"));
        assertThat(bucket.upperBoundSecAsNumberString(), is("-Infinity"));
    }
}