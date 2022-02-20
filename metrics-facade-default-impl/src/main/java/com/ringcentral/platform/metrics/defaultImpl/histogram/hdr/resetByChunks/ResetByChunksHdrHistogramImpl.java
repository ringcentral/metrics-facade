package com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.resetByChunks;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.BucketsMeasurementType;
import com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.configs.HdrHistogramImplConfig;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.utils.*;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

/**
 * That's just a slightly refactored version of
 * https://github.com/vladimir-bukhtoyarov/rolling-metrics/blob/3.0/rolling-metrics-core/src/main/java/com/github/rollingmetrics/histogram/hdr/impl/ResetByChunksRollingHdrHistogramImpl.java
 * (Copyright 2017 Vladimir Bukhtoyarov Licensed under the Apache License, Version 2.0)
 * We thank Vladimir Bukhtoyarov for his great library.
 */
public class ResetByChunksHdrHistogramImpl extends AbstractHistogramImpl {

    public ResetByChunksHdrHistogramImpl(
        HdrHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        this(
            config,
            measurables,
            executor,
            SystemTimeMsProvider.INSTANCE);
    }

    public ResetByChunksHdrHistogramImpl(
        HdrHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        TimeMsProvider timeMsProvider) {

        super(
            config,
            measurables,
            new ExtendedImplInfo(false, BucketsMeasurementType.RESETTABLE),
            measurementSpec -> new ResetByChunksExtendedHdrHistogramImpl(
                config,
                measurementSpec,
                executor,
                timeMsProvider),
            executor);
    }
}
