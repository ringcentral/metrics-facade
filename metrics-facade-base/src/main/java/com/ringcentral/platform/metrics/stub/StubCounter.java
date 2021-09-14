package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.counter.AbstractCounter;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.counter.configs.CounterConfig;
import com.ringcentral.platform.metrics.counter.configs.CounterInstanceConfig;
import com.ringcentral.platform.metrics.counter.configs.CounterSliceConfig;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.Collections.emptyMap;

public class StubCounter extends AbstractCounter<Object> implements Counter {

    static final MeasurableValueProvidersProvider<Object> stubMeasurableValueProvidersProviderInstance = measures -> emptyMap();
    static final MeterImplUpdater<Object> stubMeterImplUpdaterInstance = (meterImpl, value) -> {};
    static final Object stubCounterImplInstance = new Object();

    static final MeterImplMaker<Object, CounterInstanceConfig, CounterSliceConfig, CounterConfig> stubCounterImplMaker =
        (instanceConfig, sliceConfig, config) -> stubCounterImplInstance;

    public StubCounter(
        MetricName name,
        CounterConfig config,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            stubMeasurableValueProvidersProviderInstance,
            stubCounterImplMaker,
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
