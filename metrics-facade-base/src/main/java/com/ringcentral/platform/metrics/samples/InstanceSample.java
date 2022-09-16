package com.ringcentral.platform.metrics.samples;

import java.util.List;

public interface InstanceSample<S extends Sample<S>> {
    void add(S sample);
    List<S> samples();

    default boolean isEmpty() {
        return samples().isEmpty();
    }
}
