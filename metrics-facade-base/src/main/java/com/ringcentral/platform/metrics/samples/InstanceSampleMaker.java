package com.ringcentral.platform.metrics.samples;

public interface InstanceSampleMaker<
    S extends Sample,
    IS extends InstanceSample<S>,
    ISS extends InstanceSampleSpec> {

    IS makeInstanceSample(ISS spec);
}
