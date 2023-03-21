package com.ringcentral.platform.metrics.meter;

import com.ringcentral.platform.metrics.AbstractMeter.*;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;

public abstract class TestMeterInstance<MI> extends AbstractMeterInstance<MI> {

    final MI meterImpl;
    final List<Long> updateValues = new ArrayList<>();

    public TestMeterInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean levelInstance,
        Map<Measurable, MeasurableValueProvider<MI>> measurableValueProviders,
        MI meterImpl) {

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
                    return (V)measurableValueProviders.get(measurable).valueFor(meterImpl);
                }
            },
            measurableValueProviders,
            meterImpl);

        this.meterImpl = meterImpl;
    }

    @Override
    protected void update(long value, MeterImplUpdater<MI> meterImplUpdater) {
        super.update(value, meterImplUpdater);
        updateValues.add(value);
    }

    public MI meterImpl() {
        return meterImpl;
    }

    public List<Long> updateValues() {
        return updateValues;
    }
}
