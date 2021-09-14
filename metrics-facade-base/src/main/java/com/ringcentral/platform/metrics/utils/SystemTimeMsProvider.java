package com.ringcentral.platform.metrics.utils;

import static java.lang.System.*;

public class SystemTimeMsProvider implements TimeMsProvider {

    public static final SystemTimeMsProvider INSTANCE = new SystemTimeMsProvider();

    @Override
    public long timeMs() {
        return currentTimeMillis();
    }
}
