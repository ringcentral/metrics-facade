package com.ringcentral.platform.metrics.samples.histogram;

import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.HistogramImplConfigBuilder;

public class LastValueHistogramConfigBuilder implements HistogramImplConfigBuilder<LastValueHistogramImplConfig> {

    // DSL method
    public static LastValueHistogramConfigBuilder lastValue() {
        return new LastValueHistogramConfigBuilder();
    }

    @Override
    public LastValueHistogramImplConfig build() {
        return new LastValueHistogramImplConfig();
    }
}
