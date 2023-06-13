package com.ringcentral.platform.metrics.reporters.prometheus;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DefaultPrometheusMetricSanitizerTest {

    static final String UNSANITIZED = ".a.1.:._.й.|";
    PrometheusMetricSanitizer sanitizer = new DefaultPrometheusMetricSanitizer();

    @Test
    public void sanitizingDisabled() {
        var sanitizer = new DefaultPrometheusMetricSanitizer(false, false);
        assertThat(sanitizer.sanitizeMetricName(UNSANITIZED), is(UNSANITIZED));
        assertThat(sanitizer.sanitizeLabelName(UNSANITIZED), is(UNSANITIZED));
        assertThat(sanitizer.sanitizeLabelNames(List.of(UNSANITIZED)), is(List.of(UNSANITIZED)));
    }

    @Test
    public void sanitizingMetricName() {
        assertThat(sanitizer.sanitizeMetricName(".a.1.:._.й.|"), is("_a_1_:______"));
    }

    @Test
    public void sanitizingLabelName() {
        assertThat(sanitizer.sanitizeLabelName("a.1.:._.й.|"), is("a_1________"));
        assertThat(sanitizer.sanitizeLabelNames(List.of(".a.1.:._.й.|")), is(List.of("l__a_1________")));
    }
}