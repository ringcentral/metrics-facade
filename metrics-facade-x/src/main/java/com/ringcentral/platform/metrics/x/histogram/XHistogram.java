package com.ringcentral.platform.metrics.x.histogram;

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

public class XHistogram extends AbstractHistogram<XHistogramImpl> {

    protected interface MVP extends MeasurableValueProvider<XHistogramImpl> {

        @Override
        default Object valueFor(XHistogramImpl histogram) {
            return valueFor(histogram, null /* histogram.getSnapshot() */);
        }

        Object valueFor(XHistogramImpl histogram, Object snapshot /* Snapshot snapshot */);
    }

    public static class MeasurableValuesImpl extends AbstractMeasurableValues {

        private final XHistogramImpl histogram;
        // private final Snapshot snapshot;
        private final Map<Measurable, MeasurableValueProvider<XHistogramImpl>> measurableValueProviders;

        public MeasurableValuesImpl(
            XHistogramImpl histogram,
            Map<Measurable, MeasurableValueProvider<XHistogramImpl>> measurableValueProviders) {

            super(measurableValueProviders.keySet());
            this.histogram = histogram;
            // this.snapshot = histogram.getSnapshot();
            this.measurableValueProviders = measurableValueProviders;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> V valueOfImpl(Measurable measurable) throws NotMeasuredException {
            MVP valueProvider = (MVP)measurableValueProviders.get(measurable);
            return (V)valueProvider.valueFor(histogram, null /* snapshot */);
        }
    }

    public static class MeasurableValueProvidersProviderImpl implements MeasurableValueProvidersProvider<XHistogramImpl> {

        public static final MeasurableValueProvidersProviderImpl INSTANCE = new MeasurableValueProvidersProviderImpl();

        public static final MVP COUNT_VALUE_PROVIDER = new MVP() {

            @Override
            public Object valueFor(XHistogramImpl histogram) {
                return 1L; // histogram.getCount();
            }

            @Override
            public Object valueFor(XHistogramImpl histogram, Object snapshot /* Snapshot snapshot */) {
                return 1L; // histogram.getCount();
            }
        };

        public static final MVP MIN_VALUE_PROVIDER = (h, s) -> 1L; // s.getMin();
        public static final MVP MAX_VALUE_PROVIDER = (h, s) -> 1L; // s.getMax();
        public static final MVP MEAN_VALUE_PROVIDER = (h, s) -> 1L; // s.getMean();
        public static final MVP STANDARD_DEVIATION_VALUE_PROVIDER = (h, s) -> 1L; // s.getStdDev();

        public static class PercentileValueProvider implements MVP {

            final double quantile;

            public PercentileValueProvider(double quantile) {
                this.quantile = quantile;
            }

            @Override
            public Object valueFor(XHistogramImpl histogram, Object snapshot /* Snapshot snapshot */) {
                return 1L; // snapshot.getValue(quantile);
            }
        }

        private static final Map<Measurable, MeasurableValueProvider<XHistogramImpl>> DEFAULT_MEASURABLE_VALUE_PROVIDERS;
        private static final Logger logger = getLogger(MeasurableValueProvidersProviderImpl.class);

        static {
            DEFAULT_MEASURABLE_VALUE_PROVIDERS = makeDefaultMeasurableValueProviders();
        }

        private static Map<Measurable, MeasurableValueProvider<XHistogramImpl>> makeDefaultMeasurableValueProviders() {
            Map<Measurable, MeasurableValueProvider<XHistogramImpl>> result = new HashMap<>();

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
        public Map<Measurable, MeasurableValueProvider<XHistogramImpl>> valueProvidersFor(Set<? extends Measurable> measurables) {
            if (measurables == null || measurables.isEmpty()) {
                return DEFAULT_MEASURABLE_VALUE_PROVIDERS;
            }

            Map<Measurable, MeasurableValueProvider<XHistogramImpl>> result = new HashMap<>();

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
        XHistogramImpl,
        HistogramInstanceConfig,
        HistogramSliceConfig,
        HistogramConfig> {

        public static final MeterImplMakerImpl INSTANCE = new MeterImplMakerImpl();

        @Override
        @SuppressWarnings("DuplicatedCode")
        public XHistogramImpl makeMeterImpl(
            HistogramInstanceConfig instanceConfig,
            HistogramSliceConfig sliceConfig,
            HistogramConfig config,
            Set<? extends Measurable> measurables) {

//            if (instanceConfig != null && instanceConfig.context().has(XHistogramImpl.class)) {
//                return instanceConfig.context().get(XHistogramImpl.class);
//            }
//
//            Reservoir reservoir;
//
//            if (instanceConfig != null && instanceConfig.context().has(Reservoir.class)) {
//                reservoir = instanceConfig.context().get(Reservoir.class);
//            } else if (sliceConfig != null && sliceConfig.context().has(Reservoir.class)) {
//                reservoir = sliceConfig.context().get(Reservoir.class);
//            } else if (config != null && config.context().has(Reservoir.class)) {
//                reservoir = config.context().get(Reservoir.class);
//            } else {
//                reservoir = new ExponentiallyDecayingReservoir();
//            }
//
//            return new XHistogramImpl(reservoir);
            return null;
        }
    }

    public static class InstanceMakerImpl implements InstanceMaker<XHistogramImpl> {

        public static final InstanceMakerImpl INSTANCE = new InstanceMakerImpl();

        @Override
        public AbstractMeterInstance<XHistogramImpl> makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<XHistogramImpl>> measurableValueProviders,
            XHistogramImpl histogram) {

            return new XHistogramInstance(
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
        public AbstractExpirableMeterInstance<XHistogramImpl> makeExpirableInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<XHistogramImpl>> measurableValueProviders,
            XHistogramImpl histogram,
            long creationTimeMs) {

            return new XExpirableHistogramInstance(
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

    public XHistogram(
        MetricName name,
        HistogramConfig config,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            MeasurableValueProvidersProviderImpl.INSTANCE,
            MeterImplMakerImpl.INSTANCE,
            null /* Histogram::update */,
            InstanceMakerImpl.INSTANCE,
            timeMsProvider,
            executor);
    }
}
