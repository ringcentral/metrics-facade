package com.ringcentral.platform.metrics.utils;

import org.junit.Test;

import static com.ringcentral.platform.metrics.utils.TimeUnitUtils.convertTimeUnit;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TimeUnitUtilsTest {

    @Test
    public void convertingTimeUnit() {
        assertThat(convertTimeUnit(1.7, SECONDS, MILLISECONDS), is(1700.0));
        assertThat(convertTimeUnit(1.7, MILLISECONDS, SECONDS), is(0.0017));
        assertThat(convertTimeUnit(1.7, MILLISECONDS, NANOSECONDS), is(1700000.0));
        assertThat(convertTimeUnit(1.7, NANOSECONDS, MILLISECONDS), is(1.7E-6));
    }
}