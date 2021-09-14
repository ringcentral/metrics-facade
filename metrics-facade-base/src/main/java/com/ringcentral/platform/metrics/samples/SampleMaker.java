package com.ringcentral.platform.metrics.samples;

public interface SampleMaker<S extends Sample, SS extends SampleSpec, ISS extends InstanceSampleSpec> {
    S makeSample(SS spec, ISS instanceSampleSpec);
}
