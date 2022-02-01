package com.ringcentral.platform.metrics.x.histogram.configs;

import com.ringcentral.platform.metrics.*;

@MetricContextTypeKey
public interface XHistogramImplConfigBuilder<C extends XHistogramImplConfig> extends MetricContextTypeKeySubtype {
    C build();
}
