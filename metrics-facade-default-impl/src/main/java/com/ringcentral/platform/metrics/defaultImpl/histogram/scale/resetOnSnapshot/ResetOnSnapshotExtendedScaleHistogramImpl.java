package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.resetOnSnapshot;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.AbstractExtendedScaleHistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfig;

import static com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot.NO_VALUE;

public class ResetOnSnapshotExtendedScaleHistogramImpl extends AbstractExtendedScaleHistogramImpl {

    private volatile ResetOnSnapshotChunk activeChunk;
    private volatile ResetOnSnapshotChunk inactiveChunk;

    private long count;
    private long totalSum;

    public ResetOnSnapshotExtendedScaleHistogramImpl(
        ScaleHistogramImplConfig config,
        MeasurementSpec measurementSpec) {

        super(config, measurementSpec);

        this.activeChunk = new ResetOnSnapshotChunk(config, measurementSpec);
        this.inactiveChunk = new ResetOnSnapshotChunk(config, measurementSpec);

        this.count = measurementSpec.isWithCount() ? 0L : NO_VALUE;
        this.totalSum = measurementSpec.isWithTotalSum() ? 0L : NO_VALUE;
    }

    @Override
    public void update(long value) {
        long criticalValueAtEnter = phaser.writerCriticalSectionEnter();

        try {
            activeChunk.update(value);
        } finally {
            phaser.writerCriticalSectionExit(criticalValueAtEnter);
        }
    }

    @Override
    public synchronized HistogramSnapshot snapshot() {
        phaser.readerLock();

        try {
            return takeSnapshot();
        } finally {
            phaser.readerUnlock();
        }
    }

    private HistogramSnapshot takeSnapshot() {
        flipChunks();
        inactiveChunk.startSnapshot();
        inactiveChunk.calcLazySubtreeUpdateCounts();

        HistogramSnapshot snapshot = inactiveChunk.snapshot(count, totalSum);
        count = snapshot.count();
        totalSum = snapshot.totalSum();

        inactiveChunk.endSnapshot();
        inactiveChunk.resetSum();
        return snapshot;
    }

    private void flipChunks() {
        inactiveChunk.startUpdateEpoch();
        ResetOnSnapshotChunk tempChunk = inactiveChunk;
        inactiveChunk = activeChunk;
        activeChunk = tempChunk;
        flipPhase();
    }
}

