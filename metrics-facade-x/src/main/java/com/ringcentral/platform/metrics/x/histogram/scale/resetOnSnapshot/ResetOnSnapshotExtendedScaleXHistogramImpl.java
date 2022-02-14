package com.ringcentral.platform.metrics.x.histogram.scale.resetOnSnapshot;

import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.x.histogram.XHistogramSnapshot;
import com.ringcentral.platform.metrics.x.histogram.scale.AbstractExtendedScaleXHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig;

import static com.ringcentral.platform.metrics.x.histogram.XHistogramSnapshot.NO_VALUE;

public class ResetOnSnapshotExtendedScaleXHistogramImpl extends AbstractExtendedScaleXHistogramImpl {

    private volatile ResetOnSnapshotChunk activeChunk;
    private volatile ResetOnSnapshotChunk inactiveChunk;

    private long count;
    private long totalSum;

    public ResetOnSnapshotExtendedScaleXHistogramImpl(
        ScaleXHistogramImplConfig config,
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
    public synchronized XHistogramSnapshot snapshot() {
        phaser.readerLock();

        try {
            return takeSnapshot();
        } finally {
            phaser.readerUnlock();
        }
    }

    private XHistogramSnapshot takeSnapshot() {
        flipChunks();
        inactiveChunk.startSnapshot();
        inactiveChunk.calcLazySubtreeUpdateCounts();

        XHistogramSnapshot snapshot = inactiveChunk.snapshot(count, totalSum);
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

