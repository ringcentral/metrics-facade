package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.counter.*;
import com.ringcentral.platform.metrics.counter.configs.*;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.Collections.emptyMap;

public class StubCounter extends AbstractCounter<Object> implements Counter {

    static final MeasurableValueProvidersProvider<Object, CounterInstanceConfig, CounterSliceConfig, CounterConfig> stubMeasurableValueProvidersProviderInstance =
        (ic, sc, c, m) -> emptyMap();

    static final MeterImplUpdater<Object> stubMeterImplUpdaterInstance = (meterImpl, value) -> {};
    static final Object stubCounterImplInstance = new Object();

    static final MeterImplMaker<Object, CounterInstanceConfig, CounterSliceConfig, CounterConfig> stubCounterImplMaker =
        (ic, sc, c, m, e, r) -> stubCounterImplInstance;

    public StubCounter(
        MetricName name,
        CounterConfig config,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor,
        MetricRegistry registry) {

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
                    List<LabelValue> labelValues,
                    boolean totalInstance,
                    boolean labeledMetricTotalInstance,
                    boolean levelInstance,
                    Map<Measurable, MeasurableValueProvider<Object>> measurableValueProviders,
                    Object meterImpl) {

                    return new StubMeterInstance(
                        name,
                        labelValues,
                        totalInstance,
                        labeledMetricTotalInstance,
                        levelInstance,
                        measurableValueProviders,
                        meterImpl);
                }

                @Override
                public AbstractExpirableMeterInstance<Object> makeExpirableInstance(
                    MetricName name,
                    List<LabelValue> labelValues,
                    boolean totalInstance,
                    boolean labeledMetricTotalInstance,
                    boolean levelInstance,
                    Map<Measurable, MeasurableValueProvider<Object>> measurableValueProviders,
                    Object meterImpl,
                    long creationTimeMs) {

                    return new StubExpirableMeterInstance(
                        name,
                        labelValues,
                        totalInstance,
                        labeledMetricTotalInstance,
                        levelInstance,
                        measurableValueProviders,
                        meterImpl,
                        creationTimeMs);
                }
            },
            timeMsProvider,
            executor,
            registry);
    }
}
