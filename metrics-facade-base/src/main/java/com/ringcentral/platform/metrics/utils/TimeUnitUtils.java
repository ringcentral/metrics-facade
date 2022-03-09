package com.ringcentral.platform.metrics.utils;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

public class TimeUnitUtils {

    public static final long NANOS_PER_MS = MILLISECONDS.toNanos(1L);
    public static final long NANOS_PER_SEC = SECONDS.toNanos(1L);
    public static final long MS_PER_SEC = SECONDS.toMillis(1L);
    public static final double MS_TO_SEC_FACTOR = 0.001;

    public static double convertTimeUnit(double amount, TimeUnit from, TimeUnit to) {
        if (from == to) {
            return amount;
        }

        return
            from.ordinal() < to.ordinal() ?
            amount / from.convert(1L, to) :
            amount * to.convert(1L, from);
    }
}
