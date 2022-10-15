package com.ringcentral.platform.metrics.defaultImpl.rate.custom.makerWithSuperclass;

import com.ringcentral.platform.metrics.defaultImpl.rate.custom.AbstractTestCustomRateConfigBuilder;

public class TestCustomRateConfigBuilderForMakerWithSuperclass extends AbstractTestCustomRateConfigBuilder<
    TestCustomRateImplConfigForMakerWithSuperclass,
    TestCustomRateConfigBuilderForMakerWithSuperclass> {

    public static TestCustomRateConfigBuilderForMakerWithSuperclass customRate_MakerWithSuperclass() {
        return new TestCustomRateConfigBuilderForMakerWithSuperclass();
    }

    @Override
    public TestCustomRateImplConfigForMakerWithSuperclass build() {
        return new TestCustomRateImplConfigForMakerWithSuperclass(measurableValue);
    }
}
