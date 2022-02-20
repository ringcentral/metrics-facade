package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.neverReset;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.BucketsMeasurementType;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfig;
import com.ringcentral.platform.metrics.measurables.Measurable;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class NeverResetScaleHistogramImpl extends AbstractHistogramImpl {

    public NeverResetScaleHistogramImpl(
        ScaleHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        super(
            config,
            measurables,
            new ExtendedImplInfo(true, BucketsMeasurementType.NEVER_RESET),
            measurementSpec -> new NeverResetExtendedScaleHistogramImpl(config, measurementSpec),
            executor);
    }
}
