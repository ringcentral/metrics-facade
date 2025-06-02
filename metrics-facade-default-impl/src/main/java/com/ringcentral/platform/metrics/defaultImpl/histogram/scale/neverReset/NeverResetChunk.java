package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.neverReset;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfig;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal.*;

public class NeverResetChunk extends Chunk {

    public NeverResetChunk(ScaleHistogramImplConfig config, MeasurementSpec measurementSpec) {
        super(config, measurementSpec, false);
    }

    @Override
    protected void updateTree(ScaleTreeNode node, boolean snapshotInProgress, long snapshotNum) {
        while (node != null) {
            if (snapshotInProgress && node.snapshotNum.get() < snapshotNum) {
                long subtreeUpdateCount = node.subtreeUpdateCount.get();

                if (node.snapshotNum.get() < snapshotNum) {
                    node.snapshotSubtreeUpdateCount = subtreeUpdateCount;
                    node.snapshotNum.set(snapshotNum);
                }
            }

            node.subtreeUpdateCount.incrementAndGet();

            if (node.level > upperLazyTreeLevel) {
                node = node.parent;
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

        long nodeSubtreeUpdateCount = node.subtreeUpdateCount.get();
        long nodeSnapshotNum = node.snapshotNum.get();

        return
            nodeSnapshotNum < tree.snapshotNum ?
            nodeSubtreeUpdateCount  :
            node.snapshotSubtreeUpdateCount;
    }
}