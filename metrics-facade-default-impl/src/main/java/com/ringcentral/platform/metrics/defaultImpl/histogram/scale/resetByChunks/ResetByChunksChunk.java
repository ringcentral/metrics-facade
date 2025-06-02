package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.resetByChunks;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfig;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal.*;

public class ResetByChunksChunk extends Chunk {

    public ResetByChunksChunk(ScaleHistogramImplConfig config, MeasurementSpec measurementSpec) {
        super(config, measurementSpec, true);
    }

    @Override
    protected void updateTree(ScaleTreeNode node, boolean snapshotInProgress, long snapshotNum) {
        long nodeSubtreeUpdateCount = node.subtreeUpdateCount.get();
        long nodeUpdateEpoch = node.updateEpoch.get();
        long treeUpdateEpoch = tree.updateEpoch;

        while (node != null) {
            if (snapshotInProgress && node.snapshotNum.get() < snapshotNum) {
                if (treeUpdateEpoch == nodeUpdateEpoch) {
                    node.snapshotSubtreeUpdateCount = nodeSubtreeUpdateCount;
                    node.snapshotNum.set(snapshotNum);
                    node.subtreeUpdateCount.incrementAndGet();
                } else {
                    node.snapshotSubtreeUpdateCount = 0L;
                    node.snapshotNum.set(snapshotNum);

                    if (node.updateEpoch.compareAndSet(nodeUpdateEpoch, treeUpdateEpoch)) {
                        node.subtreeUpdateCount.addAndGet(-nodeSubtreeUpdateCount + 1L);
                    } else {
                        node.subtreeUpdateCount.incrementAndGet();
                    }
                }
            } else {
                if (treeUpdateEpoch == nodeUpdateEpoch || !node.updateEpoch.compareAndSet(nodeUpdateEpoch, treeUpdateEpoch)) {
                    node.subtreeUpdateCount.incrementAndGet();
                } else {
                    node.subtreeUpdateCount.addAndGet(-nodeSubtreeUpdateCount + 1L);
                }
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

        if (useLazy && node.level < upperLazyTreeLevel) {
            return lazySubtreeUpdateCountFor(node);
        }

        if (node.updateEpoch.get() < tree.updateEpoch) {
            return 0L;
        }

        long nodeSubtreeUpdateCount = node.subtreeUpdateCount.get();

        return
            node.snapshotNum.get() < tree.snapshotNum ?
            nodeSubtreeUpdateCount  :
            node.snapshotSubtreeUpdateCount;
    }
}
