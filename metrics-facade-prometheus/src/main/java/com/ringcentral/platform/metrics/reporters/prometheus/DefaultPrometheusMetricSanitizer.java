package com.ringcentral.platform.metrics.reporters.prometheus;

import io.prometheus.client.Collector;

import java.util.List;
import java.util.regex.Pattern;

import static com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter.NAME_PARTS_DELIMITER;
import static java.lang.Character.isLetter;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class DefaultPrometheusMetricSanitizer implements PrometheusMetricSanitizer {

    public static final boolean DEFAULT_SANITIZE_METRIC_NAME = true;
    public static final boolean DEFAULT_SANITIZE_LABEL_NAME = true;
    public static final String DEFAULT_NON_LETTER_LABEL_NAME_FIRST_CHAR_PREFIX = "l_";

    private static final Pattern LABEL_NAME_FORBIDDEN_CHAR_PATTERN = Pattern.compile("\\W");

    private final boolean sanitizeMetricName;
    private final boolean sanitizeLabelName;
    private final String nonLetterLabelNameFirstCharPrefix;

    public DefaultPrometheusMetricSanitizer() {
        this(
            DEFAULT_SANITIZE_METRIC_NAME,
            DEFAULT_SANITIZE_LABEL_NAME,
            DEFAULT_NON_LETTER_LABEL_NAME_FIRST_CHAR_PREFIX);
    }

    public DefaultPrometheusMetricSanitizer(boolean sanitizeMetricName, boolean sanitizeLabelName) {
        this(sanitizeMetricName, sanitizeLabelName, DEFAULT_NON_LETTER_LABEL_NAME_FIRST_CHAR_PREFIX);
    }

    public DefaultPrometheusMetricSanitizer(
        boolean sanitizeMetricName,
        boolean sanitizeLabelName,
        String nonLetterLabelNameFirstCharPrefix) {

        this.sanitizeMetricName = sanitizeMetricName;
        this.sanitizeLabelName = sanitizeLabelName;
        this.nonLetterLabelNameFirstCharPrefix = requireNonNull(nonLetterLabelNameFirstCharPrefix);
    }

    @Override
    public String sanitizeMetricName(String metricName) {
        return sanitizeMetricName ? Collector.sanitizeMetricName(metricName) : metricName;
    }

    @Override
    public List<String> sanitizeLabelNames(List<String> labelNames) {
        return
            sanitizeLabelName ?
            labelNames.stream().map(this::sanitizeLabelName).collect(toList()) :
            labelNames;
    }

    @Override
    public String sanitizeLabelName(String labelName) {
        if (!sanitizeLabelName) {
            return labelName;
        }

        String result = LABEL_NAME_FORBIDDEN_CHAR_PATTERN.matcher(labelName).replaceAll(NAME_PARTS_DELIMITER);
        return isLetter(result.charAt(0)) ? result : nonLetterLabelNameFirstCharPrefix + result;
    }
}
