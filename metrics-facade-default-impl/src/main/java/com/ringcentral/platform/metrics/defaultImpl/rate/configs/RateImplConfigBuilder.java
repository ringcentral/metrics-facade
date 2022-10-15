package com.ringcentral.platform.metrics.defaultImpl.rate.configs;

import com.ringcentral.platform.metrics.MetricContextTypeKey;
import com.ringcentral.platform.metrics.MetricContextTypeKeySubtype;
import com.ringcentral.platform.metrics.impl.MetricImplConfigBuilder;

@MetricContextTypeKey
public interface RateImplConfigBuilder<C extends RateImplConfig> extends MetricImplConfigBuilder, MetricContextTypeKeySubtype {
    C build();
}
