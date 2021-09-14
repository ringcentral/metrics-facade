package com.ringcentral.platform.metrics.dropwizard.histogram;

import com.codahale.metrics.Histogram;
import com.ringcentral.platform.metrics.AbstractMeter.*;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;

public class DropwizardExpirableHistogramInstance extends AbstractExpirableMeterInstance<Histogram> implements HistogramInstance {

    protected DropwizardExpirableHistogramInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean levelInstance,
        MeasurableValuesProvider measurableValuesProvider,
        Map<Measurable, MeasurableValueProvider<Histogram>> measurableValueProviders,
        Histogram histogram,
        long creationTimeMs) {

        super(
            name,
            dimensionValues,
            totalInstance,
            dimensionalTotalInstance,
            levelInstance,
            measurableValuesProvider,
            measurableValueProviders,
            histogram,
            creationTimeMs);
    }
}
