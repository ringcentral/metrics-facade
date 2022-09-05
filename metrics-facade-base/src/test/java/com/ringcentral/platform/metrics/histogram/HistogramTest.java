package com.ringcentral.platform.metrics.histogram;

import com.ringcentral.platform.metrics.histogram.Histogram.Bucket;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HistogramTest {

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