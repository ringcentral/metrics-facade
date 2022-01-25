package com.ringcentral.platform.metrics.x.histogram.scale;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfig;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public abstract class AbstractScaleXHistogramImpl extends AbstractXHistogramImpl implements ScaleXHistogramImpl {

    public AbstractScaleXHistogramImpl(
        HdrXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ExtendedImplMaker extendedImplMaker,
        ScheduledExecutorService executor) {

        super(
            config,
            measurables,
            extendedImplMaker,
            executor);
    }
}
