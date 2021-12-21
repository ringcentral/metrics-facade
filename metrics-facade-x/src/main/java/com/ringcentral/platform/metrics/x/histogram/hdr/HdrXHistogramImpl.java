package com.ringcentral.platform.metrics.x.histogram.hdr;

import com.github.rollingmetrics.histogram.hdr.*;
import com.github.rollingmetrics.histogram.hdr.impl.*;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.utils.*;
import com.ringcentral.platform.metrics.x.histogram.*;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfig;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static com.github.rollingmetrics.util.Ticker.defaultTicker;
import static com.ringcentral.platform.metrics.x.histogram.hdr.HdrXHistogramImpl.Type.*;

public class HdrXHistogramImpl extends AbstractXHistogramImpl {

    public enum Type {
        NEVER_RESET,
        RESET_ON_SNAPSHOT,
        RESET_BY_CHUNKS
    }

    private final RollingHdrHistogram parent;

    public HdrXHistogramImpl(
        HdrXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor) {

        this(
            config,
            measurables,
            executor,
            SystemTimeNanosProvider.INSTANCE);
    }

    public HdrXHistogramImpl(
        HdrXHistogramImplConfig config,
        Set<? extends Measurable> measurables,
        ScheduledExecutorService executor,
        TimeNanosProvider timeNanosProvider) {

        super(measurables);

        Type type = config.type();
        RecorderSettings recorderSettings = config.recorderSettings();

        double[] percentiles = measurables.stream()
            .filter(m -> m instanceof Histogram.Percentile)
            .mapToDouble(m -> ((Histogram.Percentile)m).quantile())
            .sorted()
            .toArray();

        if (percentiles.length > 0) {
            recorderSettings = recorderSettings.withPredefinedPercentiles(percentiles);
        }

        RollingHdrHistogram h;

        if (type == NEVER_RESET) {
            h = new UniformRollingHdrHistogramImpl(recorderSettings);
        } else if (type == RESET_ON_SNAPSHOT) {
            h = new ResetOnSnapshotRollingHdrHistogramImpl(recorderSettings);
        } else if (type == RESET_BY_CHUNKS) {
            h = new ResetByChunksRollingHdrHistogramImpl(
                recorderSettings,
                config.chunkCount(),
                config.chunkResetPeriodMs(),
                defaultTicker(),
                executor);
        } else {
            throw new UnsupportedOperationException("Unsupported HDR histogram type: " + type);
        }

        if (config.hasSnapshotTtl()) {
            h = new SnapshotCachingRollingHdrHistogram(h, config.snapshotTtl(), defaultTicker());
        }

        this.parent = h;
    }

    @Override
    protected void updateImpl(long value) {
        parent.update(value);
    }

    @Override
    public XHistogramImplSnapshot snapshot() {
        return new SnapshotImpl(parent.getSnapshot());
    }

    private static class SnapshotImpl implements XHistogramImplSnapshot {

        private final RollingSnapshot parent;

        public SnapshotImpl(RollingSnapshot parent) {
            this.parent = parent;
        }

        @Override
        public long size() {
            return parent.getSamplesCount();
        }

        @Override
        public long min() {
            return parent.getMin();
        }

        @Override
        public long max() {
            return parent.getMax();
        }

        @Override
        public double mean() {
            return parent.getMean();
        }

        @Override
        public double standardDeviation() {
            return parent.getStdDev();
        }

        @Override
        public double percentile(double quantile) {
            return parent.getValue(quantile);
        }
    }
}
