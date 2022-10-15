package com.ringcentral.platform.metrics.samples.histogram;

import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.HistogramImplConfigBuilder;

public class CountAndTotalSumScalingHistogramConfigBuilder implements HistogramImplConfigBuilder<CountAndTotalSumScalingHistogramImplConfig> {

    private long factor;

    public static CountAndTotalSumScalingHistogramConfigBuilder countAndTotalSumScaling() {
        return new CountAndTotalSumScalingHistogramConfigBuilder();
    }

    public CountAndTotalSumScalingHistogramConfigBuilder factor(long factor) {
        this.factor = factor;
        return this;
    }

    @Override
    public CountAndTotalSumScalingHistogramImplConfig build() {
        return new CountAndTotalSumScalingHistogramImplConfig(factor);
    }
}
