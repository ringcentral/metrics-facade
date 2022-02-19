package com.ringcentral.platform.metrics.scale;

import java.util.List;

public interface Scale {
    default long point(int i) {
        return points().get(i);
    }

    default long firstPoint() {
        return points().get(0);
    }

    default int pointCount() {
        return points().size();
    }

    List<Long> points();
}
