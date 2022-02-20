package com.ringcentral.platform.metrics.defaultImpl.histogram;

import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.HistogramImplConfig;

import static java.util.Objects.requireNonNull;

public class CustomHistogramImplSpec<C extends HistogramImplConfig> {

    private final CustomHistogramImplMaker<C> implMaker;

    public CustomHistogramImplSpec(CustomHistogramImplMaker<C> implMaker) {
        this.implMaker = requireNonNull(implMaker);
    }

    public CustomHistogramImplMaker<C> implMaker() {
        return implMaker;
    }
}
