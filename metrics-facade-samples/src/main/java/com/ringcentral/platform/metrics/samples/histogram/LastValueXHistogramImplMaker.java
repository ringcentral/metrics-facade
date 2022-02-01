package com.ringcentral.platform.metrics.samples.histogram;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.x.histogram.*;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class LastValueXHistogramImplMaker implements CustomXHistogramImplMaker<LastValueXHistogramImplConfig> {

    @Override
    public XHistogramImpl makeXHistogramImpl(
        LastValueXHistogramImplConfig config,
        MetricContext instanceContext,
        MetricContext sliceContext,
        MetricContext context,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        MetricRegistry registry) {

        return new LastValueXHistogramImpl();
    }
}
