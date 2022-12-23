package com.ringcentral.platform.metrics.defaultImpl.histogram.custom;

public class DefaultTestCustomHistogramConfigBuilder extends AbstractTestCustomHistogramConfigBuilder<
    DefaultTestCustomHistogramImplConfig,
    DefaultTestCustomHistogramConfigBuilder> {

    public static DefaultTestCustomHistogramConfigBuilder customHistogram_Default() {
        return new DefaultTestCustomHistogramConfigBuilder();
    }

    @Override
    public DefaultTestCustomHistogramImplConfig build() {
        return new DefaultTestCustomHistogramImplConfig(measurableValue);
    }
}
