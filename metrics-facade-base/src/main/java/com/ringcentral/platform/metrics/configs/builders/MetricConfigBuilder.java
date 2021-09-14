package com.ringcentral.platform.metrics.configs.builders;

import com.ringcentral.platform.metrics.configs.MetricConfig;

public interface MetricConfigBuilder<C extends MetricConfig> {
    void rebase(MetricConfigBuilder<?> base);
    void modify(MetricConfigBuilder<?> mod);
    C build();
}
