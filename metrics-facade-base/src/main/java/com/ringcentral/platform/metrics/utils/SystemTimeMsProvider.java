package com.ringcentral.platform.metrics.utils;

import static com.ringcentral.platform.metrics.utils.TimeUnitUtils.NANOS_PER_MS;
import static java.lang.System.*;

public class SystemTimeMsProvider implements TimeMsProvider {

    public static final SystemTimeMsProvider INSTANCE = new SystemTimeMsProvider();

    final long initialTimeNanos = nanoTime();

    @Override
    public long timeMs() {
        return currentTimeMillis();
    }

    @Override
    public long stableTimeMs() {
        return (nanoTime() - initialTimeNanos) / NANOS_PER_MS;
    }
}
