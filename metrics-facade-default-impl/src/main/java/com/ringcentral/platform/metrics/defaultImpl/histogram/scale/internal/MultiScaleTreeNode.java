package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal;

public interface MultiScaleTreeNode {
    /**
     * Check isNull() before.
     */
    long point();

    boolean isNull();
    long subtreeUpdateCount();

    boolean hasLeft();
    void toLeft();
    long leftSubtreeUpdateCount();

    boolean hasRight();
    void toRight();
    long rightSubtreeUpdateCount();
}
