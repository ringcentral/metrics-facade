package com.ringcentral.platform.metrics.samples.histogram;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.defaultImpl.histogram.*;
import com.ringcentral.platform.metrics.measurables.Measurable;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class LastValueHistogramImplMaker implements CustomHistogramImplMaker<LastValueHistogramImplConfig> {

    @Override
    public HistogramImpl makeHistogramImpl(
        LastValueHistogramImplConfig config,
        MetricContext instanceContext,
        MetricContext sliceContext,
        MetricContext context,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        MetricRegistry registry) {

        return new LastValueHistogramImpl();
    }
}
