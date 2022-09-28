package com.ringcentral.platform.metrics.samples;

public interface SamplesProducer<
    S extends Sample,
    SS extends SampleSpec,
    ISS extends InstanceSampleSpec,
    IS extends InstanceSample<S>> {

    void produceSamples(SS spec, ISS instanceSampleSpec, IS instanceSample);
}
