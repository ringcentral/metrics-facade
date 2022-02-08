package com.ringcentral.platform.metrics.x.rate;

import com.ringcentral.platform.metrics.x.rate.configs.XRateImplConfig;

import static java.util.Objects.requireNonNull;

public class CustomXRateImplSpec<C extends XRateImplConfig> {

    private final CustomXRateImplMaker<C> implMaker;

    public CustomXRateImplSpec(CustomXRateImplMaker<C> implMaker) {
        this.implMaker = requireNonNull(implMaker);
    }

    public CustomXRateImplMaker<C> implMaker() {
        return implMaker;
    }
}
