package com.ringcentral.platform.metrics.samples;

public abstract class AbstractSamplesProducer<
    S extends Sample,
    SS extends SampleSpec,
    ISS extends InstanceSampleSpec,
    IS extends InstanceSample<S>> implements SamplesProducer<S, SS, ISS, IS> {

    public void produceSamples(SS spec, ISS instanceSampleSpec, IS instanceSample) {
        instanceSample.add(makeSample(spec, instanceSampleSpec, instanceSample));
    }

    protected abstract S makeSample(SS spec, ISS instanceSampleSpec, IS instanceSample);
}
