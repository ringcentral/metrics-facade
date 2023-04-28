package com.ringcentral.platform.metrics.guide.chapter04;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Listing02Test {

    @Test
    public void test() {
        // given
        var expected =
                "# HELP histogram_override_defaultAndPreConfigured_measurables Generated from metric instances with name histogram.override.defaultAndPreConfigured.measurables\n" +
                        "# TYPE histogram_override_defaultAndPreConfigured_measurables summary\n" +
                        "histogram_override_defaultAndPreConfigured_measurables{quantile=\"0.1\",} 9.0\n" +
                        "histogram_override_defaultAndPreConfigured_measurables{quantile=\"0.25\",} 24.0\n" +
                        "histogram_override_defaultAndPreConfigured_measurables{quantile=\"0.5\",} 49.0\n" +
                        "histogram_override_defaultAndPreConfigured_measurables{quantile=\"0.9\",} 89.0\n" +
                        "histogram_override_defaultAndPreConfigured_measurables{quantile=\"0.99\",} 98.0\n" +
                        "histogram_override_defaultAndPreConfigured_measurables{quantile=\"0.999\",} 99.0\n" +
                        "histogram_override_defaultAndPreConfigured_measurables_count 100.0\n" +
                        "histogram_override_defaultAndPreConfigured_measurables_sum 4950.0\n";

        // when
        var actual = Listing02.run();

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
