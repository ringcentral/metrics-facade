package com.ringcentral.platform.metrics.samples.rate;

import com.ringcentral.platform.metrics.defaultImpl.rate.configs.RateImplConfigBuilder;

public class CountScalingRateConfigBuilder implements RateImplConfigBuilder<CountScalingRateImplConfig> {

    private long factor;

    public static CountScalingRateConfigBuilder countScaling() {
        return new CountScalingRateConfigBuilder();
    }

    public CountScalingRateConfigBuilder factor(long factor) {
        this.factor = factor;
        return this;
    }

    @Override
    public CountScalingRateImplConfig build() {
        return new CountScalingRateImplConfig(factor);
    }
}
