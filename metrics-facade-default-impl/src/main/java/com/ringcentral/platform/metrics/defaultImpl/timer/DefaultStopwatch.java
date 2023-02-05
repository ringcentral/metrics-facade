package com.ringcentral.platform.metrics.defaultImpl.timer;

import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.timer.Stopwatch;
import com.ringcentral.platform.metrics.utils.*;

import static com.ringcentral.platform.metrics.labels.LabelValues.NO_LABEL_VALUES;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class DefaultStopwatch implements Stopwatch {

    private final DefaultTimer timer;
    private final LabelValues labelValues;
    private final TimeNanosProvider timeNanosProvider;
    private final long startTime;

    public DefaultStopwatch(DefaultTimer timer, LabelValues labelValues) {
        this(
            timer,
            labelValues,
            SystemTimeNanosProvider.INSTANCE);
    }

    public DefaultStopwatch(
        DefaultTimer timer,
        LabelValues labelValues,
        TimeNanosProvider timeNanosProvider) {

        this.timer = timer;
        this.labelValues = labelValues;
        this.timeNanosProvider = timeNanosProvider;
        this.startTime = timeNanosProvider.timeNanos();
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
        long time = timeNanosProvider.timeNanos() - startTime;
        timer.update(time, NANOSECONDS, labelValues);
        return time;
    }
}
