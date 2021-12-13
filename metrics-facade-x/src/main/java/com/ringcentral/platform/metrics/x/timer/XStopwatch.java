package com.ringcentral.platform.metrics.x.timer;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.timer.Stopwatch;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.NO_DIMENSION_VALUES;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class XStopwatch implements Stopwatch {

    private final XTimer timer;
    private final MetricDimensionValues dimensionValues;
    private final long startTime;

    public XStopwatch(XTimer timer, MetricDimensionValues dimensionValues) {
        this.timer = timer;
        this.dimensionValues = dimensionValues;
        this.startTime = 1L; // Clock.defaultClock().getTick();
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
        long time = 1L; // Clock.defaultClock().getTick() - startTime;
        timer.update(time, NANOSECONDS, dimValues);
        return time;
    }
}
