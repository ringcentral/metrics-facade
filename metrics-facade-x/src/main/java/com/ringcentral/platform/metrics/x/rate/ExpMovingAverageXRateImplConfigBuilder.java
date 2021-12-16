package com.ringcentral.platform.metrics.x.rate;

public class ExpMovingAverageXRateImplConfigBuilder implements XRateImplConfigBuilder<ExpMovingAverageXRateImplConfig> {

    public static ExpMovingAverageXRateImplConfigBuilder expMovingAverage() {
        return expMovingAverageXRateImplConfigBuilder();
    }

    public static ExpMovingAverageXRateImplConfigBuilder expMovingAverageXRateImpl() {
        return expMovingAverageXRateImplConfigBuilder();
    }

    public static ExpMovingAverageXRateImplConfigBuilder withExpMovingAverageXRateImpl() {
        return expMovingAverageXRateImplConfigBuilder();
    }

    public static ExpMovingAverageXRateImplConfigBuilder expMovingAverageXRateImplConfigBuilder() {
        return new ExpMovingAverageXRateImplConfigBuilder();
    }

    public ExpMovingAverageXRateImplConfig build() {
        return ExpMovingAverageXRateImplConfig.DEFAULT;
    }
}
