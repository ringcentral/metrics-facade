package com.ringcentral.platform.metrics.reporters.prometheus;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrometheusMetricsExporterFormatTest {

    @Test
    public void shouldBeText004WhenPrometheus() {
        // given, when
        String actual = PrometheusMetricsExporter.Format.PROMETHEUS_TEXT_O_O_4.contentType();

        // then
        assertEquals("text/plain; version=0.0.4; charset=utf-8", actual);
    }


    @Test
    public void shouldBeApplicationWhenOpenMetrics() {
        // given, when
        String actual = PrometheusMetricsExporter.Format.OPENMETRICS_TEXT_1_0_0.contentType();

        // then
        assertEquals("application/openmetrics-text; version=1.0.0; charset=utf-8", actual);
    }

}