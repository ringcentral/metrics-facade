package com.ringcentral.platform.metrics.x.histogram.scale.resetByChunks;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.utils.*;
import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class ResetByChunksScaleXHistogramImpl extends AbstractXHistogramImpl {

    public ResetByChunksScaleXHistogramImpl(
        ScaleXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        this(
            config,
            measurables,
            executor,
            SystemTimeMsProvider.INSTANCE);
    }

    public ResetByChunksScaleXHistogramImpl(
        ScaleXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        TimeMsProvider timeMsProvider) {

        super(
            config,
            measurables,
            new ExtendedImplInfo(true),
            measurementSpec -> new ResetByChunksExtendedScaleXHistogramImpl(
                config,
                measurementSpec,
                executor,
                timeMsProvider),
            executor);
    }
}
