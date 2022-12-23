package com.ringcentral.platform.metrics.defaultImpl.rate.custom;

import com.ringcentral.platform.metrics.defaultImpl.rate.configs.RateImplConfig;

public class AbstractTestCustomRateImplConfig implements RateImplConfig {

    private final long measurableValue;

    public AbstractTestCustomRateImplConfig(long measurableValue) {
        this.measurableValue = measurableValue;
    }

    public long measurableValue() {
        return measurableValue;
    }
}
