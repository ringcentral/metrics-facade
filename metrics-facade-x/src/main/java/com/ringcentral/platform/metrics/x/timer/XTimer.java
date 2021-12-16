package com.ringcentral.platform.metrics.x.timer;

import com.ringcentral.platform.metrics.NotMeasuredException;
import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.histogram.Histogram.*;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.timer.*;
import com.ringcentral.platform.metrics.timer.configs.*;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

public class XTimer extends AbstractTimer<XTimerImpl> {

    protected interface MVP extends MeasurableValueProvider<XTimerImpl> {

        @Override
        default Object valueFor(XTimerImpl timer) {
            return valueFor(timer, null /* timer.getSnapshot() */);
        }

        Object valueFor(XTimerImpl timer, Object snapshot /* Snapshot snapshot */);
    }

    public static class MeasurableValuesImpl extends AbstractMeasurableValues {

        private final XTimerImpl timer;
        // private final Snapshot snapshot;
        private final Map<Measurable, MeasurableValueProvider<XTimerImpl>> measurableValueProviders;

        public MeasurableValuesImpl(
            XTimerImpl timer,
            Map<Measurable, MeasurableValueProvider<XTimerImpl>> measurableValueProviders) {

            super(measurableValueProviders.keySet());
            this.timer = timer;
            // this.snapshot = timer.getSnapshot();
            this.measurableValueProviders = measurableValueProviders;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> V valueOfImpl(Measurable measurable) throws NotMeasuredException {
            MVP valueProvider = (MVP)measurableValueProviders.get(measurable);
            return (V)valueProvider.valueFor(timer, null /* snapshot */);
        }
    }

    public static class MeasurableValueProvidersProviderImpl implements MeasurableValueProvidersProvider<XTimerImpl> {

        public static final MeasurableValueProvidersProviderImpl INSTANCE = new MeasurableValueProvidersProviderImpl();

        public static final double DURATION_FACTOR = 1.0 / MILLISECONDS.toNanos(1L);

        public static final MVP COUNT_VALUE_PROVIDER = new MVP() {

            @Override
            public Object valueFor(XTimerImpl timer) {
                return 1L; // timer.getCount();
            }

            @Override
            public Object valueFor(XTimerImpl timer, Object snapshot /* Snapshot snapshot */) {
                return 1L; // timer.getCount();
            }
        };

        public static final MVP MEAN_RATE_VALUE_PROVIDER = (t, s) -> 1L; // t.getMeanRate();
        public static final MVP ONE_MINUTE_RATE_VALUE_PROVIDER = (t, s) -> 1L; // t.getOneMinuteRate();
        public static final MVP FIVE_MINUTES_RATE_VALUE_PROVIDER = (t, s) -> 1L; // t.getFiveMinuteRate();
        public static final MVP FIFTEEN_MINUTES_RATE_VALUE_PROVIDER = (t, s) -> 1L; // t.getFifteenMinuteRate();

        public static final MVP RATE_UNIT_VALUE_PROVIDER = new MVP() {

            @Override
            public Object valueFor(XTimerImpl timer) {
                return "events/sec";
            }

            @Override
            public Object valueFor(XTimerImpl timer, Object snapshot /* Snapshot snapshot */) {
                return "events/sec";
            }
        };

        public static final MVP MIN_VALUE_PROVIDER = (t, s) -> 1L; // s.getMin() * DURATION_FACTOR;
        public static final MVP MAX_VALUE_PROVIDER = (t, s) -> 1L; // s.getMax() * DURATION_FACTOR;
        public static final MVP MEAN_VALUE_PROVIDER = (t, s) -> 1L; // s.getMean() * DURATION_FACTOR;
        public static final MVP STANDARD_DEVIATION_VALUE_PROVIDER = (t, s) -> 1L; // s.getStdDev() * DURATION_FACTOR;

        public static class PercentileValueProvider implements MVP {

            final double quantile;

            public PercentileValueProvider(double quantile) {
                this.quantile = quantile;
            }

            @Override
            public Object valueFor(XTimerImpl timer, Object snapshot /* Snapshot snapshot */) {
                return 1L; // snapshot.getValue(quantile) * DURATION_FACTOR;
            }
        }

        public static final MVP DURATION_UNIT_VALUE_PROVIDER = new MVP() {

            @Override
            public Object valueFor(XTimerImpl timer) {
                return "ms";
            }

            @Override
            public Object valueFor(XTimerImpl timer, Object snapshot /* Snapshot snapshot */) {
                return "ms";
            }
        };

        private static final Map<Measurable, MeasurableValueProvider<XTimerImpl>> DEFAULT_MEASURABLE_VALUE_PROVIDERS;

        private static final Logger logger = getLogger(MeasurableValueProvidersProviderImpl.class);

        static {
            DEFAULT_MEASURABLE_VALUE_PROVIDERS = makeDefaultMeasurableValueProviders();
        }

