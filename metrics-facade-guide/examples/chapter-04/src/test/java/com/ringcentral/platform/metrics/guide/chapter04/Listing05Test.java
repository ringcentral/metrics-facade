package com.ringcentral.platform.metrics.guide.chapter04;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Listing05Test {

    @Test
    public void test() {
        // given
        var expected =
                "# HELP third_party_histogram Generated from metric instances with name third.party.histogram\n" +
                        "# TYPE third_party_histogram summary\n" +
                        "third_party_histogram{quantile=\"0.99\",} 9.0\n" +
                        "third_party_histogram_count 10.0\n" +
                        "third_party_histogram_sum 45.0\n" +
                        "# HELP app_histogram Generated from metric instances with name app.histogram\n" +
                        "# TYPE app_histogram summary\n" +
                        "app_histogram{quantile=\"0.5\",} 4.0\n" +
                        "app_histogram{quantile=\"0.9\",} 8.0\n" +
                        "app_histogram{quantile=\"0.99\",} 9.0\n" +
                        "app_histogram_count 10.0\n" +
                        "app_histogram_sum 45.0\n";

        // when
        var actual = Listing05.run();

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
