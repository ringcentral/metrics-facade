package com.ringcentral.platform.metrics.x.rate;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.x.rate.configs.XRateImplConfig;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public interface CustomXRateImplMaker<C extends XRateImplConfig> {
    XRateImpl makeXRateImpl(
        C config,
        MetricContext instanceContext,
        MetricContext sliceContext,
        MetricContext context,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        MetricRegistry registry);
}
