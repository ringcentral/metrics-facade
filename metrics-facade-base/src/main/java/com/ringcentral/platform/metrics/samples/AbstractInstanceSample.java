package com.ringcentral.platform.metrics.samples;

import java.util.*;

import static java.util.Objects.*;

public class AbstractInstanceSample<S extends Sample> implements InstanceSample<S> {

    protected final List<S> samples;

    public AbstractInstanceSample() {
        this(new ArrayList<>());
    }

    public AbstractInstanceSample(List<S> samples) {
        this.samples = requireNonNull(samples);
    }

    public void add(S sample) {
        samples.add(sample);
    }

    public List<S> samples() {
        return samples;
    }
}
