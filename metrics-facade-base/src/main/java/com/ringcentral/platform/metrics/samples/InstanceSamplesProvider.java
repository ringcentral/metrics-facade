package com.ringcentral.platform.metrics.samples;

import java.util.Set;

public interface InstanceSamplesProvider<S extends Sample<S>, IS extends InstanceSample<S>> {
    Set<IS> instanceSamples();
}
