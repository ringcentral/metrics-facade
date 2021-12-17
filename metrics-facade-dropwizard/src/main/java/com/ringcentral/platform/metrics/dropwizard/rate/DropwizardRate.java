package com.ringcentral.platform.metrics.dropwizard.rate;

import com.codahale.metrics.*;
import com.ringcentral.platform.metrics.NotMeasuredException;
import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.AbstractRate;
import com.ringcentral.platform.metrics.rate.configs.*;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static org.slf4j.LoggerFactory.getLogger;

public class DropwizardRate extends AbstractRate<Meter> {

    public static class MeasurableValuesImpl extends AbstractMeasurableValues {

        private final Meter meter;
        private final Map<Measurable, MeasurableValueProvider<Meter>> measurableValueProviders;

        protected MeasurableValuesImpl(
            Meter meter,
            Map<Measurable, MeasurableValueProvider<Meter>> measurableValueProviders) {

            super(measurableValueProviders.keySet());
            this.meter = meter;
            this.measurableValueProviders = measurableValueProviders;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> V valueOfImpl(Measurable measurable) throws NotMeasuredException {
            return (V)measurableValueProviders.get(measurable).valueFor(meter);
        }
    }

    public static class MeasurableValueProvidersProviderImpl implements MeasurableValueProvidersProvider<
        Meter,
        RateInstanceConfig,
        RateSliceConfig,
        RateConfig> {

        public static final MeasurableValueProvidersProviderImpl INSTANCE = new MeasurableValueProvidersProviderImpl();

        public static final MeasurableValueProvider<Meter> COUNT_VALUE_PROVIDER = Meter::getCount;
        public static final MeasurableValueProvider<Meter> MEAN_RATE_VALUE_PROVIDER = Meter::getMeanRate;
        public static final MeasurableValueProvider<Meter> ONE_MINUTE_RATE_VALUE_PROVIDER = Meter::getOneMinuteRate;
        public static final MeasurableValueProvider<Meter> FIVE_MINUTES_RATE_VALUE_PROVIDER = Meter::getFiveMinuteRate;
        public static final MeasurableValueProvider<Meter> FIFTEEN_MINUTES_RATE_VALUE_PROVIDER = Meter::getFifteenMinuteRate;
        public static final MeasurableValueProvider<Meter> RATE_UNIT_VALUE_PROVIDER = m -> "events/seconds";
        private static final Map<Measurable, MeasurableValueProvider<Meter>> DEFAULT_MEASURABLE_VALUE_PROVIDERS;

        private static final Logger logger = getLogger(MeasurableValueProvidersProviderImpl.class);

        static {
            DEFAULT_MEASURABLE_VALUE_PROVIDERS = makeDefaultMeasurableValueProviders();
        }

        private static Map<Measurable, MeasurableValueProvider<Meter>> makeDefaultMeasurableValueProviders() {
            Map<Measurable, MeasurableValueProvider<Meter>> result = new HashMap<>();

            DEFAULT_RATE_MEASURABLES.forEach(m -> {
                if (m instanceof Count) {
                    result.put(m, COUNT_VALUE_PROVIDER);
                } else if (m instanceof MeanRate) {
                    result.put(m, MEAN_RATE_VALUE_PROVIDER);
                } else if (m instanceof OneMinuteRate) {
                    result.put(m, ONE_MINUTE_RATE_VALUE_PROVIDER);
                } else if (m instanceof FiveMinutesRate) {
                    result.put(m, FIVE_MINUTES_RATE_VALUE_PROVIDER);
                } else if (m instanceof FifteenMinutesRate) {
                    result.put(m, FIFTEEN_MINUTES_RATE_VALUE_PROVIDER);
                } else if (m instanceof RateUnit) {
                    result.put(m, RATE_UNIT_VALUE_PROVIDER);
                }
            });

            return Map.copyOf(result);
        }

        @Override
        public Map<Measurable, MeasurableValueProvider<Meter>> valueProvidersFor(
            RateInstanceConfig instanceConfig,
            RateSliceConfig sliceConfig,
            RateConfig config,
            Set<? extends Measurable> measurables) {

            if (measurables == null || measurables.isEmpty()) {
                return DEFAULT_MEASURABLE_VALUE_PROVIDERS;
            }

            Map<Measurable, MeasurableValueProvider<Meter>> result = new HashMap<>();

            measurables.forEach(m -> {
                if (m instanceof Count) {
                    result.put(m, COUNT_VALUE_PROVIDER);
                } else if (m instanceof MeanRate) {
                    result.put(m, MEAN_RATE_VALUE_PROVIDER);
                } else if (m instanceof OneMinuteRate) {
                    result.put(m, ONE_MINUTE_RATE_VALUE_PROVIDER);
                } else if (m instanceof FiveMinutesRate) {
                    result.put(m, FIVE_MINUTES_RATE_VALUE_PROVIDER);
                } else if (m instanceof FifteenMinutesRate) {
                    result.put(m, FIFTEEN_MINUTES_RATE_VALUE_PROVIDER);
                } else if (m instanceof RateUnit) {
                    result.put(m, RATE_UNIT_VALUE_PROVIDER);
                } else {
                    logger.warn("Unsupported measurable {}", m.getClass().getName());
                }
            });

            return !result.isEmpty() ? Map.copyOf(result) : DEFAULT_MEASURABLE_VALUE_PROVIDERS;
        }
    }

