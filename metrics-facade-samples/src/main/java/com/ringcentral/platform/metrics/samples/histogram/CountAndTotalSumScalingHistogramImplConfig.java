package com.ringcentral.platform.metrics.samples.histogram;

import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.AbstractHistogramImplConfig;

public class CountAndTotalSumScalingHistogramImplConfig extends AbstractHistogramImplConfig {

    private final long factor;

    public CountAndTotalSumScalingHistogramImplConfig(long factor) {
        this.factor = factor;
    }

    public long factor() {
        return factor;
    }
}
