package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;
import org.junit.Test;

import static com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot.*;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class AbstractTotalsHistogramImplTest<H extends HistogramImpl> {

    protected abstract H makeHistogramImpl();

    @Test
    public void updateAndSnapshot() {
        // given
        HistogramImpl histogram = makeHistogramImpl();

        // then
        checkSnapshot(histogram, 0, 0);

        // when
        histogram.update(0);

        // then
        checkSnapshot(histogram, 1, 0);

        // when
        histogram.update(1);

        // then
        checkSnapshot(histogram, 2, 1);

        // when
        histogram.update(2);

        // then
        checkSnapshot(histogram, 3, 3);

        // when
        histogram.update(5);

        // then
        checkSnapshot(histogram, 4, 8);
    }

    void checkSnapshot(HistogramImpl histogram, long expectedCount, long expectedTotalSum) {
        HistogramSnapshot snapshot = histogram.snapshot();
        assertThat(snapshot.count(), is(expectedCount));
        assertThat(snapshot.totalSum(), is(expectedTotalSum));
        assertThat(snapshot.min(), is(NO_VALUE));
        assertThat(snapshot.max(), is(NO_VALUE));
        assertThat(snapshot.mean(), is(NO_VALUE_DOUBLE));
        assertThat(snapshot.standardDeviation(), is(NO_VALUE_DOUBLE));
        assertThat(snapshot.percentileValue(PERCENTILE_50), is(NO_VALUE_DOUBLE));
        assertThat(snapshot.bucketSize(MS_1_BUCKET), is(NO_VALUE));
    }
}