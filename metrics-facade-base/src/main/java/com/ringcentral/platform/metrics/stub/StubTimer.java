package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.timer.AbstractTimer;
import com.ringcentral.platform.metrics.timer.Stopwatch;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.timer.configs.TimerConfig;
import com.ringcentral.platform.metrics.timer.configs.TimerInstanceConfig;
import com.ringcentral.platform.metrics.timer.configs.TimerSliceConfig;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.Collections.emptyMap;

public class StubTimer extends AbstractTimer<Object> implements Timer {

    static final MeasurableValueProvidersProvider<Object> stubMeasurableValueProvidersProviderInstance = measures -> emptyMap();
    static final MeterImplUpdater<Object> stubMeterImplUpdaterInstance = (meterImpl, value) -> {};
    static final Object stubTimerImplInstance = new Object();

    static final MeterImplMaker<Object, TimerInstanceConfig, TimerSliceConfig, TimerConfig> stubTimerImplMaker =
        (instanceConfig, sliceConfig, config) -> stubTimerImplInstance;

    public StubTimer(
        MetricName name,
        TimerConfig config,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            stubMeasurableValueProvidersProviderInstance,
            stubTimerImplMaker,
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

    @Override
    public Stopwatch stopwatch(MetricDimensionValues dimensionValues) {
        return new StubStopWatch();
    }
}