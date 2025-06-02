package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.resetOnSnapshot;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfig;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal.*;

public class ResetOnSnapshotChunk extends Chunk {

    public ResetOnSnapshotChunk(ScaleHistogramImplConfig config, MeasurementSpec measurementSpec) {
        super(config, measurementSpec, true);
    }

    @Override
    protected void updateTree(ScaleTreeNode node, boolean snapshotInProgress, long snapshotNum) {
        long nodeSubtreeUpdateCount = node.subtreeUpdateCount.get();
        long nodeUpdateEpoch = node.updateEpoch.get();
        long treeUpdateEpoch = tree.updateEpoch;

        while (node != null) {
            if (treeUpdateEpoch == nodeUpdateEpoch || !node.updateEpoch.compareAndSet(nodeUpdateEpoch, treeUpdateEpoch)) {
                node.subtreeUpdateCount.incrementAndGet();
            } else {
                node.subtreeUpdateCount.addAndGet(-nodeSubtreeUpdateCount + 1L);
            }

            if (node.level > upperLazyTreeLevel) {
                node = node.parent;

                if (node != null) {
                    nodeSubtreeUpdateCount = node.subtreeUpdateCount.get();
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
            node.subtreeUpdateCount.get();
    }
}
