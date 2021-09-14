package com.ringcentral.platform.metrics.meter;

import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValueProvider;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.timer.TimerInstance;

import java.util.*;

public class TestTimerInstance extends TestMeterInstance<TestTimerImpl> implements TimerInstance {

    public TestTimerInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean levelInstance,
        Map<Measurable, MeasurableValueProvider<TestTimerImpl>> measurableValueProviders,
        TestTimerImpl timerImpl) {

        super(
            name,
            dimensionValues,
            totalInstance,
            dimensionalTotalInstance,
            levelInstance,
            measurableValueProviders,
            timerImpl);
    }
}
