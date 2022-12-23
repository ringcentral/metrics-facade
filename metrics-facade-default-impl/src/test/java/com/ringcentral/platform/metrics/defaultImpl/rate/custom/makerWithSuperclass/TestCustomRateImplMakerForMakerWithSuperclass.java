package com.ringcentral.platform.metrics.defaultImpl.rate.custom.makerWithSuperclass;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.rate.RateImpl;
import com.ringcentral.platform.metrics.measurables.Measurable;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class TestCustomRateImplMakerForMakerWithSuperclass extends MakerSuperclass {

    @Override
    public RateImpl makeRateImpl(
        TestCustomRateImplConfigForMakerWithSuperclass config,
        MetricContext instanceContext,
        MetricContext sliceContext,
        MetricContext context,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        MetricRegistry registry) {

        return new TestCustomRateImplForMakerWithSuperclass(config.measurableValue());
    }
}
