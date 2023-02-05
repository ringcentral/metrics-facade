package com.ringcentral.platform.metrics.timer;

import com.ringcentral.platform.metrics.labels.LabelValues;

public interface Stopwatch {
    /**
     * @return elapsed time in nanos.
     */
    long stop();

    /**
     * @return elapsed time in nanos.
     */
    long stop(LabelValues labelValues);
}
