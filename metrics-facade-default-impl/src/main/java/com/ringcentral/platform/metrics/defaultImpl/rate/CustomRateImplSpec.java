package com.ringcentral.platform.metrics.defaultImpl.rate;

import com.ringcentral.platform.metrics.defaultImpl.rate.configs.RateImplConfig;

import static java.util.Objects.requireNonNull;

public class CustomRateImplSpec<C extends RateImplConfig> {

    private final CustomRateImplMaker<C> implMaker;

    public CustomRateImplSpec(CustomRateImplMaker<C> implMaker) {
        this.implMaker = requireNonNull(implMaker);
    }

    public CustomRateImplMaker<C> implMaker() {
        return implMaker;
    }
}
