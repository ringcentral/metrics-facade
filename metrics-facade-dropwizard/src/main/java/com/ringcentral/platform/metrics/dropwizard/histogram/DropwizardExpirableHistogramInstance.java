package com.ringcentral.platform.metrics.dropwizard.histogram;

import com.codahale.metrics.Histogram;
import com.ringcentral.platform.metrics.AbstractMeter.*;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;

public class DropwizardExpirableHistogramInstance extends AbstractExpirableMeterInstance<Histogram> implements HistogramInstance {

    protected DropwizardExpirableHistogramInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean levelInstance,
        MeasurableValuesProvider measurableValuesProvider,
        Map<Measurable, MeasurableValueProvider<Histogram>> measurableValueProviders,
        Histogram histogram,
        long creationTimeMs) {

        super(
            name,
            labelValues,
            totalInstance,
            labeledMetricTotalInstance,
            levelInstance,
            measurableValuesProvider,
            measurableValueProviders,
            histogram,
            creationTimeMs);
    }
}