        private static Map<Measurable, MeasurableValueProvider<XTimerImpl>> makeDefaultMeasurableValueProviders() {
            Map<Measurable, MeasurableValueProvider<XTimerImpl>> result = new HashMap<>();

            DEFAULT_TIMER_MEASURABLES.forEach(m -> {
                if (m instanceof Count) {
                    result.put(m, COUNT_VALUE_PROVIDER);
                } else if (m instanceof Rate.MeanRate) {
                    result.put(m, MEAN_RATE_VALUE_PROVIDER);
                } else if (m instanceof Rate.OneMinuteRate) {
                    result.put(m, ONE_MINUTE_RATE_VALUE_PROVIDER);
                } else if (m instanceof Rate.FiveMinutesRate) {
                    result.put(m, FIVE_MINUTES_RATE_VALUE_PROVIDER);
                } else if (m instanceof Rate.FifteenMinutesRate) {
                    result.put(m, FIFTEEN_MINUTES_RATE_VALUE_PROVIDER);
                } else if (m instanceof Rate.RateUnit) {
                    result.put(m, RATE_UNIT_VALUE_PROVIDER);
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
                } else if (m instanceof DurationUnit) {
                    result.put(m, DURATION_UNIT_VALUE_PROVIDER);
                }
            });

            return Map.copyOf(result);
        }

        @Override
        public Map<Measurable, MeasurableValueProvider<XTimerImpl>> valueProvidersFor(Set<? extends Measurable> measurables) {
            if (measurables == null || measurables.isEmpty()) {
                return DEFAULT_MEASURABLE_VALUE_PROVIDERS;
            }

            Map<Measurable, MeasurableValueProvider<XTimerImpl>> result = new HashMap<>();

            measurables.forEach(m -> {
                if (m instanceof Count) {
                    result.put(m, COUNT_VALUE_PROVIDER);
                } else if (m instanceof Rate.MeanRate) {
                    result.put(m, MEAN_RATE_VALUE_PROVIDER);
                } else if (m instanceof Rate.OneMinuteRate) {
                    result.put(m, ONE_MINUTE_RATE_VALUE_PROVIDER);
                } else if (m instanceof Rate.FiveMinutesRate) {
                    result.put(m, FIVE_MINUTES_RATE_VALUE_PROVIDER);
                } else if (m instanceof Rate.FifteenMinutesRate) {
                    result.put(m, FIFTEEN_MINUTES_RATE_VALUE_PROVIDER);
                } else if (m instanceof Rate.RateUnit) {
                    result.put(m, RATE_UNIT_VALUE_PROVIDER);
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
                } else if (m instanceof DurationUnit) {
                    result.put(m, DURATION_UNIT_VALUE_PROVIDER);
                } else {
                    logger.warn("Unsupported measurable {}", m.getClass().getName());
                }
            });

            return !result.isEmpty() ? Map.copyOf(result) : DEFAULT_MEASURABLE_VALUE_PROVIDERS;
        }
    }

    public static final class MeterImplMakerImpl implements MeterImplMaker<
        XTimerImpl,
        TimerInstanceConfig,
        TimerSliceConfig,
        TimerConfig> {

        public static final MeterImplMakerImpl INSTANCE = new MeterImplMakerImpl();

        @Override
        @SuppressWarnings("DuplicatedCode")
        public XTimerImpl makeMeterImpl(
            TimerInstanceConfig instanceConfig,
            TimerSliceConfig sliceConfig,
            TimerConfig config,
            Set<? extends Measurable> measurables) {

//            if (instanceConfig != null && instanceConfig.context().has(XTimerImpl.class)) {
//                return instanceConfig.context().get(XTimerImpl.class);
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
//            return new Timer(reservoir);

            return null;
        }
    }

    public static class InstanceMakerImpl implements InstanceMaker<XTimerImpl> {

        public static final InstanceMakerImpl INSTANCE = new InstanceMakerImpl();

        @Override
        public AbstractMeterInstance<XTimerImpl> makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<XTimerImpl>> measurableValueProviders,
            XTimerImpl timer) {

            return new XTimerInstance(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
                levelInstance,
                () -> new MeasurableValuesImpl(timer, measurableValueProviders),
                measurableValueProviders,
                timer);
        }

        @Override
        public AbstractExpirableMeterInstance<XTimerImpl> makeExpirableInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<XTimerImpl>> measurableValueProviders,
            XTimerImpl timer,
            long creationTimeMs) {

            return new XExpirableTimerInstance(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
                levelInstance,
                () -> new MeasurableValuesImpl(timer, measurableValueProviders),
                measurableValueProviders,
                timer,
                creationTimeMs);
        }
    }

    public XTimer(
        MetricName name,
        TimerConfig config,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            MeasurableValueProvidersProviderImpl.INSTANCE,
            MeterImplMakerImpl.INSTANCE,
            null, // (timer, value) -> timer.update(value, NANOSECONDS),
            InstanceMakerImpl.INSTANCE,
            timeMsProvider,
            executor);
    }

    @Override
    public Stopwatch stopwatch(MetricDimensionValues dimensionValues) {
        return new XStopwatch(this, dimensionValues);
    }
}
