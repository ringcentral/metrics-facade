package com.ringcentral.platform.metrics.x.histogram;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.histogram.AbstractHistogram;
import com.ringcentral.platform.metrics.histogram.configs.*;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.utils.*;
import com.ringcentral.platform.metrics.x.histogram.configs.*;
import com.ringcentral.platform.metrics.x.histogram.hdr.*;
import com.ringcentral.platform.metrics.x.histogram.hdr.configs.*;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static org.slf4j.LoggerFactory.getLogger;

public class XHistogram extends AbstractHistogram<XHistogramImpl> {

    protected interface MVP extends MeasurableValueProvider<XHistogramImpl> {

        @Override
        default Object valueFor(XHistogramImpl histogram) {
            return valueFor(histogram, histogram.snapshot());
        }

        Object valueFor(XHistogramImpl histogram, XHistogramImplSnapshot snapshot);
    }

    public static class MeasurableValuesImpl extends AbstractMeasurableValues {

        private final XHistogramImpl histogram;
        private final XHistogramImplSnapshot snapshot;
        private final Map<Measurable, MeasurableValueProvider<XHistogramImpl>> measurableValueProviders;

        public MeasurableValuesImpl(
            XHistogramImpl histogram,
            Map<Measurable, MeasurableValueProvider<XHistogramImpl>> measurableValueProviders) {

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
        XHistogramImpl,
        HistogramInstanceConfig,
        HistogramSliceConfig,
        HistogramConfig> {

        public static final MeasurableValueProvidersProviderImpl INSTANCE = new MeasurableValueProvidersProviderImpl();

        public static final MVP COUNT_VALUE_PROVIDER = new MVP() {

            @Override
            public Object valueFor(XHistogramImpl histogram) {
                return histogram.count();
            }

            @Override
            public Object valueFor(XHistogramImpl histogram, XHistogramImplSnapshot snapshot) {
                return histogram.count();
            }
        };

        public static final MVP TOTAL_SUM_VALUE_PROVIDER = new MVP() {

            @Override
            public Object valueFor(XHistogramImpl histogram) {
                return histogram.totalSum();
            }

            @Override
            public Object valueFor(XHistogramImpl histogram, XHistogramImplSnapshot snapshot) {
                return histogram.totalSum();
            }
        };

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
            public Object valueFor(XHistogramImpl histogram, XHistogramImplSnapshot snapshot) {
                return snapshot.percentileValue(percentile);
            }
        }

        public static class BucketValueProvider implements MVP {

            final Bucket bucket;

            public BucketValueProvider(Bucket bucket) {
                this.bucket = bucket;
            }

            @Override
            public Object valueFor(XHistogramImpl histogram, XHistogramImplSnapshot snapshot) {
                return snapshot.bucketSize(bucket);
            }
        }

        private static final Map<Measurable, MeasurableValueProvider<XHistogramImpl>> DEFAULT_MEASURABLE_VALUE_PROVIDERS;
        private static final Logger logger = getLogger(MeasurableValueProvidersProviderImpl.class);

        static {
            DEFAULT_MEASURABLE_VALUE_PROVIDERS = makeDefaultMeasurableValueProviders();
        }

        private static Map<Measurable, MeasurableValueProvider<XHistogramImpl>> makeDefaultMeasurableValueProviders() {
            Map<Measurable, MeasurableValueProvider<XHistogramImpl>> result = new LinkedHashMap<>();
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
                }
            });

            return Map.copyOf(result);
        }

        private static void addBucketMvp(
            Map<Measurable, MeasurableValueProvider<XHistogramImpl>> result,
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
        public Map<Measurable, MeasurableValueProvider<XHistogramImpl>> valueProvidersFor(
            HistogramInstanceConfig instanceConfig,
            HistogramSliceConfig sliceConfig,
            HistogramConfig config,
            Set<? extends Measurable> measurables) {

            if (measurables == null || measurables.isEmpty()) {
                return DEFAULT_MEASURABLE_VALUE_PROVIDERS;
            }

            Map<Measurable, MeasurableValueProvider<XHistogramImpl>> result = new LinkedHashMap<>();
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
        public XHistogramImpl makeMeterImpl(
            HistogramInstanceConfig instanceConfig,
            HistogramSliceConfig sliceConfig,
            HistogramConfig config,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor) {

            return makeMeterImpl(
                instanceConfig != null ? instanceConfig.context() : null,
                sliceConfig != null ? sliceConfig.context() : null,
                config != null ? config.context() : null,
                measurables,
                executor);
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        public XHistogramImpl makeMeterImpl(
            MetricContext instanceContext,
            MetricContext sliceContext,
            MetricContext context,
            Set<? extends Measurable> measurables,
            ScheduledExecutorService executor) {

            XHistogramImplConfig implConfig = null;

            if (instanceContext != null) {
                implConfig = xHistogramImplConfig(instanceContext);
            }

            if (implConfig == null && sliceContext != null) {
                implConfig = xHistogramImplConfig(sliceContext);
            }

            if (implConfig == null && context != null) {
                implConfig = xHistogramImplConfig(context);
            }

            if (implConfig == null) {
                implConfig = HdrXHistogramImplConfig.DEFAULT;
            }

            if (implConfig instanceof HdrXHistogramImplConfig) {
                HdrXHistogramImplConfig hdrImplConfig = (HdrXHistogramImplConfig)implConfig;
                HdrXHistogramImpl hdrImpl;

                if (hdrImplConfig.type() == HdrXHistogramType.UNIFORM) {
                    hdrImpl = new UniformHdrXHistogramImpl(hdrImplConfig, measurables, executor);
                } else if (hdrImplConfig.type() == HdrXHistogramType.RESET_ON_SNAPSHOT) {
                    hdrImpl = new ResetOnSnapshotHdrXHistogramImpl(hdrImplConfig, measurables, executor);
                } else if (hdrImplConfig.type() == HdrXHistogramType.RESET_BY_CHUNKS) {
                    hdrImpl = new ResetByChunksHdrXHistogramImpl(hdrImplConfig, measurables, executor);
                } else {
                    throw new IllegalArgumentException(
                        "Unsupported " + HdrXHistogramImplConfig.class.getSimpleName()
                        + ": " + hdrImplConfig.getClass().getName());
                }

                return
                    hdrImplConfig.hasSnapshotTtl() ?
                    new SnapshotCachingHdrXHistogramImpl(hdrImpl, hdrImplConfig.snapshotTtl().get()) :
                    hdrImpl;
            }

            throw new IllegalArgumentException(
                "Unsupported " + XHistogramImplConfig.class.getSimpleName()
                + ": " + implConfig.getClass().getName());
        }

        private XHistogramImplConfig xHistogramImplConfig(MetricContext context) {
            if (context.has(XHistogramImplConfig.class)) {
                return context.getForClass(XHistogramImplConfig.class);
            } else if (context.has(HdrXHistogramImplConfig.class)) {
                return context.getForClass(HdrXHistogramImplConfig.class);
            } else if (context.has(XHistogramImplConfigBuilder.class)) {
                return context.getForClass(XHistogramImplConfigBuilder.class).build();
            } else if (context.has(HdrXHistogramImplConfigBuilder.class)) {
                return context.getForClass(HdrXHistogramImplConfigBuilder.class).build();
            }

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
            XHistogramImpl::update,
            InstanceMakerImpl.INSTANCE,
            timeMsProvider,
            executor);
    }
}
