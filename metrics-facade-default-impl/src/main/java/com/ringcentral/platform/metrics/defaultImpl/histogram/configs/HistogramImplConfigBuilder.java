package com.ringcentral.platform.metrics.defaultImpl.histogram.configs;

import com.ringcentral.platform.metrics.*;

@MetricContextTypeKey
public interface HistogramImplConfigBuilder<C extends HistogramImplConfig> extends MetricContextTypeKeySubtype {
    C build();
}
