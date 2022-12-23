package com.ringcentral.platform.metrics.samples.rate;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.rate.CustomRateImplMaker;
import com.ringcentral.platform.metrics.defaultImpl.rate.RateImpl;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.utils.SystemTimeNanosProvider;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class CountScalingRateImplMaker implements CustomRateImplMaker<CountScalingRateImplConfig> {

    @Override
    public RateImpl makeRateImpl(
        CountScalingRateImplConfig config,
        MetricContext instanceContext,
        MetricContext sliceContext,
        MetricContext context,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        MetricRegistry registry) {

        return new CountScalingRateImpl(
            measurables,
            SystemTimeNanosProvider.INSTANCE,
            config.factor());
    }
}
