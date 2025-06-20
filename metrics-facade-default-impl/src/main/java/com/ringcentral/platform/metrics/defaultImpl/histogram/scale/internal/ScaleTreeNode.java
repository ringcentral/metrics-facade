package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal;

import java.util.concurrent.atomic.*;

import static com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal.ScaleTree.INITIAL_SNAPSHOT_NUM;

public class ScaleTreeNode {

    public final long point;

    public ScaleTreeNode parent;
    public final ScaleTreeNode left;
    public final ScaleTreeNode right;
    public final int level;
    public int levelOrderIndex;

    public final AtomicLong updateEpoch;
    public final AtomicLong subtreeUpdateCount = new AtomicLong();

    public final AtomicLong snapshotNum = new AtomicLong(INITIAL_SNAPSHOT_NUM);
    public volatile long snapshotSubtreeUpdateCount = Long.MIN_VALUE;

    public ScaleTreeNode(
        long point,
        ScaleTreeNode left,
        ScaleTreeNode right,
        int level,
        boolean resettable,
        long updateEpoch) {

        this.point = point;
        this.left = left;
        this.right = right;
        this.level = level;
        this.updateEpoch = resettable ? new AtomicLong(updateEpoch) : null;
    }

    public boolean isLeftChild() {
        return parent != null && parent.left == this;
    }

    @Override
    public String toString() {
        return Long.toString(point);
    }
}
