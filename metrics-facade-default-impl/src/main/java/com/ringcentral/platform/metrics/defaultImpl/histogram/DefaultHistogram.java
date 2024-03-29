package com.ringcentral.platform.metrics.defaultImpl.histogram;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.*;
import com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.configs.*;
import com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.neverReset.NeverResetHdrHistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.resetByChunks.ResetByChunksHdrHistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.hdr.resetOnSnapshot.ResetOnSnapshotHdrHistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.*;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.neverReset.NeverResetScaleHistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.resetByChunks.ResetByChunksScaleHistogramImpl;
import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.resetOnSnapshot.ResetOnSnapshotScaleHistogramImpl;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.histogram.AbstractHistogram;
import com.ringcentral.platform.metrics.histogram.configs.*;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.utils.*;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static com.ringcentral.platform.metrics.utils.MetricContextUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

public class DefaultHistogram extends AbstractHistogram<HistogramImpl> {

    protected interface MVP extends MeasurableValueProvider<HistogramImpl> {

        @Override
        default Object valueFor(HistogramImpl histogram) {
            return valueFor(histogram, histogram.snapshot());
        }

        Object valueFor(HistogramImpl histogram, HistogramSnapshot snapshot);
    }

    public static class MeasurableValuesImpl extends AbstractMeasurableValues {

        private final HistogramImpl histogram;
        private final HistogramSnapshot snapshot;
        private final Map<Measurable, MeasurableValueProvider<HistogramImpl>> measurableValueProviders;

