package com.ringcentral.platform.metrics.guide.chapter01;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Listing01Test {

    @Test
    public void test() {
        // given
        var expected = "# HELP request_total Generated from metric instances with name request.total\n" +
                "# TYPE request_total gauge\n" +
                "request_total 1.0\n";

        // when
        var actual = Listing01.run();

        // then
        assertThat(actual).isEqualTo(expected);
    }
}