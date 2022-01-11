package com.ringcentral.platform.metrics.x.histogram.hdr;

import org.HdrHistogram.*;

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
