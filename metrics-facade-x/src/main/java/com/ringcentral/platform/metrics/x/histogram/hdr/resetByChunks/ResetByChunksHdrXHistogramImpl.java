package com.ringcentral.platform.metrics.x.histogram.hdr.resetByChunks;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.utils.*;
import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.configs.BucketsMeasurementType;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfig;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

/**
 * That's just a slightly refactored version of
 * https://github.com/vladimir-bukhtoyarov/rolling-metrics/blob/3.0/rolling-metrics-core/src/main/java/com/github/rollingmetrics/histogram/hdr/impl/ResetByChunksRollingHdrHistogramImpl.java
 * (Copyright 2017 Vladimir Bukhtoyarov Licensed under the Apache License, Version 2.0)
 * We thank Vladimir Bukhtoyarov for his great library.
 */
public class ResetByChunksHdrXHistogramImpl extends AbstractXHistogramImpl {

    public ResetByChunksHdrXHistogramImpl(
        HdrXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        this(
            config,
            measurables,
            executor,
            SystemTimeMsProvider.INSTANCE);
    }

    public ResetByChunksHdrXHistogramImpl(
        HdrXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        TimeMsProvider timeMsProvider) {

        super(
            config,
            measurables,
            new ExtendedImplInfo(false, BucketsMeasurementType.RESETTABLE),
            measurementSpec -> new ResetByChunksExtendedHdrXHistogramImpl(
                config,
                measurementSpec,
                executor,
                timeMsProvider),
            executor);
    }
}
