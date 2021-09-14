package com.ringcentral.platform.metrics.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import io.micrometer.core.instrument.*;

import java.util.function.ToDoubleFunction;

public class MfFunctionCounter<A> extends AbstractMeter implements MfMeter, FunctionCounter {

    private final MfLongGauge<A> gauge;

    public MfFunctionCounter(
        MetricRegistry mfRegistry,
        Id id,
        ToDoubleFunction<A> fun,
        A funArg) {

        super(id);

        this.gauge = new MfLongGauge<>(
            mfRegistry,
            id,
            a -> (long)fun.applyAsDouble(funArg),
            funArg,
            true);
    }

    @Override
    public double count() {
        return gauge.value();
    }

    @Override
    public void meterRemoved() {
        gauge.meterRemoved();
    }
}
