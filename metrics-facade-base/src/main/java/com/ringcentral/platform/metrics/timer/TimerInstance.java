package com.ringcentral.platform.metrics.timer;

import com.ringcentral.platform.metrics.MeterInstance;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public interface TimerInstance extends MeterInstance {
    default TimeUnit durationUnit() {
        return MILLISECONDS;
    }
}