package com.ringcentral.platform.metrics.dropwizard.rate;

import com.codahale.metrics.Meter;
import com.ringcentral.platform.metrics.AbstractMeter.AbstractMeterInstance;
import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValueProvider;
import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValuesProvider;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateInstance;

import java.util.List;
import java.util.Map;

public class DropwizardRateInstance extends AbstractMeterInstance<Meter> implements RateInstance {

    protected DropwizardRateInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean levelInstance,
        MeasurableValuesProvider measurableValuesProvider,
        Map<Measurable, MeasurableValueProvider<Meter>> measurableValueProviders,
        Meter meter) {

        super(
            name,
            labelValues,
            totalInstance,
            labeledMetricTotalInstance,
            levelInstance,
            measurableValuesProvider,
            measurableValueProviders,
            meter);
    }
}