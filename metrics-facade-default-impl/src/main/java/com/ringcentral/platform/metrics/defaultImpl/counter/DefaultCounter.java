package com.ringcentral.platform.metrics.defaultImpl.counter;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.AbstractCounter;
import com.ringcentral.platform.metrics.counter.configs.*;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.LongAdder;

public class DefaultCounter extends AbstractCounter<LongAdder> {

    public static class MeasurableValuesImpl extends AbstractMeasurableValues {

        private final LongAdder counter;
        private final Map<Measurable, MeasurableValueProvider<LongAdder>> measurableValueProviders;

        protected MeasurableValuesImpl(
            LongAdder counter,
            Map<Measurable, MeasurableValueProvider<LongAdder>> measurableValueProviders) {

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
        LongAdder,
        CounterInstanceConfig,
        CounterSliceConfig,
        CounterConfig> {

        public static final MeasurableValueProvidersProviderImpl INSTANCE = new MeasurableValueProvidersProviderImpl();
        private static final Map<Measurable, MeasurableValueProvider<LongAdder>> MEASURABLE_VALUE_PROVIDERS = Map.of(COUNT, LongAdder::sum);

        @Override
        public Map<Measurable, MeasurableValueProvider<LongAdder>> valueProvidersFor(
            CounterInstanceConfig instanceConfig,
            CounterSliceConfig sliceConfig,
            CounterConfig config,
            Set<? extends Measurable> measurables) {

            return MEASURABLE_VALUE_PROVIDERS;
        }
    }

    public static final class MeterImplMakerImpl implements MeterImplMaker<
        LongAdder,
        CounterInstanceConfig,
        CounterSliceConfig,
        CounterConfig> {

        public static final MeterImplMakerImpl INSTANCE = new MeterImplMakerImpl();

        @Override
        public LongAdder makeMeterImpl(
            CounterInstanceConfig instanceConfig,
            CounterSliceConfig sliceConfig,
            CounterConfig config,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return
                instanceConfig != null ?
                instanceConfig.context().get(LongAdder.class, new LongAdder()) :
                new LongAdder();
        }
    }

    public static class InstanceMakerImpl implements InstanceMaker<LongAdder> {

        public static final InstanceMakerImpl INSTANCE = new InstanceMakerImpl();

        @Override
        public AbstractMeterInstance<LongAdder> makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<LongAdder>> measurableValueProviders,
            LongAdder counter) {

            MeasurableValues measurableValues = new MeasurableValuesImpl(counter, measurableValueProviders);

            return new DefaultCounterInstance(
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
        public AbstractExpirableMeterInstance<LongAdder> makeExpirableInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<LongAdder>> measurableValueProviders,
            LongAdder counter,
            long creationTimeMs) {

            MeasurableValues measurableValues = new MeasurableValuesImpl(counter, measurableValueProviders);

            return new DefaultExpirableCounterInstance(
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

    public DefaultCounter(
        MetricName name,
        CounterConfig config,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor,
        MetricRegistry registry) {

        super(
            name,
            config,
            MeasurableValueProvidersProviderImpl.INSTANCE,
            MeterImplMakerImpl.INSTANCE,
            LongAdder::add,
            InstanceMakerImpl.INSTANCE,
            timeMsProvider,
            executor,
            registry);
    }
}
