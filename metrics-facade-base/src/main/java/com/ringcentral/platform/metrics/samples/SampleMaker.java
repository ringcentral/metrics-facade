package com.ringcentral.platform.metrics.samples;

public interface SampleMaker<
    S extends Sample<S>,
    SS extends SampleSpec,
    ISS extends InstanceSampleSpec,
    IS extends InstanceSample<S>> {

    S makeSample(SS spec, ISS instanceSampleSpec, IS instanceSample);
}
