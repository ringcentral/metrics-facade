package com.ringcentral.platform.metrics.guide.chapter04;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Listing04Test {

    @Test
    public void test() {
        // given
        var expected =
                "# HELP histogram_metric_example Generated from metric instances with name histogram.metric.example\n" +
                        "# TYPE histogram_metric_example summary\n" +
                        "histogram_metric_example{prefix=\"prefix-value\",quantile=\"0.5\",} 4.0\n" +
                        "histogram_metric_example{prefix=\"prefix-value\",quantile=\"0.9\",} 8.0\n" +
                        "histogram_metric_example{prefix=\"prefix-value\",quantile=\"0.99\",} 9.0\n" +
                        "histogram_metric_example_count{prefix=\"prefix-value\",} 10.0\n" +
                        "histogram_metric_example_sum{prefix=\"prefix-value\",} 45.0\n";

        // when
        var actual = Listing04.run();

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
