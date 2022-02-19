package com.ringcentral.platform.metrics.scale;

import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.scale.CompositeScaleBuilder.first;
import static com.ringcentral.platform.metrics.scale.LinearScaleBuilder.linear;
import static com.ringcentral.platform.metrics.scale.SpecificScaleBuilder.points;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CompositeScaleBuilderTest {

    @Test
    public void build() {
        Scale scale = first(linear().steps(25, 4))
            .then(points(List.of(10L, 20L)))
            .then(linear().steps(25, 2).withInf()).build();

        assertThat(scale.points(), is(List.of(0L, 25L, 50L, 75L, 100L, 110L, 120L, 145L, 170L, Long.MAX_VALUE)));
    }
}