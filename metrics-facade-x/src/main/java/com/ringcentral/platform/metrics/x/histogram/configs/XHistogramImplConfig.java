package com.ringcentral.platform.metrics.x.histogram.configs;

public interface XHistogramImplConfig {
    boolean DEFAULT_BUCKETS_RESETTABLE = false;
    boolean areBucketsResettable();
}
