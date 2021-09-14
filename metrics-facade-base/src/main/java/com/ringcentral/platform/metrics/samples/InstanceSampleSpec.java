package com.ringcentral.platform.metrics.samples;

public interface InstanceSampleSpec {
    boolean isEnabled();
    InstanceSampleSpec modify(InstanceSampleSpec mod);
}