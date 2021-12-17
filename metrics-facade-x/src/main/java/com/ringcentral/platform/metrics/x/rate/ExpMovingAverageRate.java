package com.ringcentral.platform.metrics.x.rate;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.rate.Rate.*;
import com.ringcentral.platform.metrics.utils.*;

import java.util.Set;
import java.util.concurrent.atomic.*;

import static java.lang.Math.exp;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ExpMovingAverageRate extends AbstractXRateImpl {

    private static final long TICK_INTERVAL = SECONDS.toNanos(5);

    private final ExpMovingAverage m1;
    private final ExpMovingAverage m5;
    private final ExpMovingAverage m15;
    private final AtomicLong lastTick;

    public ExpMovingAverageRate(
        ExpMovingAverageRateConfig config,
        Set<? extends Measurable> measurables) {

        this(
            config,
            measurables,
            SystemTimeNanosProvider.INSTANCE);
    }

    public ExpMovingAverageRate(
        ExpMovingAverageRateConfig config,
        Set<? extends Measurable> measurables,
        TimeNanosProvider timeNanosProvider) {

        super(measurables, timeNanosProvider);

        this.m1 =
            measurables.stream().anyMatch(m -> m instanceof OneMinuteRate) ?
            ExpMovingAverage.forOneMinute() :
            null;

        this.m5 =
            measurables.stream().anyMatch(m -> m instanceof FiveMinutesRate) ?
            ExpMovingAverage.forFiveMinutes() :
            null;

        this.m15 =
            measurables.stream().anyMatch(m -> m instanceof FifteenMinutesRate) ?
            ExpMovingAverage.forFifteenMinutes() :
            null;

        this.lastTick =
            this.m1 != null || this.m5 != null || this.m15 != null ?
            new AtomicLong(timeNanosProvider.timeNanos()) :
            null;
    }

    @Override
    protected void markForRates(long count) {
        if (lastTick == null) {
            return;
        }

        tickIfNeeded();

        if (m1 != null) {
            m1.mark(count);
        }

        if (m5 != null) {
            m5.mark(count);
        }

        if (m15 != null) {
            m15.mark(count);
        }
    }

    private void tickIfNeeded() {
        long oldTick = lastTick.get();
        long newTick = timeNanosProvider.timeNanos();
        long diff = newTick - oldTick;

        if (diff > TICK_INTERVAL) {
            long newIntervalStartTick = newTick - diff % TICK_INTERVAL;

            if (lastTick.compareAndSet(oldTick, newIntervalStartTick)) {
                long elapsedTicks = diff / TICK_INTERVAL;

                for (long i = 0; i < elapsedTicks; ++i) {
                    if (m1 != null) {
                        m1.tick();
                    }

                    if (m5 != null) {
                        m5.tick();
                    }

                    if (m15 != null) {
                        m15.tick();
                    }
                }
            }
        }
    }

    @Override
    public double oneMinuteRate() {
        tickIfNeeded();
        return m1.rate();
    }

    @Override
    public double fiveMinutesRate() {
        tickIfNeeded();
        return m5.rate();
    }

    @Override
    public double fifteenMinutesRate() {
        tickIfNeeded();
        return m15.rate();
    }

    private static class ExpMovingAverage {

        static final long INTERVAL_SEC = 5L;
        static final double INTERVAL = SECONDS.toNanos(INTERVAL_SEC);
        static final double SECONDS_PER_MINUTE = 60.0;
        static final double M1_ALPHA = 1 - exp(-INTERVAL_SEC / SECONDS_PER_MINUTE / 1);
        static final double M5_ALPHA = 1 - exp(-INTERVAL_SEC / SECONDS_PER_MINUTE / 5);
        static final double M15_ALPHA = 1 - exp(-INTERVAL_SEC / SECONDS_PER_MINUTE / 15);

        final double alpha;
        volatile boolean initialized = false;
        volatile double rate = 0.0;
        final LongAdder uncounted = new LongAdder();

        static ExpMovingAverage forOneMinute() {
            return new ExpMovingAverage(M1_ALPHA);
        }

        static ExpMovingAverage forFiveMinutes() {
            return new ExpMovingAverage(M5_ALPHA);
        }

        static ExpMovingAverage forFifteenMinutes() {
            return new ExpMovingAverage(M15_ALPHA);
        }

        ExpMovingAverage(double alpha) {
            this.alpha = alpha;
        }

        void mark(long count) {
            uncounted.add(count);
        }

        void tick() {
            long count = uncounted.sumThenReset();
            double uncountedRate = count / INTERVAL;

            if (initialized) {
                double oldRate = rate;
                rate = oldRate + alpha * (uncountedRate - oldRate);
            } else {
                rate = uncountedRate;
                initialized = true;
            }
        }

        double rate() {
            return rate * NANOS_PER_SEC;
        }
    }
}
