package com.ringcentral.platform.metrics.samples;

public interface SampleSpec {
    boolean isEnabled();
    SampleSpec modify(SampleSpec mod);
}