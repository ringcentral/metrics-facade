package com.ringcentral.platform.metrics.x.histogram;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.x.histogram.configs.XHistogramImplConfig;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public interface CustomXHistogramImplMaker<C extends XHistogramImplConfig> {
    XHistogramImpl makeXHistogramImpl(
        C config,
        MetricContext instanceContext,
        MetricContext sliceContext,
        MetricContext context,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        MetricRegistry registry);
}
