package com.ringcentral.platform.metrics.defaultImpl.rate;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.rate.configs.*;
import com.ringcentral.platform.metrics.defaultImpl.rate.ema.ExpMovingAverageRateImpl;
import com.ringcentral.platform.metrics.defaultImpl.rate.ema.configs.ExpMovingAverageRateImplConfig;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.AbstractRate;
import com.ringcentral.platform.metrics.rate.configs.*;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.utils.MetricContextUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

public class DefaultRate extends AbstractRate<RateImpl> {

    public static class MeasurableValuesImpl extends AbstractMeasurableValues {

        private final RateImpl rate;
        private final Map<Measurable, MeasurableValueProvider<RateImpl>> measurableValueProviders;

        protected MeasurableValuesImpl(
            RateImpl rate,
            Map<Measurable, MeasurableValueProvider<RateImpl>> measurableValueProviders) {

            super(measurableValueProviders.keySet());
            this.rate = rate;
            this.measurableValueProviders = measurableValueProviders;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> V valueOfImpl(Measurable measurable) throws NotMeasuredException {
            return (V)measurableValueProviders.get(measurable).valueFor(rate);
        }
    }

    public static class MeasurableValueProvidersProviderImpl implements MeasurableValueProvidersProvider<
        RateImpl,
        RateInstanceConfig,
        RateSliceConfig,
        RateConfig> {

        public static final MeasurableValueProvidersProviderImpl INSTANCE = new MeasurableValueProvidersProviderImpl();

        public static final MeasurableValueProvider<RateImpl> COUNT_VALUE_PROVIDER = RateImpl::count;
        public static final MeasurableValueProvider<RateImpl> MEAN_RATE_VALUE_PROVIDER = RateImpl::meanRate;
        public static final MeasurableValueProvider<RateImpl> ONE_MINUTE_RATE_VALUE_PROVIDER = RateImpl::oneMinuteRate;
        public static final MeasurableValueProvider<RateImpl> FIVE_MINUTES_RATE_VALUE_PROVIDER = RateImpl::fiveMinutesRate;
        public static final MeasurableValueProvider<RateImpl> FIFTEEN_MINUTES_RATE_VALUE_PROVIDER = RateImpl::fifteenMinutesRate;
        public static final MeasurableValueProvider<RateImpl> RATE_UNIT_VALUE_PROVIDER = m -> "events/seconds";
        private static final Map<Measurable, MeasurableValueProvider<RateImpl>> DEFAULT_MEASURABLE_VALUE_PROVIDERS;

        private static final Logger logger = getLogger(MeasurableValueProvidersProviderImpl.class);

        static {
            DEFAULT_MEASURABLE_VALUE_PROVIDERS = makeDefaultMeasurableValueProviders();
        }

        private static Map<Measurable, MeasurableValueProvider<RateImpl>> makeDefaultMeasurableValueProviders() {
            Map<Measurable, MeasurableValueProvider<RateImpl>> result = new LinkedHashMap<>();

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
        public Map<Measurable, MeasurableValueProvider<RateImpl>> valueProvidersFor(
            RateInstanceConfig instanceConfig,
            RateSliceConfig sliceConfig,
            RateConfig config,
            Set<? extends Measurable> measurables) {

            if (measurables == null || measurables.isEmpty()) {
                return DEFAULT_MEASURABLE_VALUE_PROVIDERS;
            }

            Map<Measurable, MeasurableValueProvider<RateImpl>> result = new LinkedHashMap<>();

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
        RateImpl,
        RateInstanceConfig,
        RateSliceConfig,
        RateConfig> {

        public static final MeterImplMakerImpl INSTANCE = new MeterImplMakerImpl();

        @Override
        public RateImpl makeMeterImpl(
            RateInstanceConfig instanceConfig,
            RateSliceConfig sliceConfig,
            RateConfig config,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return makeMeterImpl(
                instanceConfig != null ? instanceConfig.context() : null,
                sliceConfig != null ? sliceConfig.context() : null,
                config != null ? config.context() : null,
                measurables,
                executor,
                registry);
        }

        public RateImpl makeMeterImpl(
            MetricContext instanceContext,
            MetricContext sliceContext,
            MetricContext context,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            RateImplConfig implConfig = implConfig(instanceContext, sliceContext, context);
            RateImpl impl;

            if (implConfig instanceof ExpMovingAverageRateImplConfig) {
                impl = new ExpMovingAverageRateImpl((ExpMovingAverageRateImplConfig)implConfig, measurables);
            } else {
                impl = makeCustomImpl(
                    implConfig,
                    instanceContext,
                    sliceContext,
                    context,
                    measurables,
                    executor,
                    registry);
            }

            if (impl == null) {
                throw new IllegalArgumentException(
                    "Unsupported " + RateImplConfig.class.getSimpleName()
                    + ": " + implConfig.getClass().getName());
            }

            return impl;
        }

        private RateImplConfig implConfig(
            MetricContext instanceContext,
            MetricContext sliceContext,
            MetricContext context) {

            if (has(RateImplConfig.class, instanceContext, sliceContext, context)) {
                return getForClass(RateImplConfig.class, instanceContext, sliceContext, context);
            }

            if (has(RateImplConfigBuilder.class, instanceContext, sliceContext, context)) {
                return getForClass(RateImplConfigBuilder.class, instanceContext, sliceContext, context).build();
            }

            return ExpMovingAverageRateImplConfig.DEFAULT;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private RateImpl makeCustomImpl(
            RateImplConfig config,
            MetricContext instanceContext,
            MetricContext sliceContext,
            MetricContext context,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            CustomRateImplSpec<? extends RateImplConfig> customImplSpec =
                ((DefaultMetricRegistry) registry).customRateImplMakerFor(config.getClass());

            if (customImplSpec == null) {
                return null;
            }

            CustomRateImplMaker customImplMaker = (CustomRateImplMaker)customImplSpec;

            return customImplMaker.makeRateImpl(
                config,
                instanceContext,
                sliceContext,
                context,
                measurables,
                executor,
                registry);
        }
    }

    public static class InstanceMakerImpl implements InstanceMaker<RateImpl> {

        public static final InstanceMakerImpl INSTANCE = new InstanceMakerImpl();

        @Override
        public AbstractMeterInstance<RateImpl> makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<RateImpl>> measurableValueProviders,
            RateImpl rate) {

            MeasurableValues measurableValues = new MeasurableValuesImpl(rate, measurableValueProviders);

            return new DefaultRateInstance(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
                levelInstance,
                () -> measurableValues,
                measurableValueProviders,
                rate);
        }

        @Override
        public AbstractExpirableMeterInstance<RateImpl> makeExpirableInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<RateImpl>> measurableValueProviders,
            RateImpl rate,
            long creationTimeMs) {

            MeasurableValues measurableValues = new MeasurableValuesImpl(rate, measurableValueProviders);

            return new DefaultExpirableRateInstance(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
                levelInstance,
                () -> measurableValues,
                measurableValueProviders,
                rate,
                creationTimeMs);
        }
    }

    public DefaultRate(
        MetricName name,
        RateConfig config,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor,
        MetricRegistry registry) {

        super(
            name,
            config,
            MeasurableValueProvidersProviderImpl.INSTANCE,
            MeterImplMakerImpl.INSTANCE,
            RateImpl::mark,
            InstanceMakerImpl.INSTANCE,
            timeMsProvider,
            executor,
            registry);
    }
}
