package com.ringcentral.platform.metrics.utils;

import static java.lang.System.nanoTime;

public class SystemTimeNanosProvider implements TimeNanosProvider {

    public static final SystemTimeNanosProvider INSTANCE = new SystemTimeNanosProvider();

    @Override
    public long timeNanos() {
        return nanoTime();
    }
}
