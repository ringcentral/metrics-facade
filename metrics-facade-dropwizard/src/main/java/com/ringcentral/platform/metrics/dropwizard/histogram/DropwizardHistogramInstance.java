package com.ringcentral.platform.metrics.dropwizard.histogram;

import java.util.*;
import com.codahale.metrics.Histogram;
import com.ringcentral.platform.metrics.AbstractMeter.*;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

public class DropwizardHistogramInstance extends AbstractMeterInstance<Histogram> implements HistogramInstance {

    protected DropwizardHistogramInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean levelInstance,
        MeasurableValuesProvider measurableValuesProvider,
        Map<Measurable, MeasurableValueProvider<Histogram>> measurableValueProviders,
        Histogram histogram) {

        super(
            name,
            labelValues,
            totalInstance,
            labeledMetricTotalInstance,
            levelInstance,
            measurableValuesProvider,
            measurableValueProviders,
            histogram);
    }
}
