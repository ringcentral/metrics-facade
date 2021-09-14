package com.ringcentral.platform.metrics.dropwizard.rate;

import com.codahale.metrics.Meter;
import com.ringcentral.platform.metrics.AbstractMeter.*;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateInstance;

import java.util.*;

public class DropwizardExpirableRateInstance extends AbstractExpirableMeterInstance<Meter> implements RateInstance {

    protected DropwizardExpirableRateInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean levelInstance,
        MeasurableValuesProvider measurableValuesProvider,
        Map<Measurable, MeasurableValueProvider<Meter>> measurableValueProviders,
        Meter meter,
        long creationTimeMs) {

        super(
            name,
            dimensionValues,
            totalInstance,
            dimensionalTotalInstance,
            levelInstance,
            measurableValuesProvider,
            measurableValueProviders,
            meter,
            creationTimeMs);
    }
}