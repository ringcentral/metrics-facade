package com.ringcentral.platform.metrics.spring.jmx;

import com.ringcentral.platform.metrics.reporters.jmx.JmxMetricsReporter;

public interface JmxMetricsReporterCustomizer {
    void customizeJmxMetricsReporter(JmxMetricsReporter reporter);
}