        public MeasurableValuesImpl(
            HistogramImpl histogram,
            Map<Measurable, MeasurableValueProvider<HistogramImpl>> measurableValueProviders) {

            super(measurableValueProviders.keySet());
            this.histogram = histogram;
            this.snapshot = histogram.snapshot();
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
        HistogramImpl,
        HistogramInstanceConfig,
        HistogramSliceConfig,
        HistogramConfig> {

        public static final MeasurableValueProvidersProviderImpl INSTANCE = new MeasurableValueProvidersProviderImpl();

        public static final MVP COUNT_VALUE_PROVIDER = (h, s) -> s.count();
        public static final MVP TOTAL_SUM_VALUE_PROVIDER = (h, s) -> s.totalSum();
        public static final MVP MIN_VALUE_PROVIDER = (h, s) -> s.min();
        public static final MVP MAX_VALUE_PROVIDER = (h, s) -> s.max();
        public static final MVP MEAN_VALUE_PROVIDER = (h, s) -> s.mean();
        public static final MVP STANDARD_DEVIATION_VALUE_PROVIDER = (h, s) -> s.standardDeviation();

        public static class PercentileValueProvider implements MVP {

            final Percentile percentile;

            public PercentileValueProvider(Percentile percentile) {
                this.percentile = percentile;
            }

            @Override
            public Object valueFor(HistogramImpl histogram, HistogramSnapshot snapshot) {
                return snapshot.percentileValue(percentile);
            }
        }

        public static class BucketValueProvider implements MVP {

            final Bucket bucket;

            public BucketValueProvider(Bucket bucket) {
                this.bucket = bucket;
            }

            @Override
            public Object valueFor(HistogramImpl histogram, HistogramSnapshot snapshot) {
                return snapshot.bucketSize(bucket);
            }
        }

        private static final Map<Measurable, MeasurableValueProvider<HistogramImpl>> DEFAULT_MEASURABLE_VALUE_PROVIDERS;
        private static final Logger logger = getLogger(MeasurableValueProvidersProviderImpl.class);

        static {
            DEFAULT_MEASURABLE_VALUE_PROVIDERS = makeDefaultMeasurableValueProviders();
        }

        private static Map<Measurable, MeasurableValueProvider<HistogramImpl>> makeDefaultMeasurableValueProviders() {
            Map<Measurable, MeasurableValueProvider<HistogramImpl>> result = new LinkedHashMap<>();
            Ref<Boolean> infBucketAdded = new Ref<>(false);

            DEFAULT_HISTOGRAM_MEASURABLES.forEach(m -> {
                if (m instanceof Count) {
                    result.put(m, COUNT_VALUE_PROVIDER);
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
                }
            });

            return Map.copyOf(result);
        }

        private static void addBucketMvp(
            Map<Measurable, MeasurableValueProvider<HistogramImpl>> result,
            Measurable measurable,
            Ref<Boolean> infBucketAdded) {

            Bucket bucket = (Bucket)measurable;

            if (!infBucketAdded.value()) {
                result.put(INF_BUCKET, new BucketValueProvider(INF_BUCKET));
                infBucketAdded.setValue(true);
            }

            if (!bucket.isInf()) {
                result.put(measurable, new BucketValueProvider(bucket));
            }
        }

        @Override
        public Map<Measurable, MeasurableValueProvider<HistogramImpl>> valueProvidersFor(
            HistogramInstanceConfig instanceConfig,
            HistogramSliceConfig sliceConfig,
            HistogramConfig config,
            Set<? extends Measurable> measurables) {

            if (measurables == null || measurables.isEmpty()) {
                return DEFAULT_MEASURABLE_VALUE_PROVIDERS;
            }

            Map<Measurable, MeasurableValueProvider<HistogramImpl>> result = new LinkedHashMap<>();
            Ref<Boolean> infBucketAdded = new Ref<>(false);

            measurables.forEach(m -> {
                if (m instanceof Count) {
                    result.put(m, COUNT_VALUE_PROVIDER);
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
                } else {
                    logger.warn("Unsupported measurable {}", m.getClass().getName());
                }
            });

            return !result.isEmpty() ? Map.copyOf(result) : DEFAULT_MEASURABLE_VALUE_PROVIDERS;
        }
    }

    public static final class MeterImplMakerImpl implements MeterImplMaker<
        HistogramImpl,
        HistogramInstanceConfig,
        HistogramSliceConfig,
        HistogramConfig> {

        public static final MeterImplMakerImpl INSTANCE = new MeterImplMakerImpl();

        @Override
        public HistogramImpl makeMeterImpl(
            HistogramInstanceConfig instanceConfig,
            HistogramSliceConfig sliceConfig,
            HistogramConfig config,
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

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        public HistogramImpl makeMeterImpl(
            MetricContext instanceContext,
            MetricContext sliceContext,
            MetricContext context,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            HistogramImplConfig implConfig = implConfig(instanceContext, sliceContext, context);
            HistogramImpl impl;

            if (implConfig instanceof HdrHistogramImplConfig) {
                HdrHistogramImplConfig hdrImplConfig = (HdrHistogramImplConfig)implConfig;

                if (hdrImplConfig.type() == HdrHistogramImplType.NEVER_RESET) {
                    impl = new NeverResetHdrHistogramImpl(hdrImplConfig, measurables, executor);
                } else if (hdrImplConfig.type() == HdrHistogramImplType.RESET_ON_SNAPSHOT) {
                    impl = new ResetOnSnapshotHdrHistogramImpl(hdrImplConfig, measurables, executor);
                } else if (hdrImplConfig.type() == HdrHistogramImplType.RESET_BY_CHUNKS) {
                    impl = new ResetByChunksHdrHistogramImpl(hdrImplConfig, measurables, executor);
                } else {
                    throw new IllegalArgumentException(
                        "Unsupported " + HdrHistogramImplConfig.class.getSimpleName()
                        + ": " + hdrImplConfig.getClass().getName());
                }
            } else if (implConfig instanceof ScaleHistogramImplConfig) {
                ScaleHistogramImplConfig scaleImplConfig = (ScaleHistogramImplConfig)implConfig;

                if (scaleImplConfig.type() == ScaleHistogramImplType.NEVER_RESET) {
                    impl = new NeverResetScaleHistogramImpl(scaleImplConfig, measurables, executor);
                } else if (scaleImplConfig.type() == ScaleHistogramImplType.RESET_ON_SNAPSHOT) {
                    impl = new ResetOnSnapshotScaleHistogramImpl(scaleImplConfig, measurables, executor);
                } else if (scaleImplConfig.type() == ScaleHistogramImplType.RESET_BY_CHUNKS) {
                    impl = new ResetByChunksScaleHistogramImpl(scaleImplConfig, measurables, executor);
                } else {
                    throw new IllegalArgumentException(
                        "Unsupported " + ScaleHistogramImplConfig.class.getSimpleName()
                        + ": " + scaleImplConfig.getClass().getName());
                }
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
                    "Unsupported " + HistogramImplConfig.class.getSimpleName()
                    + ": " + implConfig.getClass().getName());
            }

            if (implConfig.hasSnapshotTtl()) {
                impl = new SnapshotCachingHistogramImpl(impl, implConfig.snapshotTtl().get());
            }

            return impl;
        }

        private HistogramImplConfig implConfig(
            MetricContext instanceContext,
            MetricContext sliceContext,
            MetricContext context) {

            if (has(HistogramImplConfig.class, instanceContext, sliceContext, context)) {
                return getForClass(HistogramImplConfig.class, instanceContext, sliceContext, context);
            }

            if (has(HistogramImplConfigBuilder.class, instanceContext, sliceContext, context)) {
                return getForClass(HistogramImplConfigBuilder.class, instanceContext, sliceContext, context).build();
            }

            return HdrHistogramImplConfig.DEFAULT;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private HistogramImpl makeCustomImpl(
            HistogramImplConfig config,
            MetricContext instanceContext,
            MetricContext sliceContext,
            MetricContext context,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            CustomHistogramImplSpec<? extends HistogramImplConfig> implSpec =
                ((DefaultMetricRegistry)registry).customHistogramImplSpecFor(config.getClass());

            if (implSpec == null) {
                return null;
            }

            CustomHistogramImplMaker implMaker = implSpec.implMaker();

            return implMaker.makeHistogramImpl(
                config,
                instanceContext,
                sliceContext,
                context,
                measurables,
                executor,
                registry);
        }
    }

    public static class InstanceMakerImpl implements InstanceMaker<HistogramImpl> {

        public static final InstanceMakerImpl INSTANCE = new InstanceMakerImpl();

        @Override
        public AbstractMeterInstance<HistogramImpl> makeInstance(
            MetricName name,
            List<LabelValue> labelValues,
            boolean totalInstance,
            boolean labeledMetricTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<HistogramImpl>> measurableValueProviders,
            HistogramImpl histogram) {

            return new DefaultHistogramInstance(
                name,
                labelValues,
                totalInstance,
                labeledMetricTotalInstance,
                levelInstance,
                () -> new MeasurableValuesImpl(histogram, measurableValueProviders),
                measurableValueProviders,
                histogram);
        }

        @Override
        public AbstractExpirableMeterInstance<HistogramImpl> makeExpirableInstance(
            MetricName name,
            List<LabelValue> labelValues,
            boolean totalInstance,
            boolean labeledMetricTotalInstance,
            boolean levelInstance,
            Map<Measurable, MeasurableValueProvider<HistogramImpl>> measurableValueProviders,
            HistogramImpl histogram,
            long creationTimeMs) {

            return new DefaultExpirableHistogramInstance(
                name,
                labelValues,
                totalInstance,
                labeledMetricTotalInstance,
                levelInstance,
                () -> new MeasurableValuesImpl(histogram, measurableValueProviders),
                measurableValueProviders,
                histogram,
                creationTimeMs);
        }
    }

    public DefaultHistogram(
        MetricName name,
        HistogramConfig config,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor,
        MetricRegistry registry) {

        super(
            name,
            config,
            MeasurableValueProvidersProviderImpl.INSTANCE,
            MeterImplMakerImpl.INSTANCE,
            HistogramImpl::update,
            InstanceMakerImpl.INSTANCE,
            timeMsProvider,
            executor,
            registry);
    }
}
