package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.AbstractMeter.AbstractMeterInstance;
import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValueProvider;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.measurables.AbstractMeasurableValues;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateInstance;
import com.ringcentral.platform.metrics.timer.TimerInstance;

import java.util.List;
import java.util.Map;

public class StubMeterInstance
    extends AbstractMeterInstance<Object>
    implements CounterInstance, TimerInstance, RateInstance, HistogramInstance {

    public StubMeterInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean levelInstance,
        Map<Measurable, MeasurableValueProvider<Object>> measurableValueProviders,
        Object meter) {

        super(
            name,
            labelValues,
            totalInstance,
            labeledMetricTotalInstance,
            levelInstance,
            () -> new AbstractMeasurableValues(measurableValueProviders.keySet()) {

                @Override
                @SuppressWarnings("unchecked")
                protected <V> V valueOfImpl(Measurable measurable) {
                    return (V)measurableValueProviders.get(measurable).valueFor(null);
                }
            },
            measurableValueProviders,
            meter);
    }
}
