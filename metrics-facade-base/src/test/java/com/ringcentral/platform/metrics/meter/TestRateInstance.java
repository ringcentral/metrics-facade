package com.ringcentral.platform.metrics.meter;

import com.ringcentral.platform.metrics.AbstractMeter.MeasurableValueProvider;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateInstance;

import java.util.*;

public class TestRateInstance extends TestMeterInstance<TestRateImpl> implements RateInstance {

    public TestRateInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean levelInstance,
        Map<Measurable, MeasurableValueProvider<TestRateImpl>> measurableValueProviders,
        TestRateImpl rateImpl) {

        super(
            name,
            dimensionValues,
            totalInstance,
            dimensionalTotalInstance,
            levelInstance,
            measurableValueProviders,
            rateImpl);
    }
}
