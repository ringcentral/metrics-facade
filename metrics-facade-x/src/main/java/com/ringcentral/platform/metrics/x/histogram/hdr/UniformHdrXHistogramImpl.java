package com.ringcentral.platform.metrics.x.histogram.hdr;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfig;
import org.HdrHistogram.*;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.x.histogram.hdr.HdrHistogramUtils.*;

public class UniformHdrXHistogramImpl extends AbstractHdrXHistogramImpl {

    private final Recorder recorder;
    private Histogram intervalHistogram;
    private final Histogram totalHistogram;

    public UniformHdrXHistogramImpl(
        HdrXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        super(config, measurables, executor);

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
