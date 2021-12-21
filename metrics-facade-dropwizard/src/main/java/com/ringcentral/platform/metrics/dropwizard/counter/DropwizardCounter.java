package com.ringcentral.platform.metrics.dropwizard.counter;

import com.codahale.metrics.Counter;
import com.ringcentral.platform.metrics.NotMeasuredException;
import com.ringcentral.platform.metrics.counter.AbstractCounter;
import com.ringcentral.platform.metrics.counter.configs.*;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

public class DropwizardCounter extends AbstractCounter<Counter> {

    public static class MeasurableValuesImpl extends AbstractMeasurableValues {

        private final Counter counter;
        private final Map<Measurable, MeasurableValueProvider<Counter>> measurableValueProviders;

        protected MeasurableValuesImpl(
            Counter counter,
            Map<Measurable, MeasurableValueProvider<Counter>> measurableValueProviders) {

            super(measurableValueProviders.keySet());
            this.counter = counter;
            this.measurableValueProviders = measurableValueProviders;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> V valueOfImpl(Measurable measurable) throws NotMeasuredException {
            return (V)measurableValueProviders.get(measurable).valueFor(counter);
        }
    }

    public static class MeasurableValueProvidersProviderImpl implements MeasurableValueProvidersProvider<
        Counter,
        CounterInstanceConfig,
        CounterSliceConfig,
        CounterConfig> {

        public static final MeasurableValueProvidersProviderImpl INSTANCE = new MeasurableValueProvidersProviderImpl();
        private static final Map<Measurable, MeasurableValueProvider<Counter>> MEASURABLE_VALUE_PROVIDERS = Map.of(COUNT, Counter::getCount);

        @Override
        public Map<Measurable, MeasurableValueProvider<Counter>> valueProvidersFor(
            CounterInstanceConfig instanceConfig,
            CounterSliceConfig sliceConfig,
            CounterConfig config,
            Set<? extends Measurable> measurables) {

            return MEASURABLE_VALUE_PROVIDERS;
        }
    }

    public static final class MeterImplMakerImpl implements MeterImplMaker<
        Counter,
        CounterInstanceConfig,
        CounterSliceConfig,
        CounterConfig> {

        public static final MeterImplMakerImpl INSTANCE = new MeterImplMakerImpl();

        @Override
        public Counter makeMeterImpl(
            CounterInstanceConfig instanceConfig,
            CounterSliceConfig sliceConfig,
            CounterConfig config,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor) {

            return
                instanceConfig != null ?
                instanceConfig.context().get(Counter.class, new Counter()) :
                new Counter();
        }
    }

    public static class InstanceMakerImpl implements InstanceMaker<Counter> {

        public static final InstanceMakerImpl INSTANCE = new InstanceMakerImpl();

        @Override
        public AbstractMeterInstance<Counter> makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<Counter>> measurableValueProviders,
            Counter counter) {

            MeasurableValues measurableValues = new MeasurableValuesImpl(counter, measurableValueProviders);

            return new DropwizardCounterInstance(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
                levelInstance,
                () -> measurableValues,
                measurableValueProviders,
                counter);
        }

        @Override
        public AbstractExpirableMeterInstance<Counter> makeExpirableInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<Counter>> measurableValueProviders,
            Counter counter,
            long creationTimeMs) {

            MeasurableValues measurableValues = new MeasurableValuesImpl(counter, measurableValueProviders);

            return new DropwizardExpirableCounterInstance(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
                levelInstance,
                () -> measurableValues,
                measurableValueProviders,
                counter,
                creationTimeMs);
        }
    }

    public DropwizardCounter(
        MetricName name,
        CounterConfig config,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            MeasurableValueProvidersProviderImpl.INSTANCE,
            MeterImplMakerImpl.INSTANCE,
            Counter::inc,
            InstanceMakerImpl.INSTANCE,
            timeMsProvider,
            executor);
    }
}
