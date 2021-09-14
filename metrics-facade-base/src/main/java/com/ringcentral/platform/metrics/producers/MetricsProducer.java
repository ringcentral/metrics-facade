package com.ringcentral.platform.metrics.producers;

import com.ringcentral.platform.metrics.MetricRegistry;

public interface MetricsProducer {
    void produceMetrics(MetricRegistry registry);
}
