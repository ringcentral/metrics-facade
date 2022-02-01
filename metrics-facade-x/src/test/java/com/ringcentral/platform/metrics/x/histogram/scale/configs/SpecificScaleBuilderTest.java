package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SpecificScaleBuilderTest {

    @Test
    public void build() {
        Scale scale = new SpecificScaleBuilder(List.of(1L, 2L, 3L)).build();
        assertThat(scale.points(), is(List.of(1L, 2L, 3L)));
    }
}