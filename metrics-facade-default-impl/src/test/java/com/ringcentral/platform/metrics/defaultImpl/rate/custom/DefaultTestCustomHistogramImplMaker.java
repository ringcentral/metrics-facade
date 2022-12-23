package com.ringcentral.platform.metrics.defaultImpl.rate.custom;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.rate.CustomRateImplMaker;
import com.ringcentral.platform.metrics.defaultImpl.rate.RateImpl;
import com.ringcentral.platform.metrics.measurables.Measurable;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class DefaultTestCustomHistogramImplMaker implements CustomRateImplMaker<DefaultTestCustomRateImplConfig> {

    @Override
    public RateImpl makeRateImpl(
        DefaultTestCustomRateImplConfig config,
        MetricContext instanceContext,
        MetricContext sliceContext,
        MetricContext context,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        MetricRegistry registry) {

        return new DefaultTestCustomRateImpl(config.measurableValue());
    }
}
