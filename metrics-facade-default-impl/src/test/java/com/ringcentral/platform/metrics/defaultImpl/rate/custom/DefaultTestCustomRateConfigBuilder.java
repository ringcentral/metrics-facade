package com.ringcentral.platform.metrics.defaultImpl.rate.custom;

public class DefaultTestCustomRateConfigBuilder extends AbstractTestCustomRateConfigBuilder<
    DefaultTestCustomRateImplConfig,
    DefaultTestCustomRateConfigBuilder> {

    public static DefaultTestCustomRateConfigBuilder customRate_Default() {
        return new DefaultTestCustomRateConfigBuilder();
    }

    @Override
    public DefaultTestCustomRateImplConfig build() {
        return new DefaultTestCustomRateImplConfig(measurableValue);
    }
}
