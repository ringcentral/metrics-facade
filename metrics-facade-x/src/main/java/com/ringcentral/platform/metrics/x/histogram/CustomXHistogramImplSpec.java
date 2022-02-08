package com.ringcentral.platform.metrics.x.histogram;

import com.ringcentral.platform.metrics.x.histogram.configs.XHistogramImplConfig;

import static java.util.Objects.requireNonNull;

public class CustomXHistogramImplSpec<C extends XHistogramImplConfig> {

    private final CustomXHistogramImplMaker<C> implMaker;

    public CustomXHistogramImplSpec(CustomXHistogramImplMaker<C> implMaker) {
        this.implMaker = requireNonNull(implMaker);
    }

    public CustomXHistogramImplMaker<C> implMaker() {
        return implMaker;
    }
}
