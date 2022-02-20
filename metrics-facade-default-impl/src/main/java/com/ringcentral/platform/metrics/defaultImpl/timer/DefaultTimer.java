package com.ringcentral.platform.metrics.defaultImpl.timer;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.defaultImpl.histogram.*;
import com.ringcentral.platform.metrics.defaultImpl.rate.*;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.timer.*;
import com.ringcentral.platform.metrics.timer.configs.*;
import com.ringcentral.platform.metrics.utils.*;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

public class DefaultTimer extends AbstractTimer<TimerImpl> {

    protected interface MVP extends MeasurableValueProvider<TimerImpl> {

        @Override
        default Object valueFor(TimerImpl timer) {
            return valueFor(timer, timer.histogram().snapshot());
        }

        Object valueFor(TimerImpl timer, HistogramSnapshot snapshot);
    }

    public static class MeasurableValuesImpl extends AbstractMeasurableValues {

        private final TimerImpl timer;
        private final HistogramSnapshot snapshot;
        private final Map<Measurable, MeasurableValueProvider<TimerImpl>> measurableValueProviders;

        public MeasurableValuesImpl(
            TimerImpl timer,
            Map<Measurable, MeasurableValueProvider<TimerImpl>> measurableValueProviders) {

            super(measurableValueProviders.keySet());
            this.timer = timer;
            this.snapshot = timer.histogram().snapshot();
            this.measurableValueProviders = measurableValueProviders;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> V valueOfImpl(Measurable measurable) throws NotMeasuredException {
            MVP valueProvider = (MVP)measurableValueProviders.get(measurable);
            return (V)valueProvider.valueFor(timer, snapshot);
        }
    }

