package com.ringcentral.platform.metrics.counter;

import com.ringcentral.platform.metrics.AbstractMeter;
import com.ringcentral.platform.metrics.counter.configs.*;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import java.util.concurrent.ScheduledExecutorService;

public abstract class AbstractCounter<MI> extends AbstractMeter<
    MI,
    CounterInstanceConfig,
    CounterSliceConfig,
    CounterConfig> implements Counter {

    protected AbstractCounter(
        MetricName name,
        CounterConfig config,
        MeasurableValueProvidersProvider<MI, CounterInstanceConfig, CounterSliceConfig, CounterConfig> measurableValueProvidersProvider,
        MeterImplMaker<MI, CounterInstanceConfig, CounterSliceConfig, CounterConfig> meterImplMaker,
        MeterImplUpdater<MI> meterImplUpdater,
        InstanceMaker<MI> instanceMaker,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            measurableValueProvidersProvider,
            meterImplMaker,
            meterImplUpdater,
            instanceMaker,
            timeMsProvider,
            executor);
    }

    @Override
    public void inc(long count, MetricDimensionValues dimensionValues) {
        update(count, dimensionValues);
    }

    @Override
    public void dec(long count, MetricDimensionValues dimensionValues) {
        update(-count, dimensionValues);
    }
}
