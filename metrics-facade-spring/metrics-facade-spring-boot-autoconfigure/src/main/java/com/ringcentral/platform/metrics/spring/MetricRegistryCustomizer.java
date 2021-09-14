package com.ringcentral.platform.metrics.spring;

import com.ringcentral.platform.metrics.MetricRegistry;

public interface MetricRegistryCustomizer {
    void customizeMetricRegistry(MetricRegistry registry);
}
