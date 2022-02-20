package com.ringcentral.platform.metrics.defaultImpl.timer;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.timer.Stopwatch;
import com.ringcentral.platform.metrics.utils.*;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.NO_DIMENSION_VALUES;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class DefaultStopwatch implements Stopwatch {

    private final DefaultTimer timer;
    private final MetricDimensionValues dimensionValues;
    private final TimeNanosProvider timeNanosProvider;
    private final long startTime;

    public DefaultStopwatch(
        DefaultTimer timer,
        MetricDimensionValues dimensionValues) {

        this(
            timer,
            dimensionValues,
            SystemTimeNanosProvider.INSTANCE);
    }

    public DefaultStopwatch(
        DefaultTimer timer,
        MetricDimensionValues dimensionValues,
        TimeNanosProvider timeNanosProvider) {

        this.timer = timer;
        this.dimensionValues = dimensionValues;
        this.timeNanosProvider = timeNanosProvider;
        this.startTime = timeNanosProvider.timeNanos();
    }

    @Override
    public long stop() {
        return stopFor(dimensionValues != null ? dimensionValues : NO_DIMENSION_VALUES);
    }

    @Override
    public long stop(MetricDimensionValues dimensionValues) {
        requireNonNull(dimensionValues);
        checkState(this.dimensionValues == null, "Dimension values change is not allowed");
        return stopFor(dimensionValues);
    }

    private long stopFor(MetricDimensionValues dimValues) {
        long time = timeNanosProvider.timeNanos() - startTime;
        timer.update(time, NANOSECONDS, dimValues);
        return time;
    }
}
