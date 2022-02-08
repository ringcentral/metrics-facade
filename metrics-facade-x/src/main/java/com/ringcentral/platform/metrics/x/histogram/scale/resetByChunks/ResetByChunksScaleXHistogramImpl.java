package com.ringcentral.platform.metrics.x.histogram.scale.resetByChunks;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.utils.SystemTimeMsProvider;
import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class ResetByChunksScaleXHistogramImpl extends AbstractXHistogramImpl {

    public ResetByChunksScaleXHistogramImpl(
        ScaleXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        super(
            config,
            measurables,
            measurementSpec -> new ResetByChunksExtendedScaleXHistogramImpl(
                config,
                measurementSpec,
                executor,
                SystemTimeMsProvider.INSTANCE),
            executor);
    }
}
