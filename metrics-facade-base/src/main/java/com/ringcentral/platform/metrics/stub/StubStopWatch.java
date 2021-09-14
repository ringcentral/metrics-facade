package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.timer.Stopwatch;

public class StubStopWatch implements Stopwatch {

    @Override
    public long stop() {
        return 0;
    }
}
