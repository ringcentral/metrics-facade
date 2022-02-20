package com.ringcentral.platform.metrics.defaultImpl.histogram;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.HistogramImplConfig;
import com.ringcentral.platform.metrics.measurables.Measurable;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public interface CustomHistogramImplMaker<C extends HistogramImplConfig> {
    HistogramImpl makeHistogramImpl(
        C config,
        MetricContext instanceContext,
        MetricContext sliceContext,
        MetricContext context,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        MetricRegistry registry);
}
