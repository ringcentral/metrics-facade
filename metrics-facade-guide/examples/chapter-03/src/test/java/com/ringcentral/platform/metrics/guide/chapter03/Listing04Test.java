package com.ringcentral.platform.metrics.guide.chapter03;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Listing04Test {

    @Test
    public void test() throws InterruptedException {
        // given
        var expected =
                "# HELP request_total Generated from metric instances with name request.total\n" +
                        "# TYPE request_total gauge\n" +
                        "request_total{service=\"auth\",server=\"auth-server-1\",port=\"8080\",} 1.0\n" +
                        "request_total{service=\"auth\",server=\"auth-server-2\",port=\"8080\",} 10.0\n" +
                        "request_total{service=\"contacts\",server=\"contact-server-1\",port=\"8081\",} 3.0\n";

        // when
        var actual = Listing04.run();

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
