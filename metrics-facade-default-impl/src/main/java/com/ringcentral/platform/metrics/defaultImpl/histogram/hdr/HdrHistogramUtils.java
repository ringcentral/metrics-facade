package com.ringcentral.platform.metrics.defaultImpl.histogram.hdr;

import org.HdrHistogram.*;

/**
 * That's just a slightly refactored version of
 * https://github.com/vladimir-bukhtoyarov/rolling-metrics/blob/3.0/rolling-metrics-core/src/main/java/com/github/rollingmetrics/histogram/hdr/HdrHistogramUtil.java
 * (Copyright 2017 Vladimir Bukhtoyarov Licensed under the Apache License, Version 2.0)
 * We thank Vladimir Bukhtoyarov for his great library.
 */
public class HdrHistogramUtils {

    public static void reset(Histogram histogram) {
        if (histogram.getTotalCount() > 0) {
            histogram.reset();
        }
    }

    public static void addSecondToFirst(Histogram first, Histogram second) {
        if (second.getTotalCount() > 0) {
            first.add(second);
        }
    }

    public static Histogram makeNonConcurrentCopy(Histogram source) {
        if (source instanceof ConcurrentHistogram) {
            return new Histogram(source.getNumberOfSignificantValueDigits());
        } else if (source instanceof AtomicHistogram) {
            return new AtomicHistogram(
                source.getLowestDiscernibleValue(),
                source.getHighestTrackableValue(),
                source.getNumberOfSignificantValueDigits());
        } else {
            throw new IllegalArgumentException("Unsupported histogram type: " + source.getClass());
        }
    }
}
