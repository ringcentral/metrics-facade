package com.ringcentral.platform.metrics.defaultImpl.histogram.custom.makerWithSuperclass;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.measurables.Measurable;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class TestCustomHistogramImplMakerForMakerWithSuperclass extends MakerSuperclass {

    @Override
    public HistogramImpl makeHistogramImpl(
        TestCustomHistogramImplConfigForMakerWithSuperclass config,
        MetricContext instanceContext,
        MetricContext sliceContext,
        MetricContext context,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        MetricRegistry registry) {

        return new TestCustomHistogramImplForMakerWithSuperclass(config.measurableValue());
    }
}
