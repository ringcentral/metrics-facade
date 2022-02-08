package com.ringcentral.platform.metrics.x.histogram.scale.neverReset;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.x.histogram.*;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class NeverResetScaleXHistogramImpl extends AbstractXHistogramImpl {

    public NeverResetScaleXHistogramImpl(
        ScaleXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        super(
            config,
            measurables,
            measurementSpec -> new NeverResetExtendedScaleXHistogramImpl(config, measurementSpec),
            executor);
    }
}
