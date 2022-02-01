package com.ringcentral.platform.metrics.samples.histogram;

import com.ringcentral.platform.metrics.x.histogram.configs.XHistogramImplConfigBuilder;

public class LastValueXHistogramConfigBuilder implements XHistogramImplConfigBuilder<LastValueXHistogramImplConfig> {

    // DSL method
    public static LastValueXHistogramConfigBuilder lastValueImpl() {
        return new LastValueXHistogramConfigBuilder();
    }

    @Override
    public LastValueXHistogramImplConfig build() {
        return new LastValueXHistogramImplConfig();
    }
}
