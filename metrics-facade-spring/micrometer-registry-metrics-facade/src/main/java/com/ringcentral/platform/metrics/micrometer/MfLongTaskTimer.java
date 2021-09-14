package com.ringcentral.platform.metrics.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.internal.DefaultLongTaskTimer;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class MfLongTaskTimer extends DefaultLongTaskTimer implements MfMeter {

    private final MfLongGauge<?> activeTasksGauge;
    private final MfDoubleGauge<?> durationGauge;
    private final MfDoubleGauge<?> maxGauge;

    public MfLongTaskTimer(
        MetricRegistry mfRegistry,
        Meter.Id id,
        Clock clock,
        TimeUnit baseTimeUnit,
        DistributionStatisticConfig distributionStatisticConfig) {

        super(
            id,
            clock,
            baseTimeUnit,
            distributionStatisticConfig,
            false);

        this.activeTasksGauge = new MfLongGauge<>(
            mfRegistry,
            id.withName(id.getName() + ".activeTasks"),
            a -> activeTasks(),
            MfLongTaskTimer.class,
            false);

        this.durationGauge = new MfDoubleGauge<>(
            mfRegistry,
            id.withName(id.getName() + ".duration"),
            a -> duration(NANOSECONDS),
            MfLongTaskTimer.class,
            false);

        this.maxGauge = new MfDoubleGauge<>(
            mfRegistry,
            id.withName(id.getName() + ".max"),
            a -> max(NANOSECONDS),
            MfLongTaskTimer.class,
            false);
    }

    @Override
    public void meterRemoved() {
        activeTasksGauge.meterRemoved();
        durationGauge.meterRemoved();
        maxGauge.meterRemoved();
    }
}
