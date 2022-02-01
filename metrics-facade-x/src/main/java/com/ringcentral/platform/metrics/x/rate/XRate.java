package com.ringcentral.platform.metrics.x.rate;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.AbstractRate;
import com.ringcentral.platform.metrics.rate.configs.*;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import com.ringcentral.platform.metrics.x.XMetricRegistry;
import com.ringcentral.platform.metrics.x.rate.configs.*;
import com.ringcentral.platform.metrics.x.rate.ema.ExpMovingAverageXRateImpl;
import com.ringcentral.platform.metrics.x.rate.ema.configs.ExpMovingAverageXRateImplConfig;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.utils.MetricContextUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

public class XRate extends AbstractRate<XRateImpl> {

    public static class MeasurableValuesImpl extends AbstractMeasurableValues {

        private final XRateImpl rate;
        private final Map<Measurable, MeasurableValueProvider<XRateImpl>> measurableValueProviders;

        protected MeasurableValuesImpl(
            XRateImpl rate,
            Map<Measurable, MeasurableValueProvider<XRateImpl>> measurableValueProviders) {

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
        XRateImpl,
        RateInstanceConfig,
        RateSliceConfig,
        RateConfig> {

        public static final MeasurableValueProvidersProviderImpl INSTANCE = new MeasurableValueProvidersProviderImpl();

        public static final MeasurableValueProvider<XRateImpl> COUNT_VALUE_PROVIDER = XRateImpl::count;
        public static final MeasurableValueProvider<XRateImpl> MEAN_RATE_VALUE_PROVIDER = XRateImpl::meanRate;
        public static final MeasurableValueProvider<XRateImpl> ONE_MINUTE_RATE_VALUE_PROVIDER = XRateImpl::oneMinuteRate;
        public static final MeasurableValueProvider<XRateImpl> FIVE_MINUTES_RATE_VALUE_PROVIDER = XRateImpl::fiveMinutesRate;
        public static final MeasurableValueProvider<XRateImpl> FIFTEEN_MINUTES_RATE_VALUE_PROVIDER = XRateImpl::fifteenMinutesRate;
        public static final MeasurableValueProvider<XRateImpl> RATE_UNIT_VALUE_PROVIDER = m -> "events/seconds";
        private static final Map<Measurable, MeasurableValueProvider<XRateImpl>> DEFAULT_MEASURABLE_VALUE_PROVIDERS;

        private static final Logger logger = getLogger(MeasurableValueProvidersProviderImpl.class);

        static {
            DEFAULT_MEASURABLE_VALUE_PROVIDERS = makeDefaultMeasurableValueProviders();
        }

        private static Map<Measurable, MeasurableValueProvider<XRateImpl>> makeDefaultMeasurableValueProviders() {
            Map<Measurable, MeasurableValueProvider<XRateImpl>> result = new LinkedHashMap<>();

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
        public Map<Measurable, MeasurableValueProvider<XRateImpl>> valueProvidersFor(
            RateInstanceConfig instanceConfig,
            RateSliceConfig sliceConfig,
            RateConfig config,
            Set<? extends Measurable> measurables) {

            if (measurables == null || measurables.isEmpty()) {
                return DEFAULT_MEASURABLE_VALUE_PROVIDERS;
            }

            Map<Measurable, MeasurableValueProvider<XRateImpl>> result = new LinkedHashMap<>();

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
        XRateImpl,
        RateInstanceConfig,
        RateSliceConfig,
        RateConfig> {

        public static final MeterImplMakerImpl INSTANCE = new MeterImplMakerImpl();

        @Override
        public XRateImpl makeMeterImpl(
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

        public XRateImpl makeMeterImpl(
            MetricContext instanceContext,
            MetricContext sliceContext,
            MetricContext context,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            XRateImplConfig implConfig = implConfig(instanceContext, sliceContext, context);
            XRateImpl impl;

            if (implConfig instanceof ExpMovingAverageXRateImplConfig) {
                impl = new ExpMovingAverageXRateImpl((ExpMovingAverageXRateImplConfig)implConfig, measurables);
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
                    "Unsupported " + XRateImplConfig.class.getSimpleName()
                    + ": " + implConfig.getClass().getName());
            }

            return impl;
        }

        private XRateImplConfig implConfig(
            MetricContext instanceContext,
            MetricContext sliceContext,
            MetricContext context) {

            if (has(XRateImplConfig.class, instanceContext, sliceContext, context)) {
                return getForClass(XRateImplConfig.class, instanceContext, sliceContext, context);
            }

            if (has(XRateImplConfigBuilder.class, instanceContext, sliceContext, context)) {
                return getForClass(XRateImplConfigBuilder.class, instanceContext, sliceContext, context).build();
            }

            return ExpMovingAverageXRateImplConfig.DEFAULT;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private XRateImpl makeCustomImpl(
            XRateImplConfig config,
            MetricContext instanceContext,
            MetricContext sliceContext,
            MetricContext context,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            CustomXRateImplMaker customImplMaker = ((XMetricRegistry)registry).customXRateImplMakerFor(config.getClass());

            if (customImplMaker == null) {
                return null;
            }

            return customImplMaker.makeXRateImpl(
                config,
                instanceContext,
                sliceContext,
                context,
                measurables,
                executor,
                registry);
        }
    }

    public static class InstanceMakerImpl implements InstanceMaker<XRateImpl> {

        public static final InstanceMakerImpl INSTANCE = new InstanceMakerImpl();

        @Override
        public AbstractMeterInstance<XRateImpl> makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<XRateImpl>> measurableValueProviders,
            XRateImpl rate) {

            MeasurableValues measurableValues = new MeasurableValuesImpl(rate, measurableValueProviders);

            return new XRateInstance(
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
        public AbstractExpirableMeterInstance<XRateImpl> makeExpirableInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<XRateImpl>> measurableValueProviders,
            XRateImpl rate,
            long creationTimeMs) {

            MeasurableValues measurableValues = new MeasurableValuesImpl(rate, measurableValueProviders);

            return new XExpirableRateInstance(
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

    public XRate(
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
            XRateImpl::mark,
            InstanceMakerImpl.INSTANCE,
            timeMsProvider,
            executor,
            registry);
    }
}
