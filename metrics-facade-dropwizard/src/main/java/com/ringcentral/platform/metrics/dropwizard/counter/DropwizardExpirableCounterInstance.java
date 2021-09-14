package com.ringcentral.platform.metrics.dropwizard.counter;

import java.util.*;
import com.codahale.metrics.Counter;
import com.ringcentral.platform.metrics.AbstractMeter.*;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

public class DropwizardExpirableCounterInstance extends AbstractExpirableMeterInstance<Counter> implements CounterInstance {

    protected DropwizardExpirableCounterInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean levelInstance,
        MeasurableValuesProvider measurableValuesProvider,
        Map<Measurable, MeasurableValueProvider<Counter>> measurableValueProviders,
        Counter counter,
        long creationTimeMs) {

        super(
            name,
            dimensionValues,
            totalInstance,
            dimensionalTotalInstance,
            levelInstance,
            measurableValuesProvider,
            measurableValueProviders,
            counter,
            creationTimeMs);
    }
}