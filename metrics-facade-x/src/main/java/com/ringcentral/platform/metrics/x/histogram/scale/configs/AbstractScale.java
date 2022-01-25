package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import java.util.Iterator;

public abstract class AbstractScale implements Scale {

    @Override
    public long firstPoint() {
        return points().get(0);
    }

    @Override
    public int pointCount() {
        return points().size();
    }

    @Override
    public Iterator<Long> iterator() {
        return points().iterator();
    }
}
