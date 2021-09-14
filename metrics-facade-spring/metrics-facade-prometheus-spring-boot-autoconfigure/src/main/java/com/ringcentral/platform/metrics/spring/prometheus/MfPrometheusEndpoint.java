package com.ringcentral.platform.metrics.spring.prometheus;

import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.TextOutputFormat;

import static com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter.DEFAULT_FORMAT;

@WebEndpoint(id = "mfprometheus")
public class MfPrometheusEndpoint {

    private final PrometheusMetricsExporter exporter;

    public MfPrometheusEndpoint(PrometheusMetricsExporter exporter) {
        this.exporter = exporter;
    }

    @ReadOperation(producesFrom = TextOutputFormat.class)
    public WebEndpointResponse<String> export(TextOutputFormat format) {
        try {
            PrometheusMetricsExporter.Format exporterFormat = DEFAULT_FORMAT;

            if (format == TextOutputFormat.CONTENT_TYPE_004) {
                exporterFormat = PrometheusMetricsExporter.Format.PROMETHEUS_TEXT_O_O_4;
            } else if (format == TextOutputFormat.CONTENT_TYPE_OPENMETRICS_100) {
                exporterFormat = PrometheusMetricsExporter.Format.OPENMETRICS_TEXT_1_0_0;
            }

            return new WebEndpointResponse<>(exporter.exportMetrics(exporterFormat), format);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to export metrics", e);
        }
    }
}
