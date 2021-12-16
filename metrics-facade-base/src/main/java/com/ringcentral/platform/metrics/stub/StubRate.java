package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.*;
import com.ringcentral.platform.metrics.rate.configs.*;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.Collections.emptyMap;

public class StubRate extends AbstractRate<Object> implements Rate {

    static final MeasurableValueProvidersProvider<Object> stubMeasurableValueProvidersProviderInstance = measures -> emptyMap();
    static final MeterImplUpdater<Object> stubMeterImplUpdaterInstance = (meterImpl, value) -> {};
    static final Object stubRateImplInstance = new Object();

    static final MeterImplMaker<Object, RateInstanceConfig, RateSliceConfig, RateConfig> stubRateImplMaker =
        (instanceConfig, sliceConfig, config, measurables) -> stubRateImplInstance;

    public StubRate(
        MetricName name,
        RateConfig config,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            stubMeasurableValueProvidersProviderInstance,
            stubRateImplMaker,
            stubMeterImplUpdaterInstance,
            new InstanceMaker<>() {

                @Override
                public AbstractMeterInstance<Object> makeInstance(
                    MetricName name,
                    List<MetricDimensionValue> dimensionValues,
                    boolean totalInstance,
                    boolean dimensionalTotalInstance,
                    boolean levelInstance,
                    Map<Measurable, MeasurableValueProvider<Object>> measurableValueProviders,
                    Object meterImpl) {

                    return new StubMeterInstance(
                        name,
                        dimensionValues,
                        totalInstance,
                        dimensionalTotalInstance,
                        levelInstance,
                        measurableValueProviders,
                        meterImpl);
                }

                @Override
                public AbstractExpirableMeterInstance<Object> makeExpirableInstance(
                    MetricName name,
                    List<MetricDimensionValue> dimensionValues,
                    boolean totalInstance,
                    boolean dimensionalTotalInstance,
                    boolean levelInstance,
                    Map<Measurable, MeasurableValueProvider<Object>> measurableValueProviders,
                    Object meterImpl,
                    long creationTimeMs) {

                    return new StubExpirableMeterInstance(
                        name,
                        dimensionValues,
                        totalInstance,
                        dimensionalTotalInstance,
                        levelInstance,
                        measurableValueProviders,
                        meterImpl,
                        creationTimeMs);
                }
            },
            timeMsProvider,
            executor);
    }
}
