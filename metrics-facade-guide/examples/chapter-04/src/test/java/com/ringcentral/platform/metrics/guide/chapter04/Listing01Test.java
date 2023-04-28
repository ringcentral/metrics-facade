package com.ringcentral.platform.metrics.guide.chapter04;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Listing01Test {

    @Test
    public void test() {
        // given
        var expected =
                "# HELP histogram_use_preConfigure_measurables Generated from metric instances with name histogram.use.preConfigure.measurables\n" +
                        "# TYPE histogram_use_preConfigure_measurables summary\n" +
                        "histogram_use_preConfigure_measurables{quantile=\"0.5\",} 4.0\n" +
                        "histogram_use_preConfigure_measurables{quantile=\"0.9\",} 8.0\n" +
                        "histogram_use_preConfigure_measurables{quantile=\"0.99\",} 9.0\n" +
                        "histogram_use_preConfigure_measurables_count 10.0\n" +
                        "histogram_use_preConfigure_measurables_sum 45.0\n";

        // when
        var actual = Listing01.run();

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
