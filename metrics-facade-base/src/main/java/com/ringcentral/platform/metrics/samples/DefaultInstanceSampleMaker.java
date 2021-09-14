package com.ringcentral.platform.metrics.samples;

public class DefaultInstanceSampleMaker implements InstanceSampleMaker<
    DefaultSample,
    InstanceSample<DefaultSample>,
    DefaultInstanceSampleSpec> {

    @Override
    public InstanceSample<DefaultSample> makeInstanceSample(DefaultInstanceSampleSpec spec) {
        return new DefaultInstanceSample();
    }
}
