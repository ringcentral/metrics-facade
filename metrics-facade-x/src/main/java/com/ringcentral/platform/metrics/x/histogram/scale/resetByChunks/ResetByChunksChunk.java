package com.ringcentral.platform.metrics.x.histogram.scale.resetByChunks;

import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig;
import com.ringcentral.platform.metrics.x.histogram.scale.internal.*;

public class ResetByChunksChunk extends Chunk {

    public ResetByChunksChunk(ScaleXHistogramImplConfig config, MeasurementSpec measurementSpec) {
        super(config, measurementSpec, true);
    }

    @Override
    protected void updateTree(ScaleTreeNode node, boolean snapshotInProgress, long snapshotNum) {
        long nodeSubtreeUpdateCount = node.subtreeUpdateCount.sum();
        long nodeUpdateEpoch = node.updateEpoch.get();
        long treeUpdateEpoch = tree.updateEpoch;

        while (node != null) {
            if (snapshotInProgress && node.snapshotNum.get() < snapshotNum) {
                if (treeUpdateEpoch == nodeUpdateEpoch) {
                    node.snapshotSubtreeUpdateCount = nodeSubtreeUpdateCount;
                    node.snapshotNum.set(snapshotNum);
                    node.subtreeUpdateCount.increment();
                } else {
                    node.snapshotSubtreeUpdateCount = 0L;
                    node.snapshotNum.set(snapshotNum);

                    if (node.updateEpoch.compareAndSet(nodeUpdateEpoch, treeUpdateEpoch)) {
                        node.subtreeUpdateCount.add(-nodeSubtreeUpdateCount + 1L);
                    } else {
                        node.subtreeUpdateCount.increment();
                    }
                }
            } else {
                if (treeUpdateEpoch == nodeUpdateEpoch || !node.updateEpoch.compareAndSet(nodeUpdateEpoch, treeUpdateEpoch)) {
                    node.subtreeUpdateCount.increment();
                } else {
                    node.subtreeUpdateCount.add(-nodeSubtreeUpdateCount + 1L);
                }
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

        if (useLazy && node.level < upperLazyTreeLevel) {
            return lazySubtreeUpdateCountFor(node);
        }

        if (node.updateEpoch.get() < tree.updateEpoch) {
            return 0L;
        }

        long nodeSubtreeUpdateCount = node.subtreeUpdateCount.sum();

        return
            node.snapshotNum.get() < tree.snapshotNum ?
            nodeSubtreeUpdateCount  :
            node.snapshotSubtreeUpdateCount;
    }
}
