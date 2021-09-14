package com.ringcentral.platform.metrics.micrometer;

import io.micrometer.core.instrument.Meter;

public interface MfMeter extends Meter {
    void meterRemoved();
}
