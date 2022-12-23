package com.ringcentral.platform.metrics.samples.rate;

import com.ringcentral.platform.metrics.defaultImpl.rate.configs.RateImplConfig;

public class CountScalingRateImplConfig implements RateImplConfig {

    private final long factor;

    public CountScalingRateImplConfig(long factor) {
        this.factor = factor;
    }

    public long factor() {
        return factor;
    }
}
