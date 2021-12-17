package com.ringcentral.platform.metrics.x.histogram;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.utils.*;

import java.util.Set;

public class HdrHistogram implements XHistogramImpl {

    public HdrHistogram(
        HdrHistogramConfig config,
        Set<? extends Measurable> measurables) {

        this(
            config,
            measurables,
            SystemTimeNanosProvider.INSTANCE);
    }

    public HdrHistogram(
        HdrHistogramConfig config,
        Set<? extends Measurable> measurables,
        TimeNanosProvider timeNanosProvider) {

        // TODO
    }

    @Override
    public void update(long value) {
        // TODO
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public XHistogramSnapshot snapshot() {
        return null;
    }
}
