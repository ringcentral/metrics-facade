package com.ringcentral.platform.metrics.x.histogram.hdr.neverReset;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.hdr.AbstractExtendedHdrXHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfig;
import org.HdrHistogram.*;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.x.histogram.hdr.HdrHistogramUtils.*;

/**
 * That's just a slightly refactored version of
 * https://github.com/vladimir-bukhtoyarov/rolling-metrics/blob/3.0/rolling-metrics-core/src/main/java/com/github/rollingmetrics/histogram/hdr/impl/UniformRollingHdrHistogramImpl.java
 * (Copyright 2017 Vladimir Bukhtoyarov Licensed under the Apache License, Version 2.0)
 * We thank Vladimir Bukhtoyarov for his great library.
 */
public class NeverResetHdrXHistogramImpl extends AbstractXHistogramImpl {

    public NeverResetHdrXHistogramImpl(
        HdrXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        super(
            config,
            measurables,
            new ExtendedImplInfo(false),
            measurementSpec -> new ExtendedImpl(config, measurementSpec, executor),
            executor);
    }

    protected static class ExtendedImpl extends AbstractExtendedHdrXHistogramImpl {

        private final Recorder recorder;
        private Histogram intervalHistogram;
        private final Histogram totalHistogram;

        protected ExtendedImpl(
            HdrXHistogramImplConfig config,
            MeasurementSpec measurementSpec,
            ScheduledExecutorService executor) {

            super(
                config,
                measurementSpec,
                executor);

            this.recorder = makeRecorder(config);
            this.intervalHistogram = recorder.getIntervalHistogram();
            this.totalHistogram = makeNonConcurrentCopy(this.intervalHistogram);
        }

        @Override
        protected void updateWithExpectedInterval(long value, long expectedInterval) {
            recorder.recordValueWithExpectedInterval(value, expectedInterval);
        }

        @Override
        protected Histogram hdrHistogramForSnapshot() {
            intervalHistogram = recorder.getIntervalHistogram(intervalHistogram);
            addSecondToFirst(totalHistogram, intervalHistogram);
            return totalHistogram;
        }
    }
}
