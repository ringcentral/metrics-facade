package com.ringcentral.platform.metrics.benchmark.histogram;

import com.codahale.metrics.Snapshot;
import com.github.rollingmetrics.histogram.hdr.RollingSnapshot;

import java.io.*;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RollingHdrSnapshot extends Snapshot {

    private final RollingSnapshot parent;

    public RollingHdrSnapshot(RollingSnapshot parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public double getValue(double quantile) {
        return parent.getValue(quantile);
    }

    @Override
    public long[] getValues() {
        return parent.getValues();
    }

    @Override
    public int size() {
        return getValues().length;
    }

    @Override
    public long getMax() {
        return parent.getMax();
    }

    @Override
    public double getMean() {
        return parent.getMean();
    }

    @Override
    public long getMin() {
        return parent.getMin();
    }

    @Override
    public double getStdDev() {
        return parent.getStdDev();
    }

    @Override
    public void dump(OutputStream output) {
        try (PrintWriter p = new PrintWriter(new OutputStreamWriter(output, UTF_8))) {
            for (long value : getValues()) {
                p.printf("%f%n", (double) value);
            }
        }
    }
}