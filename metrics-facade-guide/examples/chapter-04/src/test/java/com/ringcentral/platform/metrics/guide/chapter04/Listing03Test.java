package com.ringcentral.platform.metrics.guide.chapter04;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Listing03Test {

    @Test
    public void test() {
        // given
        var expected =
                "# HELP histogram_override_defaultAndPreConfiguredAndCustom_measurables Generated from metric instances with name histogram.override.defaultAndPreConfiguredAndCustom.measurables\n" +
                        "# TYPE histogram_override_defaultAndPreConfiguredAndCustom_measurables summary\n" +
                        "histogram_override_defaultAndPreConfiguredAndCustom_measurables{quantile=\"0.99\",} 98.0\n" +
                        "histogram_override_defaultAndPreConfiguredAndCustom_measurables_count 100.0\n" +
                        "histogram_override_defaultAndPreConfiguredAndCustom_measurables_sum 4950.0\n";

        // when
        var actual = Listing03.run();

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
