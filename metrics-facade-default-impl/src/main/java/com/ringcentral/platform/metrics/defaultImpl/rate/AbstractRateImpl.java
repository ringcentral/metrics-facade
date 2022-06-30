package com.ringcentral.platform.metrics.defaultImpl.rate;

import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.rate.Rate.MeanRate;
import com.ringcentral.platform.metrics.utils.TimeNanosProvider;

import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

import static com.ringcentral.platform.metrics.utils.TimeUnitUtils.NANOS_PER_SEC;

public abstract class AbstractRateImpl implements RateImpl {

    protected final TimeNanosProvider timeNanosProvider;
    protected final long startTime;
    protected final LongAdder counter;

    protected AbstractRateImpl(
        Set<? extends Measurable> measurables,
        TimeNanosProvider timeNanosProvider) {

        this.timeNanosProvider = timeNanosProvider;
        this.startTime = timeNanosProvider.timeNanos();

        this.counter =
            measurables.stream().anyMatch(m -> m instanceof Count || m instanceof MeanRate) ?
            new LongAdder() :
            null;
    }

    @Override
    public void mark(long count) {
        if (counter != null) {
            counter.add(count);
        }

        markForRates(count);
    }

    protected abstract void markForRates(long count);

    @Override
    public long count() {
        return counter != null ? counter.sum() : 0L;
    }

    @Override
    public double meanRate() {
        long count = count();

        if (count > 0) {
            double elapsed = timeNanosProvider.timeNanos() - startTime;
            return count / elapsed * NANOS_PER_SEC;
        }

        return 0.0;
    }
}
