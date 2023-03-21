package com.ringcentral.platform.metrics.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.timer.Timer;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.distribution.*;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static io.micrometer.core.instrument.util.TimeUtils.nanosToUnit;
import static java.util.concurrent.TimeUnit.*;

public class MfTimer extends AbstractTimer implements MfMeter {

    private final MfMeterBase base;
    private final Timer mfTimer;
    private final LongAdder count = new LongAdder();
    private final TimeWindowMax max;
    private final LongAdder total = new LongAdder();

    public MfTimer(
        MetricRegistry mfRegistry,
        Id id,
        Clock clock,
        DistributionStatisticConfig distributionStatisticConfig,
        PauseDetector pauseDetector) {

        super(
            id,
            clock,
            distributionStatisticConfig,
            pauseDetector,
            MILLISECONDS,
            false);

        this.base = new MfMeterBase(mfRegistry, id);
        this.max = new TimeWindowMax(clock, distributionStatisticConfig);

        this.mfTimer =
            this.base.hasLabels() ?
            mfRegistry.timer(this.base.name(), () -> withTimer().labels(this.base.labels())) :
            mfRegistry.timer(this.base.name());
    }

    @Override
    protected void recordNonNegative(long amount, TimeUnit unit) {
        if (amount >= 0) {
            mfTimer.update(amount, unit, base.labelValues());
            count.add(amount);
            long amountNanos = NANOSECONDS.convert(amount, unit);
            max.record(amountNanos, NANOSECONDS);
            total.add(amountNanos);
        }
    }

    @Override
    public long count() {
        return count.sum();
    }

    @Override
    public double totalTime(TimeUnit unit) {
        return nanosToUnit(total.sum(), unit);
    }

    @Override
    public double max(TimeUnit unit) {
        return max.poll(unit);
    }

    @Override
    public void meterRemoved() {
        if (base.hasLabels()) {
            mfTimer.removeInstancesFor(base.labelValues());
        } else {
            base.mfRegistry().remove(base.name());
        }
    }
}
