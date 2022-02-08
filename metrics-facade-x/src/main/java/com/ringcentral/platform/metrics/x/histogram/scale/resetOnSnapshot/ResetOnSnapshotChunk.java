package com.ringcentral.platform.metrics.x.histogram.scale.resetOnSnapshot;

import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig;
import com.ringcentral.platform.metrics.x.histogram.scale.internal.*;

public class ResetOnSnapshotChunk extends Chunk {

    public ResetOnSnapshotChunk(ScaleXHistogramImplConfig config, MeasurementSpec measurementSpec) {
        super(config, measurementSpec, true);
    }

    @Override
    protected void updateTree(ScaleTreeNode node, boolean snapshotInProgress, long snapshotNum) {
        long nodeSubtreeUpdateCount = node.subtreeUpdateCount.sum();
        long nodeUpdateEpoch = node.updateEpoch.get();
        long treeUpdateEpoch = tree.updateEpoch;

        while (node != null) {
            if (treeUpdateEpoch == nodeUpdateEpoch || !node.updateEpoch.compareAndSet(nodeUpdateEpoch, treeUpdateEpoch)) {
                node.subtreeUpdateCount.increment();
            } else {
                node.subtreeUpdateCount.add(-nodeSubtreeUpdateCount + 1L);
            }

            if (node.level > upperLazyTreeLevel) {
                node = node.parent;

                if (node != null) {
                    nodeSubtreeUpdateCount = node.subtreeUpdateCount.sum();
                    nodeUpdateEpoch = node.updateEpoch.get();
                }
            } else {
                break;
            }
        }
    }

    @Override
    public long subtreeUpdateCountFor(ScaleTreeNode node, boolean useLazy) {
        if (node == null) {
            return 0L;
        }

        return
            useLazy && node.level < upperLazyTreeLevel ?
            lazySubtreeUpdateCountFor(node) :
            node.subtreeUpdateCount.sum();
    }
}
