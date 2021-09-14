package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.AbstractMeter.AbstractExpirableMeterInstance;
import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValueProvider;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.measurables.AbstractMeasurableValues;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateInstance;
import com.ringcentral.platform.metrics.timer.TimerInstance;

import java.util.List;
import java.util.Map;

public class StubExpirableMeterInstance
    extends AbstractExpirableMeterInstance<Object>
    implements CounterInstance, TimerInstance, RateInstance, HistogramInstance {

    protected StubExpirableMeterInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean levelInstance,
        Map<Measurable, MeasurableValueProvider<Object>> measurableValueProviders,
        Object meter,
        long creationTimeMs) {

        super(
            name,
            dimensionValues,
            totalInstance,
            dimensionalTotalInstance,
            levelInstance,
            () -> new AbstractMeasurableValues(measurableValueProviders.keySet()) {

                @Override
                @SuppressWarnings("unchecked")
                protected <V> V valueOfImpl(Measurable measurable) {
                    return (V)measurableValueProviders.get(measurable).valueFor(null);
                }
            },
            measurableValueProviders,
            meter,
            creationTimeMs);
    }
}