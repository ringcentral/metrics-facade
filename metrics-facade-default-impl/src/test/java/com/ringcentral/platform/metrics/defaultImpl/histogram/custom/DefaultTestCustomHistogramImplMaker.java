package com.ringcentral.platform.metrics.defaultImpl.histogram.custom;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.histogram.CustomHistogramImplMaker;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramImpl;
import com.ringcentral.platform.metrics.measurables.Measurable;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class DefaultTestCustomHistogramImplMaker implements CustomHistogramImplMaker<DefaultTestCustomHistogramImplConfig> {

    @Override
    public HistogramImpl makeHistogramImpl(
        DefaultTestCustomHistogramImplConfig config,
        MetricContext instanceContext,
        MetricContext sliceContext,
        MetricContext context,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        MetricRegistry registry) {

        return new DefaultTestCustomHistogramImpl(config.measurableValue());
    }
}
