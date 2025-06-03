package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal;

public interface MultiScaleTreeNode {
    /**
     * Check isNull() before.
     */
    long point();

    boolean isNull();
    long subtreeUpdateCount();

    void toLeft();
    long leftSubtreeUpdateCount();

    void toRight();
    long rightSubtreeUpdateCount();
}
