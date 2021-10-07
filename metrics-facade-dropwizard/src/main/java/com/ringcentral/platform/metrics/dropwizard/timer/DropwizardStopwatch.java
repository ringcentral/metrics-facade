package com.ringcentral.platform.metrics.dropwizard.timer;

import com.codahale.metrics.Clock;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.timer.Stopwatch;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.NO_DIMENSION_VALUES;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class DropwizardStopwatch implements Stopwatch {

    private final DropwizardTimer timer;
    private final MetricDimensionValues dimensionValues;
    private final long startTime;

    public DropwizardStopwatch(DropwizardTimer timer, MetricDimensionValues dimensionValues) {
        this.timer = timer;
        this.dimensionValues = dimensionValues;
        this.startTime = Clock.defaultClock().getTick();
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
        long time = Clock.defaultClock().getTick() - startTime;
        timer.update(time, NANOSECONDS, dimValues);
        return time;
    }
}
