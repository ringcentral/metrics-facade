package com.ringcentral.platform.metrics.reporters;

public interface MetricsJsonExporter extends MetricsExporter<MetricsJson> {
    MetricsJson exportMetrics();
}
