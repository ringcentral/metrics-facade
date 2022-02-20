package com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.resetOnSnapshot;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.BucketsMeasurementType;
import com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.AbstractExtendedHdrHistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.configs.HdrHistogramImplConfig;
import com.ringcentral.platform.metrics.measurables.Measurable;
import org.HdrHistogram.*;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

/**
 * That's just a slightly refactored version of
 * https://github.com/vladimir-bukhtoyarov/rolling-metrics/blob/3.0/rolling-metrics-core/src/main/java/com/github/rollingmetrics/histogram/hdr/impl/ResetOnSnapshotRollingHdrHistogramImpl.java
 * (Copyright 2017 Vladimir Bukhtoyarov Licensed under the Apache License, Version 2.0)
 * We thank Vladimir Bukhtoyarov for his great library.
 */
public class ResetOnSnapshotHdrHistogramImpl extends AbstractHistogramImpl {

    public ResetOnSnapshotHdrHistogramImpl(
        HdrHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        super(
            config,
            measurables,
            new ExtendedImplInfo(false, BucketsMeasurementType.RESETTABLE),
            measurementSpec -> new ExtendedImpl(config, measurementSpec, executor),
            executor);
    }

    protected static class ExtendedImpl extends AbstractExtendedHdrHistogramImpl {

        private final Recorder recorder;
        private Histogram intervalHistogram;

        protected ExtendedImpl(
            HdrHistogramImplConfig config,
            MeasurementSpec measurementSpec,
            ScheduledExecutorService executor) {

            super(
                config,
                measurementSpec,
                executor);

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
}
