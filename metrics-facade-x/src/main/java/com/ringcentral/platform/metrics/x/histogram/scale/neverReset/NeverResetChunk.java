package com.ringcentral.platform.metrics.x.histogram.scale.neverReset;

import com.ringcentral.platform.metrics.x.histogram.AbstractXHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig;
import com.ringcentral.platform.metrics.x.histogram.scale.internal.*;

public class NeverResetChunk extends Chunk {

    public NeverResetChunk(ScaleXHistogramImplConfig config, MeasurementSpec measurementSpec) {
        super(config, measurementSpec, false);
    }

    @Override
    protected void updateTree(ScaleTreeNode node, boolean snapshotInProgress, long snapshotNum) {
        while (node != null) {
            if (snapshotInProgress && node.snapshotNum.get() < snapshotNum) {
                long subtreeUpdateCount = node.subtreeUpdateCount.sum();

                if (node.snapshotNum.get() < snapshotNum) {
                    node.snapshotSubtreeUpdateCount = subtreeUpdateCount;
                    node.snapshotNum.set(snapshotNum);
                }
            }

            node.subtreeUpdateCount.increment();

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

        long nodeSubtreeUpdateCount = node.subtreeUpdateCount.sum();
        long nodeSnapshotNum = node.snapshotNum.get();

        return
            nodeSnapshotNum < tree.snapshotNum ?
            nodeSubtreeUpdateCount  :
            node.snapshotSubtreeUpdateCount;
    }
}