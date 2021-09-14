package com.ringcentral.platform.metrics.counter;

import com.ringcentral.platform.metrics.histogram.HistogramMeasurable;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.rate.RateMeasurable;
import com.ringcentral.platform.metrics.timer.TimerMeasurable;

public interface CounterMeasurable extends Measurable, RateMeasurable, HistogramMeasurable, TimerMeasurable {}
