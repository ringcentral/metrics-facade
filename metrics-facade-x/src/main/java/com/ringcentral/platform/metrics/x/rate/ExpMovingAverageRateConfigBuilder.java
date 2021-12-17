package com.ringcentral.platform.metrics.x.rate;

public class ExpMovingAverageRateConfigBuilder implements XRateImplConfigBuilder<ExpMovingAverageRateConfig> {

    public static ExpMovingAverageRateConfigBuilder expMovingAverage() {
        return expMovingAverageRateConfigBuilder();
    }

    public static ExpMovingAverageRateConfigBuilder expMovingAverageRate() {
        return expMovingAverageRateConfigBuilder();
    }

    public static ExpMovingAverageRateConfigBuilder withExpMovingAverageRate() {
        return expMovingAverageRateConfigBuilder();
    }

    public static ExpMovingAverageRateConfigBuilder expMovingAverageRateConfigBuilder() {
        return new ExpMovingAverageRateConfigBuilder();
    }

    public ExpMovingAverageRateConfig build() {
        return ExpMovingAverageRateConfig.DEFAULT;
    }
}
