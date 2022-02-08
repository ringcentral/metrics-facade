package com.ringcentral.platform.metrics.x.histogram.scale.resetOnSnapshot;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class ResetOnSnapshotScaleXHistogramImpl extends AbstractXHistogramImpl {

    public ResetOnSnapshotScaleXHistogramImpl(
        ScaleXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        super(
            config,
            measurables,
            measurementSpec -> new ResetOnSnapshotExtendedScaleXHistogramImpl(config, measurementSpec),
            executor);
    }
}
