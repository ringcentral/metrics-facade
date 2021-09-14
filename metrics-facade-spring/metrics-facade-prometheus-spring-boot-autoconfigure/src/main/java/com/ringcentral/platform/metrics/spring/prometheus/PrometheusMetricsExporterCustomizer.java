package com.ringcentral.platform.metrics.spring.prometheus;

import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

public interface PrometheusMetricsExporterCustomizer {
    void customizePrometheusMetricsExporter(PrometheusMetricsExporter exporter);
}
