package com.ringcentral.platform.metrics.guide.chapter03;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Listing03Test {

    @Test
    public void test() throws InterruptedException {
        // given
        var expected =
                "# HELP request_total Generated from metric instances with name request.total\n" +
                        "# TYPE request_total gauge\n" +
                        "request_total{service=\"auth\",server=\"auth-server-5\",port=\"8080\",} 1.0\n" +
                        "request_total{service=\"auth\",server=\"auth-server-4\",port=\"8080\",} 1.0\n" +
                        "request_total{service=\"auth\",server=\"auth-server-3\",port=\"8080\",} 1.0\n";

        // when
        var actual = Listing03.run();

        // then
        assertThat(actual).isEqualTo(expected);
    }
}