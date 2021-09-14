package com.ringcentral.platform.metrics.micrometer;

import io.micrometer.core.instrument.config.MeterRegistryConfig;

public interface MfMeterRegistryConfig extends MeterRegistryConfig {

    String PREFIX = "mf";

    @Override
    default String prefix() {
        return PREFIX;
    }
}
