package com.ringcentral.platform.metrics.x.histogram.hdr;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfig;
import org.HdrHistogram.*;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

/**
 * That's just a slightly refactored version of
 * https://github.com/vladimir-bukhtoyarov/rolling-metrics/blob/3.0/rolling-metrics-core/src/main/java/com/github/rollingmetrics/histogram/hdr/impl/ResetOnSnapshotRollingHdrHistogramImpl.java
 * (Copyright 2017 Vladimir Bukhtoyarov Licensed under the Apache License, Version 2.0)
 * We thank Vladimir Bukhtoyarov for his great library.
 */
public class ResetOnSnapshotHdrXHistogramImpl extends AbstractHdrXHistogramImpl {

    private final Recorder recorder;
    private Histogram intervalHistogram;

    public ResetOnSnapshotHdrXHistogramImpl(
        HdrXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        super(config, measurables, executor);

        this.recorder = makeRecorder(config);
        this.intervalHistogram = recorder.getIntervalHistogram();
    }

    @Override
    protected void updateWithExpectedInterval(long value, long expectedInterval) {
        recorder.recordValueWithExpectedInterval(value, expectedInterval);
    }

    @Override
    protected Histogram hdrHistogramForSnapshot() {
        return intervalHistogram = recorder.getIntervalHistogram(intervalHistogram);
    }
}
