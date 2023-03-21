package com.ringcentral.platform.metrics.dropwizard.timer;

import com.codahale.metrics.Clock;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.timer.Stopwatch;

import static com.ringcentral.platform.metrics.labels.LabelValues.NO_LABEL_VALUES;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class DropwizardStopwatch implements Stopwatch {

    private final DropwizardTimer timer;
    private final LabelValues labelValues;
    private final long startTime;

    public DropwizardStopwatch(DropwizardTimer timer, LabelValues labelValues) {
        this.timer = timer;
        this.labelValues = labelValues;
        this.startTime = Clock.defaultClock().getTick();
    }

    @Override
    public long stop() {
        return stopFor(labelValues != null ? labelValues : NO_LABEL_VALUES);
    }

    @Override
    public long stop(LabelValues labelValues) {
        requireNonNull(labelValues);
        checkState(this.labelValues == null, "Label values change is not allowed");
        return stopFor(labelValues);
    }

    private long stopFor(LabelValues labelValues) {
        long time = Clock.defaultClock().getTick() - startTime;
        timer.update(time, NANOSECONDS, labelValues);
        return time;
    }
}
