package com.ringcentral.platform.metrics.defaultImpl.histogram;

import com.ringcentral.platform.metrics.AbstractMeter.*;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;

public class DefaultExpirableHistogramInstance extends AbstractExpirableMeterInstance<HistogramImpl> implements HistogramInstance {

    protected DefaultExpirableHistogramInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean levelInstance,
        MeasurableValuesProvider measurableValuesProvider,
        Map<Measurable, MeasurableValueProvider<HistogramImpl>> measurableValueProviders,
        HistogramImpl histogram,
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
