package com.ringcentral.platform.metrics.x.histogram.scale.internal;

public interface MultiNode {
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
