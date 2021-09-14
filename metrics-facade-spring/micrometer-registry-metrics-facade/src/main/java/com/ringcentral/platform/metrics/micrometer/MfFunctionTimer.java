package com.ringcentral.platform.metrics.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.util.TimeUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

import static java.lang.Double.isNaN;

public class MfFunctionTimer<A> extends AbstractMeter implements MfMeter, FunctionTimer {

    private final ToLongFunction<A> countFun;
    private final MfLongGauge<A> countGauge;
    private volatile long lastCount;

    private final MfDoubleGauge<A> totalTimeGauge;
    private final TimeUnit totalTimeFunUnit;
    private volatile double lastTotalTime;

    private final WeakReference<A> funArgWeakRef;
    private final TimeUnit baseTimeUnit;

    public MfFunctionTimer(
        MetricRegistry mfRegistry,
        Id id,
        ToLongFunction<A> countFun,
        ToDoubleFunction<A> totalTimeFun,
        TimeUnit totalTimeFunUnit,
        TimeUnit baseTimeUnit,
        A funArg) {

        super(id);

        this.countFun = countFun;

        this.countGauge = new MfLongGauge<>(
            mfRegistry,
            id.withName(id.getName() + ".count"),
            countFun,
            funArg,
            true);

        this.totalTimeGauge = new MfDoubleGauge<>(
            mfRegistry,
            id.withName(id.getName() + ".totalTime"),
            totalTimeFun,
            funArg,
            true);

        this.totalTimeFunUnit = totalTimeFunUnit;
        this.funArgWeakRef = new WeakReference<>(funArg);
        this.baseTimeUnit = baseTimeUnit;
    }

    @Override
    public double count() {
        A funArgRef = funArgWeakRef.get();

        if (funArgRef == null) {
            return lastCount;
        }

        return lastCount = countFun.applyAsLong(funArgRef);
    }

    @Override
    public double totalTime(TimeUnit unit) {
        A funArgRef = funArgWeakRef.get();

        if (funArgRef != null) {
            double totalTime = totalTimeGauge.value();

            if (!isNaN(totalTime)) {
                lastTotalTime = TimeUtils.convert(totalTime, totalTimeFunUnit, baseTimeUnit);
            }
        }

        return TimeUtils.convert(lastTotalTime, baseTimeUnit, unit);
    }

    @Override
    public TimeUnit baseTimeUnit() {
        return baseTimeUnit;
    }

    @Override
    public void meterRemoved() {
        countGauge.meterRemoved();
        totalTimeGauge.meterRemoved();
    }
}
