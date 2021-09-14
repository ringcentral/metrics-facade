package com.ringcentral.platform.metrics.reporters;

public interface MetricsExporter<R> extends MetricsReporter {
    R exportMetrics();
}
