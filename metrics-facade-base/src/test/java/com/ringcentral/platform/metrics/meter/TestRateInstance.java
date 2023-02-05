package com.ringcentral.platform.metrics.meter;

import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValueProvider;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateInstance;

import java.util.*;

public class TestRateInstance extends TestMeterInstance<TestRateImpl> implements RateInstance {

    public TestRateInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean levelInstance,
        Map<Measurable, MeasurableValueProvider<TestRateImpl>> measurableValueProviders,
        TestRateImpl rateImpl) {

        super(
            name,
            labelValues,
            totalInstance,
            labeledMetricTotalInstance,
            levelInstance,
            measurableValueProviders,
            rateImpl);
    }
}
