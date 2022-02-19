package com.ringcentral.platform.metrics.scale;

import org.junit.Test;

import static com.ringcentral.platform.metrics.scale.ExpStepScaleBuilder.expStep;
import static java.util.List.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExpStepScaleBuilderTest {

    @Test
    public void build() {
        Scale scale = expStep().base(2).factor(2).steps(3).withInf().build();
        assertThat(scale.points(), is(of(0L, 2L, 6L, 14L, Long.MAX_VALUE)));

        scale = expStep().from(100).base(2).factor(2).steps(3).withInf().build();
        assertThat(scale.points(), is(of(100L, 102L, 106L, 114L, Long.MAX_VALUE)));

        scale = expStep().from(100).base(2).factor(1.5).steps(30).max(300).build();
        assertThat(scale.points(), is(of(100L, 102L, 105L, 109L, 115L, 124L, 137L, 156L, 184L, 226L, 289L)));

        scale = expStep().from(2).base(2).factor(2).steps(5).withInf().build();
        assertThat(scale.points(), is(of(2L, 4L, 8L, 16L, 32L, 64L, Long.MAX_VALUE)));
    }
}