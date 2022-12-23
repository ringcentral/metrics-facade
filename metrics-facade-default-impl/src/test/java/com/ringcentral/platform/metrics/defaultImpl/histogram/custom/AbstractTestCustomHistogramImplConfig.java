package com.ringcentral.platform.metrics.defaultImpl.histogram.custom;

import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.AbstractHistogramImplConfig;

public class AbstractTestCustomHistogramImplConfig extends AbstractHistogramImplConfig {

    private final long measurableValue;

    public AbstractTestCustomHistogramImplConfig(long measurableValue) {
        this.measurableValue = measurableValue;
    }

    public long measurableValue() {
        return measurableValue;
    }
}
