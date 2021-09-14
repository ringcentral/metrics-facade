package com.ringcentral.platform.metrics.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.instrument.distribution.*;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;
import io.micrometer.core.lang.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MfMeterRegistry extends MeterRegistry {

    private final MetricRegistry mfRegistry;

    public MfMeterRegistry(MetricRegistry mfRegistry, Clock clock) {
        super(clock);
        this.mfRegistry = mfRegistry;

        config()
            .namingConvention(NamingConvention.identity)
            .onMeterRemoved(this::meterRemoved);
    }

    private void meterRemoved(Meter meter) {
        if (meter instanceof MfMeter) {
            ((MfMeter)meter).meterRemoved();
        }
    }

    @Override
    protected <T> Gauge newGauge(Meter.Id id, @Nullable T obj, ToDoubleFunction<T> valueFunction) {
        return new MfDoubleGauge<>(mfRegistry, id, valueFunction, obj, false);
    }

    @Override
    protected Counter newCounter(Meter.Id id) {
        return new MfCounter(mfRegistry, id);
    }

    @Override
    protected <T> FunctionCounter newFunctionCounter(Meter.Id id, T obj, ToDoubleFunction<T> countFunction) {
        return new MfFunctionCounter<>(mfRegistry, id, countFunction, obj);
    }

    @Override
    protected DistributionSummary newDistributionSummary(
        Meter.Id id,
        DistributionStatisticConfig distributionStatisticConfig,
        double scale) {

        MfDistributionSummary distributionSummary = new MfDistributionSummary(
            mfRegistry,
            id,
            clock,
            distributionStatisticConfig,
            scale);

        HistogramGauges.registerWithCommonFormat(distributionSummary, this);
        return distributionSummary;
    }

    @Override
    protected Timer newTimer(
        Meter.Id id,
        DistributionStatisticConfig distributionStatisticConfig,
        PauseDetector pauseDetector) {

        MfTimer timer = new MfTimer(mfRegistry, id, clock, distributionStatisticConfig, pauseDetector);
        HistogramGauges.registerWithCommonFormat(timer, this);
        return timer;
    }

    @Override
    protected <T> FunctionTimer newFunctionTimer(
        Meter.Id id,
        T obj,
        ToLongFunction<T> countFunction,
        ToDoubleFunction<T> totalTimeFunction,
        TimeUnit totalTimeFunctionUnit) {

        return new MfFunctionTimer<>(
            mfRegistry,
            id,
            countFunction,
            totalTimeFunction,
            totalTimeFunctionUnit,
            getBaseTimeUnit(),
            obj);
    }

    @Override
    protected LongTaskTimer newLongTaskTimer(Meter.Id id, DistributionStatisticConfig distributionStatisticConfig) {
        MfLongTaskTimer longTaskTimer = new MfLongTaskTimer(
            mfRegistry,
            id,
            clock,
            getBaseTimeUnit(),
            distributionStatisticConfig);

        HistogramGauges.registerWithCommonFormat(longTaskTimer, this);
        return longTaskTimer;
    }

    @Override
    protected Meter newMeter(Meter.Id id, Meter.Type type, Iterable<Measurement> measurements) {
        return new MfMeasurementsMeter(mfRegistry, id, type, measurements);
    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
        return MILLISECONDS;
    }

    @Override
    protected DistributionStatisticConfig defaultHistogramConfig() {
        return DistributionStatisticConfig.builder()
            .build()
            .merge(DistributionStatisticConfig.DEFAULT);
    }
}
