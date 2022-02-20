package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.resetByChunks;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.BucketsMeasurementType;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfig;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.utils.*;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

public class ResetByChunksScaleHistogramImpl extends AbstractHistogramImpl {

    public ResetByChunksScaleHistogramImpl(
        ScaleHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        this(
            config,
            measurables,
            executor,
            SystemTimeMsProvider.INSTANCE);
    }

    public ResetByChunksScaleHistogramImpl(
        ScaleHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        TimeMsProvider timeMsProvider) {

        super(
            config,
            measurables,
            new ExtendedImplInfo(true, BucketsMeasurementType.RESETTABLE),
            measurementSpec -> new ResetByChunksExtendedScaleHistogramImpl(
                config,
                measurementSpec,
                executor,
                timeMsProvider),
            executor);
    }
}
