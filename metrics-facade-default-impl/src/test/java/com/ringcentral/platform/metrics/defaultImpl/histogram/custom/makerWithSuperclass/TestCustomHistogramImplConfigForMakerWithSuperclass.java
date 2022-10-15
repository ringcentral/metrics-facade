package com.ringcentral.platform.metrics.defaultImpl.histogram.custom.makerWithSuperclass;

import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.AbstractHistogramImplConfig;

public class TestCustomHistogramImplConfigForMakerWithSuperclass extends AbstractHistogramImplConfig {

    private final long measurableValue;

    public TestCustomHistogramImplConfigForMakerWithSuperclass(long measurableValue) {
        this.measurableValue = measurableValue;
    }

    public long measurableValue() {
        return measurableValue;
    }
}
