package com.ringcentral.platform.metrics.scale;

import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.scale.SpecificScaleBuilder.points;
import static java.lang.Long.MAX_VALUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SpecificScaleBuilderTest {

    @Test
    public void build() {
        Scale scale = new SpecificScaleBuilder(List.of(1L, 2L, 3L)).build();
        assertThat(scale.points(), is(List.of(1L, 2L, 3L)));

        scale = points(
            MILLISECONDS,
            5, 10, 25, 50, 75, 100, 250, 500, 750, 1000,
            2500, 5000, 7500, 10000, MAX_VALUE).build();

        assertThat(scale.points(), is(List.of(
            MILLISECONDS.toNanos(5),
            MILLISECONDS.toNanos(10),
            MILLISECONDS.toNanos(25),
            MILLISECONDS.toNanos(50),
            MILLISECONDS.toNanos(75),
            MILLISECONDS.toNanos(100),
            MILLISECONDS.toNanos(250),
            MILLISECONDS.toNanos(500),
            MILLISECONDS.toNanos(750),
            MILLISECONDS.toNanos(1000),
            MILLISECONDS.toNanos(2500),
            MILLISECONDS.toNanos(5000),
            MILLISECONDS.toNanos(7500),
            MILLISECONDS.toNanos(10000),
            MAX_VALUE)));
    }
}