package com.ringcentral.platform.metrics.guide.chapter03;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Listing03Test {

    @Test
    public void test() throws InterruptedException {
        // given
        var expected =
                "Before expiration time\n" +
                        "# HELP request_total Generated from metric instances with name request.total\n" +
                        "# TYPE request_total gauge\n" +
                        "request_total{service=\"service-1\",} 1.0\n" +
                        "request_total{service=\"service-2\",} 1.0\n" +
                        "request_total{service=\"service-3\",} 1.0\n" +
                        "request_total{service=\"service-4\",} 1.0\n" +
                        "request_total{service=\"service-5\",} 1.0\n" +
                        "After expiration time\n";

        // when
        var actual = Listing04.run();

        // then
        assertThat(actual).isEqualTo(expected);
    }
}