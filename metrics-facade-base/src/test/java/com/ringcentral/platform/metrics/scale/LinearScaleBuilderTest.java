package com.ringcentral.platform.metrics.scale;

import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.scale.LinearScaleBuilder.linear;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LinearScaleBuilderTest {

    @Test
    public void build() {
        Scale scale = linear().from(100).steps(25, 4).withInf().build();
        assertThat(scale.points(), is(List.of(100L, 125L, 150L, 175L, 200L, Long.MAX_VALUE)));

        scale = linear().steps(25, 4).build();
        assertThat(scale.points(), is(List.of(0L, 25L, 50L, 75L, 100L)));
    }
}