package com.ringcentral.platform.metrics.defaultImpl.histogram;

import com.ringcentral.platform.metrics.AbstractMeter.*;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;

public class DefaultHistogramInstance extends AbstractMeterInstance<HistogramImpl> implements HistogramInstance {

    protected DefaultHistogramInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean levelInstance,
        MeasurableValuesProvider measurableValuesProvider,
        Map<Measurable, MeasurableValueProvider<HistogramImpl>> measurableValueProviders,
        HistogramImpl histogram) {

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

    @Override
    public void metricInstanceAdded() {
        super.metricInstanceAdded();
        meterImpl().metricInstanceAdded();
    }

    @Override
    public void metricInstanceRemoved() {
        super.metricInstanceRemoved();
        meterImpl().metricInstanceRemoved();
    }
}
