package com.ringcentral.platform.metrics.meter;

import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValueProvider;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;

public class TestCounterInstance extends TestMeterInstance<TestCounterImpl> implements CounterInstance {

    public TestCounterInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean levelInstance,
        Map<Measurable, MeasurableValueProvider<TestCounterImpl>> measurableValueProviders,
        TestCounterImpl counterImpl) {

        super(
            name,
            dimensionValues,
            totalInstance,
            dimensionalTotalInstance,
            levelInstance,
            measurableValueProviders,
            counterImpl);
    }
}
