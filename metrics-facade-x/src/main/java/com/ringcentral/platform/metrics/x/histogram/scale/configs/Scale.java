package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import java.util.List;

public interface Scale extends Iterable<Long> {
    default long point(int i) {
        return points().get(i);
    }

    List<Long> points();
    long firstPoint();
    int pointCount();
}
