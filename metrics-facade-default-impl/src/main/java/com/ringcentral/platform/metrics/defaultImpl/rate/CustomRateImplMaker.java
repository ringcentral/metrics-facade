package com.ringcentral.platform.metrics.defaultImpl.rate;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.CustomMetricImplMaker;
import com.ringcentral.platform.metrics.defaultImpl.rate.configs.RateImplConfig;
import com.ringcentral.platform.metrics.measurables.Measurable;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public interface CustomRateImplMaker<C extends RateImplConfig> extends CustomMetricImplMaker<C> {
    RateImpl makeRateImpl(
        C config,
        MetricContext instanceContext,
        MetricContext sliceContext,
        MetricContext context,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        MetricRegistry registry);
}
