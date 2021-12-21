package com.ringcentral.platform.metrics.dropwizard.histogram;

import com.codahale.metrics.*;
import com.ringcentral.platform.metrics.NotMeasuredException;
import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.histogram.AbstractHistogram;
import com.ringcentral.platform.metrics.histogram.configs.*;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static org.slf4j.LoggerFactory.getLogger;

public class DropwizardHistogram extends AbstractHistogram<Histogram> {

    protected interface MVP extends MeasurableValueProvider<Histogram> {

        @Override
        default Object valueFor(Histogram histogram) {
            return valueFor(histogram, histogram.getSnapshot());
        }

        Object valueFor(Histogram histogram, Snapshot snapshot);
    }

    public static class MeasurableValuesImpl extends AbstractMeasurableValues {

        private final Histogram histogram;
        private final Snapshot snapshot;
        private final Map<Measurable, MeasurableValueProvider<Histogram>> measurableValueProviders;

        public MeasurableValuesImpl(
            Histogram histogram,
            Map<Measurable, MeasurableValueProvider<Histogram>> measurableValueProviders) {

            super(measurableValueProviders.keySet());
            this.histogram = histogram;
            this.snapshot = histogram.getSnapshot();
            this.measurableValueProviders = measurableValueProviders;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> V valueOfImpl(Measurable measurable) throws NotMeasuredException {
            MVP valueProvider = (MVP)measurableValueProviders.get(measurable);
            return (V)valueProvider.valueFor(histogram, snapshot);
        }
    }

    public static class MeasurableValueProvidersProviderImpl implements MeasurableValueProvidersProvider<
        Histogram,
        HistogramInstanceConfig,
        HistogramSliceConfig,
        HistogramConfig> {

        public static final MeasurableValueProvidersProviderImpl INSTANCE = new MeasurableValueProvidersProviderImpl();

        public static final MVP COUNT_VALUE_PROVIDER = new MVP() {

            @Override
            public Object valueFor(Histogram histogram) {
                return histogram.getCount();
            }

            @Override
            public Object valueFor(Histogram histogram, Snapshot snapshot) {
                return histogram.getCount();
            }
        };

        public static final MVP MIN_VALUE_PROVIDER = (h, s) -> s.getMin();
        public static final MVP MAX_VALUE_PROVIDER = (h, s) -> s.getMax();
        public static final MVP MEAN_VALUE_PROVIDER = (h, s) -> s.getMean();
        public static final MVP STANDARD_DEVIATION_VALUE_PROVIDER = (h, s) -> s.getStdDev();

        public static class PercentileValueProvider implements MVP {

            final double quantile;

            public PercentileValueProvider(double quantile) {
                this.quantile = quantile;
            }

            @Override
            public Object valueFor(Histogram histogram, Snapshot snapshot) {
                return snapshot.getValue(quantile);
            }
        }

        private static final Map<Measurable, MeasurableValueProvider<Histogram>> DEFAULT_MEASURABLE_VALUE_PROVIDERS;
        private static final Logger logger = getLogger(MeasurableValueProvidersProviderImpl.class);

        static {
            DEFAULT_MEASURABLE_VALUE_PROVIDERS = makeDefaultMeasurableValueProviders();
        }

        private static Map<Measurable, MeasurableValueProvider<Histogram>> makeDefaultMeasurableValueProviders() {
            Map<Measurable, MeasurableValueProvider<Histogram>> result = new HashMap<>();

            DEFAULT_HISTOGRAM_MEASURABLES.forEach(m -> {
                if (m instanceof Count) {
                    result.put(m, COUNT_VALUE_PROVIDER);
                } else if (m instanceof Min) {
                    result.put(m, MIN_VALUE_PROVIDER);
                } else if (m instanceof Max) {
                    result.put(m, MAX_VALUE_PROVIDER);
                } else if (m instanceof Mean) {
                    result.put(m, MEAN_VALUE_PROVIDER);
                } else if (m instanceof StandardDeviation) {
                    result.put(m, STANDARD_DEVIATION_VALUE_PROVIDER);
                } else if (m instanceof Percentile) {
                    Percentile p = (Percentile)m;
                    result.put(m, new PercentileValueProvider(p.quantile()));
                }
            });

            return Map.copyOf(result);
        }

        @Override
        public Map<Measurable, MeasurableValueProvider<Histogram>> valueProvidersFor(
            HistogramInstanceConfig instanceConfig,
            HistogramSliceConfig sliceConfig,
            HistogramConfig config,
            Set<? extends Measurable> measurables) {

            if (measurables == null || measurables.isEmpty()) {
                return DEFAULT_MEASURABLE_VALUE_PROVIDERS;
            }

            Map<Measurable, MeasurableValueProvider<Histogram>> result = new HashMap<>();

            measurables.forEach(m -> {
                if (m instanceof Count) {
                    result.put(m, COUNT_VALUE_PROVIDER);
                } else if (m instanceof Min) {
                    result.put(m, MIN_VALUE_PROVIDER);
                } else if (m instanceof Max) {
                    result.put(m, MAX_VALUE_PROVIDER);
                } else if (m instanceof Mean) {
                    result.put(m, MEAN_VALUE_PROVIDER);
                } else if (m instanceof StandardDeviation) {
                    result.put(m, STANDARD_DEVIATION_VALUE_PROVIDER);
                } else if (m instanceof Percentile) {
                    Percentile p = (Percentile)m;
                    result.put(m, new PercentileValueProvider(p.quantile()));
                } else {
                    logger.warn("Unsupported measurable {}", m.getClass().getName());
                }
            });

            return !result.isEmpty() ? Map.copyOf(result) : DEFAULT_MEASURABLE_VALUE_PROVIDERS;
        }
    }

