package com.ringcentral.platform.metrics.defaultImpl.rate.ema.configs;

import com.ringcentral.platform.metrics.defaultImpl.rate.configs.RateImplConfigBuilder;

public class ExpMovingAverageRateImplConfigBuilder implements RateImplConfigBuilder<ExpMovingAverageRateImplConfig> {

    public static ExpMovingAverageRateImplConfigBuilder expMovingAverage() {
        return expMovingAverageRateImplConfigBuilder();
    }

    public static ExpMovingAverageRateImplConfigBuilder expMovingAverageImpl() {
        return expMovingAverageRateImplConfigBuilder();
    }

    public static ExpMovingAverageRateImplConfigBuilder expMovingAverageRateImplConfigBuilder() {
        return new ExpMovingAverageRateImplConfigBuilder();
    }

    public ExpMovingAverageRateImplConfig build() {
        return ExpMovingAverageRateImplConfig.DEFAULT;
    }
}
