package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.resetOnSnapshot;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.BucketsMeasurementType;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfig;
import com.ringcentral.platform.metrics.measurables.Measurable;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class ResetOnSnapshotScaleHistogramImpl extends AbstractHistogramImpl {

    public ResetOnSnapshotScaleHistogramImpl(
        ScaleHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        super(
            config,
            measurables,
            new ExtendedImplInfo(true, BucketsMeasurementType.RESETTABLE),
            measurementSpec -> new ResetOnSnapshotExtendedScaleHistogramImpl(config, measurementSpec),
            executor);
    }
}
