package com.ringcentral.platform.metrics.x.histogram.scale.resetOnSnapshot;

import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.x.histogram.XHistogramSnapshot;
import com.ringcentral.platform.metrics.x.histogram.scale.AbstractExtendedScaleXHistogramImpl;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig;

public class ResetOnSnapshotExtendedScaleXHistogramImpl extends AbstractExtendedScaleXHistogramImpl {

    private volatile ResetOnSnapshotChunk activeChunk;
    private volatile ResetOnSnapshotChunk inactiveChunk;

    public ResetOnSnapshotExtendedScaleXHistogramImpl(
        ScaleXHistogramImplConfig config,
        MeasurementSpec measurementSpec) {

        super(config, measurementSpec);

        this.activeChunk = new ResetOnSnapshotChunk(config, measurementSpec);
        this.inactiveChunk = new ResetOnSnapshotChunk(config, measurementSpec);
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
        inactiveChunk.calcLazySubtreeUpdateCounts();
        XHistogramSnapshot snapshot = inactiveChunk.snapshot();
        inactiveChunk.resetTotalSum();
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

