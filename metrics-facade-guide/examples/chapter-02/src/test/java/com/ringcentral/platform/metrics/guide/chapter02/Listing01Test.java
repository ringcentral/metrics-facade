package com.ringcentral.platform.metrics.guide.chapter02;

import com.ringcentral.platform.metrics.guide.chapter02.Listing01;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Listing01Test {

    @Test
    public void test() {
        // given
        var expected = "# HELP request_duration_seconds Generated from metric instances with name request.duration.seconds\n" +
                "# TYPE request_duration_seconds summary\n" +
                "request_duration_seconds{quantile=\"0.5\",} 10.0\n" +
                "request_duration_seconds{quantile=\"0.9\",} 15.0\n" +
                "request_duration_seconds{quantile=\"0.99\",} 15.0\n" +
                "request_duration_seconds_count 3.0\n" +
                "# HELP request_duration_seconds_mean Generated from metric instances with name request.duration.seconds\n" +
                "# TYPE request_duration_seconds_mean gauge\n" +
                "request_duration_seconds_mean 10.0\n" +
                "# HELP request_duration_seconds_min Generated from metric instances with name request.duration.seconds\n" +
                "# TYPE request_duration_seconds_min gauge\n" +
                "request_duration_seconds_min 5.0\n" +
                "# HELP request_duration_seconds_max Generated from metric instances with name request.duration.seconds\n" +
                "# TYPE request_duration_seconds_max gauge\n" +
                "request_duration_seconds_max 15.0\n";

        // when
        var actual = Listing01.run();

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
