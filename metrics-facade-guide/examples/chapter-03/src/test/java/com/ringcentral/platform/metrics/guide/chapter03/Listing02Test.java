package com.ringcentral.platform.metrics.guide.chapter03;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Listing02Test {

    @Test
    public void test() throws InterruptedException {
        // given
        var expected =
                "# HELP request_total Generated from metric instances with name request.total\n" +
                        "# TYPE request_total gauge\n" +
                        "request_total{service=\"service-3\",} 1.0\n" +
                        "request_total{service=\"service-4\",} 1.0\n" +
                        "request_total{service=\"service-5\",} 1.0\n";

        // when
        var actual = Listing02.run();

        // then
        assertThat(actual).isEqualTo(expected);
    }
}