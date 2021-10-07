package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.timer.Stopwatch;

public class StubStopWatch implements Stopwatch {

    @Override
    public long stop() {
        return 0L;
    }

    @Override
    public long stop(MetricDimensionValues dimensionValues) {
        return 0L;
    }
}
