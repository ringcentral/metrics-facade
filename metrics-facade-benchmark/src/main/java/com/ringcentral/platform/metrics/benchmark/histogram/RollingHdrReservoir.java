package com.ringcentral.platform.metrics.benchmark.histogram;

import com.codahale.metrics.*;
import com.github.rollingmetrics.histogram.hdr.RollingHdrHistogram;

import java.util.concurrent.atomic.LongAdder;

import static java.util.Objects.requireNonNull;

public class RollingHdrReservoir implements Reservoir {

    private final RollingHdrHistogram parent;
    private final LongAdder totalSum = new LongAdder();

    public RollingHdrReservoir(RollingHdrHistogram parent) {
        this.parent = requireNonNull(parent);
    }

    @Override
    public int size() {
        return getSnapshot().size();
    }

    @Override
    public void update(long value) {
        totalSum.add(value);
        parent.update(value);
    }

    @Override
    public Snapshot getSnapshot() {
        return new RollingHdrSnapshot(parent.getSnapshot());
    }
}