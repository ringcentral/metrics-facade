package com.ringcentral.platform.metrics.guide.chapter02;

import com.ringcentral.platform.metrics.guide.chapter02.Listing02;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Listing02Test {

    @Test
    public void test() {
        // given
        var expected = "# HELP request_duration_seconds Generated from metric instances with name request.duration.seconds\n" +
                "# TYPE request_duration_seconds histogram\n" +
                "request_duration_seconds_bucket{le=\"5\",} 1.0\n" +
                "request_duration_seconds_bucket{le=\"10\",} 2.0\n" +
                "request_duration_seconds_bucket{le=\"15\",} 3.0\n" +
                "request_duration_seconds_bucket{le=\"+Inf\",} 3.0\n" +
                "request_duration_seconds_count 3.0\n" +
                "request_duration_seconds_sum 30.0\n";

        // when
        var actual = Listing02.run();

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
