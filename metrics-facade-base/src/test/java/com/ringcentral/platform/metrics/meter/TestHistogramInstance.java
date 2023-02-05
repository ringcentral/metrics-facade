package com.ringcentral.platform.metrics.meter;

import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValueProvider;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;

public class TestHistogramInstance extends TestMeterInstance<TestHistogramImpl> implements HistogramInstance {

    public TestHistogramInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean levelInstance,
        Map<Measurable, MeasurableValueProvider<TestHistogramImpl>> measurableValueProviders,
        TestHistogramImpl histogramImpl) {

        super(
            name,
            labelValues,
            totalInstance,
            labeledMetricTotalInstance,
            levelInstance,
            measurableValueProviders,
            histogramImpl);
    }
}