    public static class MeasurableValueProvidersProviderImpl implements MeasurableValueProvidersProvider<
        TimerImpl,
        TimerInstanceConfig,
        TimerSliceConfig,
        TimerConfig> {

        public static final MeasurableValueProvidersProviderImpl INSTANCE = new MeasurableValueProvidersProviderImpl();

        public static final double DURATION_FACTOR = 1.0 / MILLISECONDS.toNanos(1L);

        public static final MVP COUNT_VALUE_PROVIDER = (t, s) -> s.count();
        public static final MVP TOTAL_SUM_VALUE_PROVIDER = (t, s) -> s.totalSum();
        public static final MVP MEAN_RATE_VALUE_PROVIDER = (t, s) -> t.rate().meanRate();
        public static final MVP ONE_MINUTE_RATE_VALUE_PROVIDER = (t, s) -> t.rate().oneMinuteRate();
        public static final MVP FIVE_MINUTES_RATE_VALUE_PROVIDER = (t, s) -> t.rate().fiveMinutesRate();
        public static final MVP FIFTEEN_MINUTES_RATE_VALUE_PROVIDER = (t, s) -> t.rate().fiveMinutesRate();

        public static final MVP RATE_UNIT_VALUE_PROVIDER = new MVP() {

            @Override
            public Object valueFor(TimerImpl timer) {
                return "events/sec";
            }

            @Override
            public Object valueFor(TimerImpl timer, HistogramSnapshot snapshot) {
                return "events/sec";
            }
        };

        public static final MVP MIN_VALUE_PROVIDER = (t, s) -> s.min() * DURATION_FACTOR;
        public static final MVP MAX_VALUE_PROVIDER = (t, s) -> s.max() * DURATION_FACTOR;
        public static final MVP MEAN_VALUE_PROVIDER = (t, s) -> s.mean() * DURATION_FACTOR;
        public static final MVP STANDARD_DEVIATION_VALUE_PROVIDER = (t, s) -> s.standardDeviation() * DURATION_FACTOR;

        public static class PercentileValueProvider implements MVP {

            final Percentile percentile;

            public PercentileValueProvider(Percentile percentile) {
                this.percentile = percentile;
            }

            @Override
            public Object valueFor(TimerImpl timer, HistogramSnapshot snapshot) {
                return snapshot.percentileValue(percentile) * DURATION_FACTOR;
            }
        }

        public static class BucketValueProvider implements MVP {

            final Bucket bucket;

            public BucketValueProvider(Bucket bucket) {
                this.bucket = bucket;
            }

            @Override
            public Object valueFor(TimerImpl timer, HistogramSnapshot snapshot) {
                return snapshot.bucketSize(bucket);
            }
        }

        public static final MVP DURATION_UNIT_VALUE_PROVIDER = new MVP() {

            @Override
            public Object valueFor(TimerImpl timer) {
                return "ms";
            }

            @Override
            public Object valueFor(TimerImpl timer, HistogramSnapshot snapshot) {
                return "ms";
            }
        };

        private static final Map<Measurable, MeasurableValueProvider<TimerImpl>> DEFAULT_MEASURABLE_VALUE_PROVIDERS;

        private static final Logger logger = getLogger(MeasurableValueProvidersProviderImpl.class);

        static {
            DEFAULT_MEASURABLE_VALUE_PROVIDERS = makeDefaultMeasurableValueProviders();
        }

        private static Map<Measurable, MeasurableValueProvider<TimerImpl>> makeDefaultMeasurableValueProviders() {
            Map<Measurable, MeasurableValueProvider<TimerImpl>> result = new LinkedHashMap<>();
            Ref<Boolean> infBucketAdded = new Ref<>(false);

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
                } else if (m instanceof TotalSum) {
                    result.put(m, TOTAL_SUM_VALUE_PROVIDER);
                } else if (m instanceof Min) {
                    result.put(m, MIN_VALUE_PROVIDER);
                } else if (m instanceof Max) {
                    result.put(m, MAX_VALUE_PROVIDER);
                } else if (m instanceof Mean) {
                    result.put(m, MEAN_VALUE_PROVIDER);
                } else if (m instanceof StandardDeviation) {
                    result.put(m, STANDARD_DEVIATION_VALUE_PROVIDER);
                } else if (m instanceof Percentile) {
                    result.put(m, new PercentileValueProvider((Percentile)m));
                } else if (m instanceof Bucket) {
                    addBucketMvp(result, m, infBucketAdded);
                } else if (m instanceof Buckets) {
                    for (Bucket bucket : ((Buckets)m).buckets()) {
                        addBucketMvp(result, bucket, infBucketAdded);
                    }
                } else if (m instanceof DurationUnit) {
                    result.put(m, DURATION_UNIT_VALUE_PROVIDER);
                }
            });

