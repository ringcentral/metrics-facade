package com.ringcentral.platform.metrics.defaultImpl.histogram.configs;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.impl.MetricImplConfigBuilder;

@MetricContextTypeKey
public interface HistogramImplConfigBuilder<C extends HistogramImplConfig> extends MetricImplConfigBuilder, MetricContextTypeKeySubtype {
    C build();
}
