package com.ringcentral.platform.metrics.x.histogram;

public class HdrHistogramConfigBuilder implements XHistogramImplConfigBuilder<HdrHistogramConfig> {

    public static HdrHistogramConfigBuilder hdr() {
        return hdrHistogramConfigBuilder();
    }

    public static HdrHistogramConfigBuilder hdrHistogram() {
        return hdrHistogramConfigBuilder();
    }

    public static HdrHistogramConfigBuilder withHdrHistogram() {
        return hdrHistogramConfigBuilder();
    }

    public static HdrHistogramConfigBuilder hdrHistogramConfigBuilder() {
        return new HdrHistogramConfigBuilder();
    }

    public HdrHistogramConfig build() {
        return HdrHistogramConfig.DEFAULT;
    }
}
