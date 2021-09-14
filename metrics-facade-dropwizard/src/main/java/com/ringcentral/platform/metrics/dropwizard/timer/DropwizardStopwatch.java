package com.ringcentral.platform.metrics.dropwizard.timer;

import com.codahale.metrics.Clock;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.timer.Stopwatch;
import static java.util.concurrent.TimeUnit.*;

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
        long time = Clock.defaultClock().getTick() - startTime;
        timer.update(time, NANOSECONDS, dimensionValues);
        return time;
    }
}
