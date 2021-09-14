package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.var.doubleVar.*;
import com.ringcentral.platform.metrics.var.longVar.*;
import com.ringcentral.platform.metrics.var.objectVar.*;
import com.ringcentral.platform.metrics.var.stringVar.*;

public interface MetricRegistryListener {
    default void objectVarAdded(ObjectVar objectVar) {}
    default void objectVarRemoved(ObjectVar objectVar) {}
    default void cachingObjectVarAdded(CachingObjectVar cachingObjectVar) {}
    default void cachingObjectVarRemoved(CachingObjectVar cachingObjectVar) {}

    default void longVarAdded(LongVar longVar) {}
    default void longVarRemoved(LongVar longVar) {}
    default void cachingLongVarAdded(CachingLongVar cachingLongVar) {}
    default void cachingLongVarRemoved(CachingLongVar cachingLongVar) {}

    default void doubleVarAdded(DoubleVar doubleVar) {}
    default void doubleVarRemoved(DoubleVar doubleVar) {}
    default void cachingDoubleVarAdded(CachingDoubleVar cachingDoubleVar) {}
    default void cachingDoubleVarRemoved(CachingDoubleVar cachingDoubleVar) {}

    default void stringVarAdded(StringVar stringVar) {}
    default void stringVarRemoved(StringVar stringVar) {}
    default void cachingStringVarAdded(CachingStringVar cachingStringVar) {}
    default void cachingStringVarRemoved(CachingStringVar cachingStringVar) {}

    default void counterAdded(Counter counter) {}
    default void counterRemoved(Counter counter) {}

    default void rateAdded(Rate rate) {}
    default void rateRemoved(Rate rate) {}

    default void histogramAdded(Histogram histogram) {}
    default void histogramRemoved(Histogram histogram) {}

    default void timerAdded(Timer timer) {}
    default void timerRemoved(Timer timer) {}
}