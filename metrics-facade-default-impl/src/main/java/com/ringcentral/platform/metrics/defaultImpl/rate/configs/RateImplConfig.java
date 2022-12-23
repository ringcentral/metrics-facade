package com.ringcentral.platform.metrics.defaultImpl.rate.configs;

import com.ringcentral.platform.metrics.MetricContextTypeKey;
import com.ringcentral.platform.metrics.MetricContextTypeKeySubtype;
import com.ringcentral.platform.metrics.impl.MetricImplConfig;

@MetricContextTypeKey
public interface RateImplConfig extends MetricImplConfig, MetricContextTypeKeySubtype {}
