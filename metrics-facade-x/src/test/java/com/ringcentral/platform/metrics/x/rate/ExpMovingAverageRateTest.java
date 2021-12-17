package com.ringcentral.platform.metrics.x.rate;

import com.ringcentral.platform.metrics.test.time.TestTimeNanosProvider;
import org.junit.Test;

import java.util.Set;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.rate.Rate.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExpMovingAverageRateTest {

    TestTimeNanosProvider timeNanosProvider = new TestTimeNanosProvider();

    ExpMovingAverageRate rate = new ExpMovingAverageRate(
        ExpMovingAverageRateConfig.DEFAULT,
        Set.of(COUNT, MEAN_RATE, ONE_MINUTE_RATE, FIVE_MINUTES_RATE, FIFTEEN_MINUTES_RATE),
        timeNanosProvider);

    @Test
    public void count_And_Rates() {
        assertThat(rate.count(), is(0L));
        assertThat(rate.meanRate(), is(0.0));
        assertThat(rate.oneMinuteRate(), is(0.0));
        assertThat(rate.fiveMinutesRate(), is(0.0));
        assertThat(rate.fifteenMinutesRate(), is(0.0));

        timeNanosProvider.increaseSec(1L);
        rate.mark(10L);

        assertThat(rate.count(), is(10L));
        assertThat(rate.meanRate(), is(10.0));
        assertThat(rate.oneMinuteRate(), is(0.0));
        assertThat(rate.fiveMinutesRate(), is(0.0));
        assertThat(rate.fifteenMinutesRate(), is(0.0));

        timeNanosProvider.increaseSec(4L);

        assertThat(rate.count(), is(10L));
        assertThat(rate.meanRate(), is(2.0));
        assertThat(rate.oneMinuteRate(), is(0.0));
        assertThat(rate.fiveMinutesRate(), is(0.0));
        assertThat(rate.fifteenMinutesRate(), is(0.0));

        for (int i = 0; i < 84 - 1; ++i) {
            timeNanosProvider.increaseSec(5L);
            rate.mark(10L);
        }

        assertThat(rate.count(), is(840L));
        assertThat(rate.meanRate(), is(2.0));
        assertThat(rate.oneMinuteRate(), is(1.9166323122565685));
        assertThat(rate.fiveMinutesRate(), is(1.9791547932925346));
        assertThat(rate.fifteenMinutesRate(), is(1.990941231028341));
    }
}