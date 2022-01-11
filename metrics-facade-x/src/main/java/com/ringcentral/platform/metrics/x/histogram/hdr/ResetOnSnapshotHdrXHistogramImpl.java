package com.ringcentral.platform.metrics.x.histogram.hdr;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfig;
import org.HdrHistogram.*;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

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
