package com.ringcentral.platform.metrics.x.rate.ema.configs;

import com.ringcentral.platform.metrics.x.rate.configs.XRateImplConfigBuilder;

public class ExpMovingAverageXRateImplConfigBuilder implements XRateImplConfigBuilder<ExpMovingAverageXRateImplConfig> {

    public static ExpMovingAverageXRateImplConfigBuilder expMovingAverage() {
        return expMovingAverageXRateImplConfigBuilder();
    }

    public static ExpMovingAverageXRateImplConfigBuilder expMovingAverageImpl() {
        return expMovingAverageXRateImplConfigBuilder();
    }

    public static ExpMovingAverageXRateImplConfigBuilder expMovingAverageXImpl() {
        return expMovingAverageXRateImplConfigBuilder();
    }

    public static ExpMovingAverageXRateImplConfigBuilder expMovingAverageXRateImplConfigBuilder() {
        return new ExpMovingAverageXRateImplConfigBuilder();
    }

    public ExpMovingAverageXRateImplConfig build() {
        return ExpMovingAverageXRateImplConfig.DEFAULT;
    }
}
