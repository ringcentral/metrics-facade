package com.ringcentral.platform.metrics.dropwizard.timer;

import com.codahale.metrics.Timer;
import com.ringcentral.platform.metrics.AbstractMeter.AbstractExpirableMeterInstance;
import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValueProvider;
import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValuesProvider;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.timer.TimerInstance;

import java.util.List;
import java.util.Map;

public class DropwizardExpirableTimerInstance extends AbstractExpirableMeterInstance<Timer> implements TimerInstance {

    protected DropwizardExpirableTimerInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean levelInstance,
        MeasurableValuesProvider measurableValuesProvider,
        Map<Measurable, MeasurableValueProvider<Timer>> measurableValueProviders,
        Timer timer,
        long creationTimeMs) {

        super(
            name,
            dimensionValues,
            totalInstance,
            dimensionalTotalInstance,
            levelInstance,
            measurableValuesProvider,
            measurableValueProviders,
            timer,
            creationTimeMs);
    }
}