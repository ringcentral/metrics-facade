package com.ringcentral.platform.metrics.defaultImpl.rate.configs;

import com.ringcentral.platform.metrics.impl.MetricImplConfigBuilder;

public interface RateImplConfigBuilder<C extends RateImplConfig> extends MetricImplConfigBuilder {
    C build();
}
