package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;
import org.junit.Test;

import static com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot.*;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CountHistogramImplTest {

    @Test
    public void updateAndSnapshot() {
        // given
        CountHistogramImpl histogram = new CountHistogramImpl();

        // then
        checkSnapshot(histogram, 0);

        // when
        histogram.update(0);

        // then
        checkSnapshot(histogram, 1);

        // when
        histogram.update(1);

        // then
        checkSnapshot(histogram, 2);

        // when
        histogram.update(2);

        // then
        checkSnapshot(histogram, 3);

        // when
        histogram.update(5);

        // then
        checkSnapshot(histogram, 4);
    }

    void checkSnapshot(CountHistogramImpl histogram, long expectedCount) {
        HistogramSnapshot snapshot = histogram.snapshot();
        assertThat(snapshot.count(), is(expectedCount));
        assertThat(snapshot.totalSum(), is(NO_VALUE));
        assertThat(snapshot.min(), is(NO_VALUE));
        assertThat(snapshot.max(), is(NO_VALUE));
        assertThat(snapshot.mean(), is(NO_VALUE_DOUBLE));
        assertThat(snapshot.standardDeviation(), is(NO_VALUE_DOUBLE));
        assertThat(snapshot.percentileValue(PERCENTILE_50), is(NO_VALUE_DOUBLE));
        assertThat(snapshot.bucketSize(MS_1_BUCKET), is(NO_VALUE));
    }
}