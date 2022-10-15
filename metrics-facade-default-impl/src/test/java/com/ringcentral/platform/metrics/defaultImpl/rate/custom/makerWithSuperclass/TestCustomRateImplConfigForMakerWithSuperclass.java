package com.ringcentral.platform.metrics.defaultImpl.rate.custom.makerWithSuperclass;

import com.ringcentral.platform.metrics.defaultImpl.rate.configs.RateImplConfig;

public class TestCustomRateImplConfigForMakerWithSuperclass implements RateImplConfig {

    private final long measurableValue;

    public TestCustomRateImplConfigForMakerWithSuperclass(long measurableValue) {
        this.measurableValue = measurableValue;
    }

    public long measurableValue() {
        return measurableValue;
    }
}
