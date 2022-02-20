package com.ringcentral.platform.metrics.defaultImpl.rate;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.defaultImpl.rate.configs.RateImplConfig;
import com.ringcentral.platform.metrics.measurables.Measurable;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public interface CustomRateImplMaker<C extends RateImplConfig> {
    RateImpl makeRateImpl(
        C config,
        MetricContext instanceContext,
        MetricContext sliceContext,
        MetricContext context,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        MetricRegistry registry);
}