            return Map.copyOf(result);
        }

        private static void addBucketMvp(
            Map<Measurable, MeasurableValueProvider<TimerImpl>> result,
            Measurable measurable,
            Ref<Boolean> infBucketAdded) {

            Bucket b = (Bucket)measurable;

            if (!infBucketAdded.value()) {
                result.put(INF_BUCKET, new BucketValueProvider(INF_BUCKET));
                infBucketAdded.setValue(true);
            }

            if (!b.isInf()) {
                result.put(measurable, new BucketValueProvider(b));
            }
        }

        @Override
        public Map<Measurable, MeasurableValueProvider<TimerImpl>> valueProvidersFor(
            TimerInstanceConfig instanceConfig,
            TimerSliceConfig sliceConfig,
            TimerConfig config,
            Set<? extends Measurable> measurables) {

            if (measurables == null || measurables.isEmpty()) {
                return DEFAULT_MEASURABLE_VALUE_PROVIDERS;
            }

            Map<Measurable, MeasurableValueProvider<TimerImpl>> result = new LinkedHashMap<>();
            Ref<Boolean> infBucketAdded = new Ref<>(false);

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
                } else if (m instanceof TotalSum) {
                    result.put(m, TOTAL_SUM_VALUE_PROVIDER);
                } else if (m instanceof Min) {
                    result.put(m, MIN_VALUE_PROVIDER);
                } else if (m instanceof Max) {
                    result.put(m, MAX_VALUE_PROVIDER);
                } else if (m instanceof Mean) {
                    result.put(m, MEAN_VALUE_PROVIDER);
                } else if (m instanceof StandardDeviation) {
                    result.put(m, STANDARD_DEVIATION_VALUE_PROVIDER);
                } else if (m instanceof Percentile) {
                    result.put(m, new PercentileValueProvider((Percentile)m));
                } else if (m instanceof Bucket) {
                    addBucketMvp(result, m, infBucketAdded);
                } else if (m instanceof DurationUnit) {
                    result.put(m, DURATION_UNIT_VALUE_PROVIDER);
                } else if (m instanceof Buckets) {
                    for (Bucket bucket : ((Buckets)m).buckets()) {
                        addBucketMvp(result, bucket, infBucketAdded);
                    }
                } else {
                    logger.warn("Unsupported measurable {}", m.getClass().getName());
                }
            });

            return !result.isEmpty() ? Map.copyOf(result) : DEFAULT_MEASURABLE_VALUE_PROVIDERS;
        }
    }

    public static final class MeterImplMakerImpl implements MeterImplMaker<
        TimerImpl,
        TimerInstanceConfig,
        TimerSliceConfig,
        TimerConfig> {

        public static final MeterImplMakerImpl INSTANCE = new MeterImplMakerImpl();

        @Override
        @SuppressWarnings("DuplicatedCode")
        public TimerImpl makeMeterImpl(
            TimerInstanceConfig instanceConfig,
            TimerSliceConfig sliceConfig,
            TimerConfig config,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            Set<? extends Measurable> rateMeasurables = measurables;

            if (measurables.stream().anyMatch(m -> m instanceof Count)) {
                rateMeasurables = new HashSet<>(measurables);
                rateMeasurables.removeIf(m -> m instanceof Count);
                rateMeasurables = Set.copyOf(rateMeasurables);
            }

            MetricContext instanceContext = instanceConfig != null ? instanceConfig.context() : null;
            MetricContext sliceContext = sliceConfig != null ? sliceConfig.context() : null;
            MetricContext context = config != null ? config.context() : null;

            RateImpl rateImpl = DefaultRate.MeterImplMakerImpl.INSTANCE.makeMeterImpl(
                instanceContext,
                sliceContext,
                context,
                rateMeasurables,
                executor,
                registry);

            HistogramImpl histogramImpl = DefaultHistogram.MeterImplMakerImpl.INSTANCE.makeMeterImpl(
                instanceContext,
                sliceContext,
                context,
                measurables,
                executor,
                registry);

            return new DefaultTimerImpl(rateImpl, histogramImpl);
        }
    }

    public static class InstanceMakerImpl implements InstanceMaker<TimerImpl> {

        public static final InstanceMakerImpl INSTANCE = new InstanceMakerImpl();

        @Override
        public AbstractMeterInstance<TimerImpl> makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<TimerImpl>> measurableValueProviders,
            TimerImpl timer) {

            return new DefaultTimerInstance(
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
        public AbstractExpirableMeterInstance<TimerImpl> makeExpirableInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<TimerImpl>> measurableValueProviders,
            TimerImpl timer,
            long creationTimeMs) {

            return new DefaultExpirableTimerInstance(
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

    public DefaultTimer(
        MetricName name,
        TimerConfig config,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor,
        MetricRegistry registry) {

        super(
            name,
            config,
            MeasurableValueProvidersProviderImpl.INSTANCE,
            MeterImplMakerImpl.INSTANCE,
            TimerImpl::update,
            InstanceMakerImpl.INSTANCE,
            timeMsProvider,
            executor,
            registry);
    }

    @Override
    public Stopwatch stopwatch(MetricDimensionValues dimensionValues) {
        return new DefaultStopwatch(this, dimensionValues);
    }
}