    public static final class MeterImplMakerImpl implements MeterImplMaker<
        Histogram,
        HistogramInstanceConfig,
        HistogramSliceConfig,
        HistogramConfig> {

        public static final MeterImplMakerImpl INSTANCE = new MeterImplMakerImpl();

        @Override
        @SuppressWarnings("DuplicatedCode")
        public Histogram makeMeterImpl(
            HistogramInstanceConfig instanceConfig,
            HistogramSliceConfig sliceConfig,
            HistogramConfig config,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor) {

            if (instanceConfig != null && instanceConfig.context().has(Histogram.class)) {
                return instanceConfig.context().get(Histogram.class);
            }

            Reservoir reservoir;

            if (instanceConfig != null && instanceConfig.context().has(Reservoir.class)) {
                reservoir = instanceConfig.context().get(Reservoir.class);
            } else if (sliceConfig != null && sliceConfig.context().has(Reservoir.class)) {
                reservoir = sliceConfig.context().get(Reservoir.class);
            } else if (config != null && config.context().has(Reservoir.class)) {
                reservoir = config.context().get(Reservoir.class);
            } else {
                reservoir = new ExponentiallyDecayingReservoir();
            }

            return new Histogram(reservoir);
        }
    }

    public static class InstanceMakerImpl implements InstanceMaker<Histogram> {

        public static final InstanceMakerImpl INSTANCE = new InstanceMakerImpl();

        @Override
        public AbstractMeterInstance<Histogram> makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<Histogram>> measurableValueProviders,
            Histogram histogram) {

            return new DropwizardHistogramInstance(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
                levelInstance,
                () -> new MeasurableValuesImpl(histogram, measurableValueProviders),
                measurableValueProviders,
                histogram);
        }

        @Override
        public AbstractExpirableMeterInstance<Histogram> makeExpirableInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<Histogram>> measurableValueProviders,
            Histogram histogram,
            long creationTimeMs) {

            return new DropwizardExpirableHistogramInstance(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
                levelInstance,
                () -> new MeasurableValuesImpl(histogram, measurableValueProviders),
                measurableValueProviders,
                histogram,
                creationTimeMs);
        }
    }

    public DropwizardHistogram(
        MetricName name,
        HistogramConfig config,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            MeasurableValueProvidersProviderImpl.INSTANCE,
            MeterImplMakerImpl.INSTANCE,
            Histogram::update,
            InstanceMakerImpl.INSTANCE,
            timeMsProvider,
            executor);
    }
}
