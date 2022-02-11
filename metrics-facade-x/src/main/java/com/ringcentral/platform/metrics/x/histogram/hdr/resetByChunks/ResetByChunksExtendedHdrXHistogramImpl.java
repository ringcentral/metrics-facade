package com.ringcentral.platform.metrics.x.histogram.hdr.resetByChunks;

import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.hdr.AbstractExtendedHdrXHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfig;
import org.HdrHistogram.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static com.ringcentral.platform.metrics.x.histogram.hdr.HdrHistogramUtils.*;

/**
 * That's just a slightly refactored version of
 * https://github.com/vladimir-bukhtoyarov/rolling-metrics/blob/3.0/rolling-metrics-core/src/main/java/com/github/rollingmetrics/histogram/hdr/impl/ResetByChunksRollingHdrHistogramImpl.java
 * (Copyright 2017 Vladimir Bukhtoyarov Licensed under the Apache License, Version 2.0)
 * We thank Vladimir Bukhtoyarov for his great library.
 */
public class ResetByChunksExtendedHdrXHistogramImpl extends AbstractExtendedHdrXHistogramImpl {

    private final long chunkResetPeriodMs;
    private final long creationTimeMs;

    private final Phase leftPhase;
    private final Phase rightPhase;
    private final Phase[] phases;
    private final AtomicReference<Phase> currPhaseRef;

    private final boolean withHistory;
    private final HistoryItem[] history;
    private final int historySize;

    private final Histogram histogramForSnapshot;
    private final TimeMsProvider timeMsProvider;

    protected ResetByChunksExtendedHdrXHistogramImpl(
        HdrXHistogramImplConfig config,
        AbstractXHistogramImpl.MeasurementSpec measurementSpec,
        ScheduledExecutorService executor,
        TimeMsProvider timeMsProvider) {

        super(
            config,
            measurementSpec,
            executor);

        this.chunkResetPeriodMs = config.chunkResetPeriodMs();
        this.creationTimeMs = timeMsProvider.stableTimeMs();

        this.leftPhase = new Phase(config, this.creationTimeMs + this.chunkResetPeriodMs);
        this.rightPhase = new Phase(config, Long.MAX_VALUE);
        this.phases = new Phase[]{ this.leftPhase, this.rightPhase };
        this.currPhaseRef = new AtomicReference<>(this.leftPhase);

        this.historySize = config.chunkCount();
        this.withHistory = this.historySize > 0;

        if (withHistory) {
            this.history = new HistoryItem[this.historySize];

            for (int i = 0; i < this.historySize; ++i) {
                this.history[i] = new HistoryItem(makeNonConcurrentCopy(this.leftPhase.intervalHistogram), Long.MIN_VALUE);
            }
        } else {
            this.history = null;
        }

        this.histogramForSnapshot = makeNonConcurrentCopy(this.leftPhase.intervalHistogram);
        this.timeMsProvider = timeMsProvider;
    }

    @Override
    protected void updateWithExpectedInterval(long value, long expectedInterval) {
        long nowMs = timeMsProvider.stableTimeMs();
        Phase currPhase = currPhaseRef.get();

        if (nowMs < currPhase.proposedInvalidationTimeMs) {
            currPhase.recorder.recordValueWithExpectedInterval(value, expectedInterval);
            return;
        }

        Phase nextPhase = currPhase == leftPhase ? rightPhase : leftPhase;
        nextPhase.recorder.recordValueWithExpectedInterval(value, expectedInterval);

        if (currPhaseRef.compareAndSet(currPhase, nextPhase)) {
            executor.execute(() -> rotate(nowMs, currPhase, nextPhase));
        }
    }

    private synchronized void rotate(long nowMs, Phase currPhase, Phase nextPhase) {
        try {
            currPhase.intervalHistogram = currPhase.recorder.getIntervalHistogram(currPhase.intervalHistogram);
            addSecondToFirst(currPhase.totalHistogram, currPhase.intervalHistogram);

            if (withHistory) {
                long currPhaseNum = (currPhase.proposedInvalidationTimeMs - creationTimeMs) / chunkResetPeriodMs;
                int historyIndex = (int)(currPhaseNum - 1) % historySize;
                HistoryItem historyItem = history[historyIndex];
                reset(historyItem.histogram);
                addSecondToFirst(historyItem.histogram, currPhase.totalHistogram);
                historyItem.proposedInvalidationTimeMs = currPhase.proposedInvalidationTimeMs + historySize * chunkResetPeriodMs;
            }

            reset(currPhase.totalHistogram);
        } finally {
            long msSinceCreation = nowMs - creationTimeMs;
            long chunksSinceCreation = msSinceCreation / chunkResetPeriodMs;
            currPhase.proposedInvalidationTimeMs = Long.MAX_VALUE;
            nextPhase.proposedInvalidationTimeMs = creationTimeMs + (chunksSinceCreation + 1) * chunkResetPeriodMs;
        }
    }

    @Override
    protected Histogram hdrHistogramForSnapshot() {
        long nowMs = timeMsProvider.stableTimeMs();
        reset(histogramForSnapshot);

        for (Phase phase : phases) {
            if (phase.isNeedToBeReportedToSnapshot(nowMs)) {
                phase.intervalHistogram = phase.recorder.getIntervalHistogram(phase.intervalHistogram);
                addSecondToFirst(phase.totalHistogram, phase.intervalHistogram);
                addSecondToFirst(histogramForSnapshot, phase.totalHistogram);
            }
        }

        if (withHistory) {
            for (HistoryItem item : history) {
                if (item.proposedInvalidationTimeMs > nowMs) {
                    addSecondToFirst(histogramForSnapshot, item.histogram);
                }
            }
        }

        return histogramForSnapshot;
    }

    private static final class Phase {

        final long chunkResetPeriodMs;
        final boolean withHistory;
        final int historySize;

        final Recorder recorder;
        Histogram intervalHistogram;
        final Histogram totalHistogram;

        volatile long proposedInvalidationTimeMs;

        Phase(HdrXHistogramImplConfig config, long proposedInvalidationTimeMs) {
            this.chunkResetPeriodMs = config.chunkResetPeriodMs();
            this.historySize = config.chunkCount();
            this.withHistory = this.historySize > 0;

            this.recorder = makeRecorder(config);
            this.intervalHistogram = recorder.getIntervalHistogram();
            this.totalHistogram = intervalHistogram.copy();

            this.proposedInvalidationTimeMs = proposedInvalidationTimeMs;
        }

        boolean isNeedToBeReportedToSnapshot(long nowMs) {
            long localProposedInvalidationTimeMs = proposedInvalidationTimeMs;

            if (localProposedInvalidationTimeMs > nowMs) {
                return true;
            }

            if (!withHistory) {
                return false;
            }

            return (localProposedInvalidationTimeMs + historySize * chunkResetPeriodMs) > nowMs;
        }
    }

    private static final class HistoryItem {

        final Histogram histogram;
        volatile long proposedInvalidationTimeMs;

        HistoryItem(Histogram histogram, long proposedInvalidationTimeMs) {
            this.histogram = histogram;
            this.proposedInvalidationTimeMs = proposedInvalidationTimeMs;
        }
    }
}