    public static final class MeterImplMakerImpl implements MeterImplMaker<
        Meter,
        RateInstanceConfig,
        RateSliceConfig,
        RateConfig> {

        public static final MeterImplMakerImpl INSTANCE = new MeterImplMakerImpl();

        @Override
        public Meter makeMeterImpl(
            RateInstanceConfig instanceConfig,
            RateSliceConfig sliceConfig,
            RateConfig config,
            Set<? extends Measurable> measurables) {

            if (instanceConfig != null && instanceConfig.context().has(Meter.class)) {
                return instanceConfig.context().get(Meter.class);
            }

            MovingAverages movingAverages = null;

            if (instanceConfig != null && instanceConfig.context().has(MovingAverages.class)) {
                movingAverages = instanceConfig.context().get(MovingAverages.class);
            } else if (sliceConfig != null && sliceConfig.context().has(MovingAverages.class)) {
                movingAverages = sliceConfig.context().get(MovingAverages.class);
            } else if (config != null && config.context().has(MovingAverages.class)) {
                movingAverages = config.context().get(MovingAverages.class);
            }

            return movingAverages != null ? new Meter(movingAverages) : new Meter();
        }
    }

    public static class InstanceMakerImpl implements InstanceMaker<Meter> {

        public static final InstanceMakerImpl INSTANCE = new InstanceMakerImpl();

        @Override
        public AbstractMeterInstance<Meter> makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<Meter>> measurableValueProviders,
            Meter meter) {

            MeasurableValues measurableValues = new MeasurableValuesImpl(meter, measurableValueProviders);

            return new DropwizardRateInstance(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
                levelInstance,
                () -> measurableValues,
                measurableValueProviders,
                meter);
        }

        @Override
        public AbstractExpirableMeterInstance<Meter> makeExpirableInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<Meter>> measurableValueProviders,
            Meter meter,
            long creationTimeMs) {

            MeasurableValues measurableValues = new MeasurableValuesImpl(meter, measurableValueProviders);

            return new DropwizardExpirableRateInstance(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
                levelInstance,
                () -> measurableValues,
                measurableValueProviders,
                meter,
                creationTimeMs);
        }
    }

    public DropwizardRate(
        MetricName name,
        RateConfig config,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            MeasurableValueProvidersProviderImpl.INSTANCE,
            MeterImplMakerImpl.INSTANCE,
            Meter::mark,
            InstanceMakerImpl.INSTANCE,
            timeMsProvider,
            executor);
    }
}
