package com.ringcentral.platform.metrics.defaultImpl.timer;

import com.ringcentral.platform.metrics.AbstractMeter.*;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.timer.TimerInstance;

import java.util.*;

public class DefaultTimerInstance extends AbstractMeterInstance<TimerImpl> implements TimerInstance {

    protected DefaultTimerInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean levelInstance,
        MeasurableValuesProvider measurableValuesProvider,
        Map<Measurable, MeasurableValueProvider<TimerImpl>> measurableValueProviders,
        TimerImpl timer) {

        super(
            name,
            dimensionValues,
            totalInstance,
            dimensionalTotalInstance,
            levelInstance,
            measurableValuesProvider,
            measurableValueProviders,
            timer);
    }

    @Override
    public void metricInstanceAdded() {
        super.metricInstanceAdded();
        meterImpl().histogram().metricInstanceAdded();
    }

    @Override
    public void metricInstanceRemoved() {
        super.metricInstanceRemoved();
        meterImpl().histogram().metricInstanceRemoved();
    }
}