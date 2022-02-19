package com.ringcentral.platform.metrics.scale;

import org.junit.Test;

import java.util.*;

import static com.ringcentral.platform.metrics.scale.ExpScaleBuilder.exp;
import static java.lang.Math.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExpScaleBuilderTest {

    @Test
    public void build() {
        Scale scale = exp().factor(2).withInf().build();
        List<Long> expectedPoints = new ArrayList<>();
        expectedPoints.add(0L);

        for (int i = 0; i < 63; ++i) {
            expectedPoints.add(round(pow(2, i)));
        }

        expectedPoints.add(Long.MAX_VALUE);
        assertThat(scale.points(), is(expectedPoints));

        scale = exp().from(1).factor(2).steps(5).build();
        expectedPoints.add(Long.MAX_VALUE);
        assertThat(scale.points(), is(List.of(1L, 2L, 4L, 8L, 16L, 32L)));
    }
}