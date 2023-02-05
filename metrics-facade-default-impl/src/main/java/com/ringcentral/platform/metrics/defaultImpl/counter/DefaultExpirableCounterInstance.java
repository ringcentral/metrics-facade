package com.ringcentral.platform.metrics.defaultImpl.counter;

import com.ringcentral.platform.metrics.AbstractMeter.*;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;
import java.util.concurrent.atomic.LongAdder;

public class DefaultExpirableCounterInstance extends AbstractExpirableMeterInstance<LongAdder> implements CounterInstance {

    protected DefaultExpirableCounterInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean levelInstance,
        MeasurableValuesProvider measurableValuesProvider,
        Map<Measurable, MeasurableValueProvider<LongAdder>> measurableValueProviders,
        LongAdder counter,
        long creationTimeMs) {

        super(
            name,
            labelValues,
            totalInstance,
            labeledMetricTotalInstance,
            levelInstance,
            measurableValuesProvider,
            measurableValueProviders,
            counter,
            creationTimeMs);
    }
}