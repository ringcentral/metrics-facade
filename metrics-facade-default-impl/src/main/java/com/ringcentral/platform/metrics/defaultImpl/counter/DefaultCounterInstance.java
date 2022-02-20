package com.ringcentral.platform.metrics.defaultImpl.counter;

import com.ringcentral.platform.metrics.AbstractMeter.*;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;
import java.util.concurrent.atomic.LongAdder;

public class DefaultCounterInstance extends AbstractMeterInstance<LongAdder> implements CounterInstance {

    protected DefaultCounterInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean levelInstance,
        MeasurableValuesProvider measurableValuesProvider,
        Map<Measurable, MeasurableValueProvider<LongAdder>> measurableValueProviders,
        LongAdder counter) {

        super(
            name,
            dimensionValues,
            totalInstance,
            dimensionalTotalInstance,
            levelInstance,
            measurableValuesProvider,
            measurableValueProviders,
            counter);
    }
}