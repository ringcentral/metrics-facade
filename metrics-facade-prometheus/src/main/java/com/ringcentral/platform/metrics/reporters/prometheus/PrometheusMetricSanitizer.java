package com.ringcentral.platform.metrics.reporters.prometheus;

import java.util.List;

/**
 * Provides methods for sanitizing Prometheus metric names and label names based on the following specification:
 * <a href="https://prometheus.io/docs/concepts/data_model/#metric-names-and-labels">Prometheus Data Model</a>
 */
public interface PrometheusMetricSanitizer {
    /**
     * Sanitizes a Prometheus metric name according to the data model specification.
     *
     * @param metricName the metric name to sanitize
     * @return the sanitized metric name
     */
    String sanitizeMetricName(String metricName);

    /**
     * Sanitizes a list of Prometheus label names according to the data model specification.
     *
     * @param labelNames the list of label names to sanitize
     * @return the sanitized list of label names
     */
    List<String> sanitizeLabelNames(List<String> labelNames);

    /**
     * Sanitizes a single Prometheus label name according to the data model specification.
     *
     * @param labelName the label name to sanitize
     * @return the sanitized label name
     */
    String sanitizeLabelName(String labelName);
}
