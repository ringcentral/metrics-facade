package com.ringcentral.platform.metrics.defaultImpl;

import com.ringcentral.platform.metrics.AbstractMetricRegistry;
import com.ringcentral.platform.metrics.MetricMod;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.counter.configs.CounterConfig;
import com.ringcentral.platform.metrics.defaultImpl.counter.DefaultCounter;
import com.ringcentral.platform.metrics.defaultImpl.histogram.CustomHistogramImplMaker;
import com.ringcentral.platform.metrics.defaultImpl.histogram.CustomHistogramImplSpec;
import com.ringcentral.platform.metrics.defaultImpl.histogram.DefaultHistogram;
import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.HistogramImplConfig;
import com.ringcentral.platform.metrics.defaultImpl.rate.CustomRateImplMaker;
import com.ringcentral.platform.metrics.defaultImpl.rate.CustomRateImplSpec;
import com.ringcentral.platform.metrics.defaultImpl.rate.DefaultRate;
import com.ringcentral.platform.metrics.defaultImpl.rate.configs.RateImplConfig;
import com.ringcentral.platform.metrics.defaultImpl.timer.DefaultTimer;
import com.ringcentral.platform.metrics.defaultImpl.var.doubleVar.DefaultCachingDoubleVar;
import com.ringcentral.platform.metrics.defaultImpl.var.doubleVar.DefaultDoubleVar;
import com.ringcentral.platform.metrics.defaultImpl.var.longVar.DefaultCachingLongVar;
import com.ringcentral.platform.metrics.defaultImpl.var.longVar.DefaultLongVar;
import com.ringcentral.platform.metrics.defaultImpl.var.objectVar.DefaultCachingObjectVar;
import com.ringcentral.platform.metrics.defaultImpl.var.objectVar.DefaultObjectVar;
import com.ringcentral.platform.metrics.defaultImpl.var.stringVar.DefaultCachingStringVar;
import com.ringcentral.platform.metrics.defaultImpl.var.stringVar.DefaultStringVar;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.histogram.configs.HistogramConfig;
import com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder;
import com.ringcentral.platform.metrics.impl.MetricImplConfig;
import com.ringcentral.platform.metrics.impl.MetricImplConfigBuilder;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.rate.configs.RateConfig;
import com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.timer.configs.TimerConfig;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.doubleVar.CachingDoubleVar;
import com.ringcentral.platform.metrics.var.doubleVar.DoubleVar;
import com.ringcentral.platform.metrics.var.longVar.CachingLongVar;
import com.ringcentral.platform.metrics.var.longVar.LongVar;
import com.ringcentral.platform.metrics.var.objectVar.CachingObjectVar;
import com.ringcentral.platform.metrics.var.objectVar.ObjectVar;
import com.ringcentral.platform.metrics.var.stringVar.CachingStringVar;
import com.ringcentral.platform.metrics.var.stringVar.StringVar;
import org.slf4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings("SameParameterValue")
public class DefaultMetricRegistry extends AbstractMetricRegistry {

    public static class MetricMakerImpl implements MetricMaker {

        public static final MetricMakerImpl INSTANCE = new MetricMakerImpl();

        @Override
        public ObjectVar makeObjectVar(
            MetricName name,
            VarConfig config,
            Supplier<Object> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new DefaultObjectVar(
                name,
                config,
                valueSupplier,
                executor);
        }

        @Override
        public CachingObjectVar makeCachingObjectVar(
            MetricName name,
            CachingVarConfig config,
            Supplier<Object> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new DefaultCachingObjectVar(
                name,
                config,
                valueSupplier,
                executor);
        }

        @Override
        public LongVar makeLongVar(
            MetricName name,
            VarConfig config,
            Supplier<Long> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new DefaultLongVar(
                name,
                config,
                valueSupplier,
                executor);
        }

        @Override
        public CachingLongVar makeCachingLongVar(
            MetricName name,
            CachingVarConfig config,
            Supplier<Long> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new DefaultCachingLongVar(
                name,
                config,
                valueSupplier,
                executor);
        }

        @Override
        public DoubleVar makeDoubleVar(
            MetricName name,
            VarConfig config,
            Supplier<Double> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new DefaultDoubleVar(
                name,
                config,
                valueSupplier,
                executor);
        }

        @Override
        public CachingDoubleVar makeCachingDoubleVar(
            MetricName name,
            CachingVarConfig config,
            Supplier<Double> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new DefaultCachingDoubleVar(
                name,
                config,
                valueSupplier,
                executor);
        }

        @Override
        public StringVar makeStringVar(
            MetricName name,
            VarConfig config,
            Supplier<String> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new DefaultStringVar(
                name,
                config,
                valueSupplier,
                executor);
        }

        @Override
        public CachingStringVar makeCachingStringVar(
            MetricName name,
            CachingVarConfig config,
            Supplier<String> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new DefaultCachingStringVar(
                name,
                config,
                valueSupplier,
                executor);
        }

        @Override
        public Counter makeCounter(
            MetricName name,
            CounterConfig config,
            TimeMsProvider timeMsProvider,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new DefaultCounter(
                name,
                config,
                timeMsProvider,
                executor,
                registry);
        }

        @Override
        public Rate makeRate(
            MetricName name,
            RateConfig config,
            TimeMsProvider timeMsProvider,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new DefaultRate(
                name,
                config,
                timeMsProvider,
                executor,
                registry);
        }

        @Override
        public Histogram makeHistogram(
            MetricName name,
            HistogramConfig config,
            TimeMsProvider timeMsProvider,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new DefaultHistogram(
                name,
                config,
                timeMsProvider,
                executor,
                registry);
        }

        @Override
        public Timer makeTimer(
            MetricName name,
            TimerConfig config,
            TimeMsProvider timeMsProvider,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new DefaultTimer(
                name,
                config,
                timeMsProvider,
                executor,
                registry);
        }
    }

    private final ConcurrentMap<Class<? extends RateImplConfig>, CustomRateImplSpec<?>> customRateImplSpecs = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends HistogramImplConfig>, CustomHistogramImplSpec<?>> customHistogramImplSpecs = new ConcurrentHashMap<>();

    private static final Logger logger = getLogger(DefaultMetricRegistry.class);

    public DefaultMetricRegistry() {
        super(MetricMakerImpl.INSTANCE);
    }

    public DefaultMetricRegistry(ScheduledExecutorService executor) {
        super(MetricMakerImpl.INSTANCE, executor);
    }

    public DefaultMetricRegistry(
        PredicativeMetricNamedInfoProvider<MetricMod> preModsProvider,
        PredicativeMetricNamedInfoProvider<MetricMod> postModsProvider) {

        super(
            MetricMakerImpl.INSTANCE,
            preModsProvider,
            postModsProvider);
    }

    public DefaultMetricRegistry(
        PredicativeMetricNamedInfoProvider<MetricMod> preModsProvider,
        PredicativeMetricNamedInfoProvider<MetricMod> postModsProvider,
        ScheduledExecutorService executor) {

        super(
            MetricMakerImpl.INSTANCE,
            preModsProvider,
            postModsProvider,
            executor);
    }

    public DefaultMetricRegistry(
        PredicativeMetricNamedInfoProvider<MetricMod> preModsProvider,
        PredicativeMetricNamedInfoProvider<MetricMod> postModsProvider,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor) {

        super(
            MetricMakerImpl.INSTANCE,
            preModsProvider,
            postModsProvider,
            timeMsProvider,
            executor);
    }

    /* ***** Extensions ***** */

    /**
     * You can extend {@link DefaultMetricRegistry} with custom rate implementations
     * (the CountScalingRate* classes and the RateSample class can be found in the metrics-facade-samples module):
     * <ol>
     *   <li>Define an implementation type: {@code class I extends RateImpl}.
     *       <p>Example: {@code CountScalingRateImpl}.
     *
     *   <li>Define an implementation configuration type: {@code C extends RateImplConfig}.
     *       <p>Example: {@code CountScalingRateImplConfig}.
     *
     *   <li>Define an implementation configuration builder type: {@code CB extends RateImplConfigBuilder<C>}.
     *       <p>Example: {@code CountScalingRateConfigBuilder}.
     *
     *   <li>Define an implementation maker type: {@code IM extends CustomRateImplMaker<C>}.
     *       <p>Example: {@code CountScalingRateImplMaker}.
     *
     *   <li>Register the implementation maker type: {@code registry.extendWith(new IM())}.
     *       <p>Example: {@code registry.extendWith(new CountScalingRateImplMaker())}.
     *       <p>You can also use automatic discovery of implementation maker types:
     * <pre>
     * {@code
     * DefaultMetricRegistry registry = new DefaultMetricRegistryBuilder()
     *   .withCustomMetricImplsFromPackages("package.to.scan.1", ...)
     *   .build();
     * }
     * </pre>
     * </ol>
     *
     * You can choose the specific implementation using the
     * {@link RateConfigBuilder#impl(MetricImplConfigBuilder)} method.
     * <p>See an example in the {@code RateSample} class:
     * <pre>
     * {@code
     * .impl(expMovingAverage())
     * // .impl(countScaling().factor(2)) // custom impl
     * }
     * </pre>
     */
    public void extendWith(CustomRateImplMaker<?> implMaker) {
        Class<? extends RateImplConfig> configType = configTypeForImplMakerClass(
            RateImplConfig.class,
            CustomRateImplMaker.class,
            implMaker.getClass());

        logCustomMetricImplRegistered("rate", configType, implMaker);
        customRateImplSpecs.put(configType, new CustomRateImplSpec<>(implMaker));
    }

    @SuppressWarnings("unchecked")
    public <C extends RateImplConfig> CustomRateImplSpec<C> customRateImplSpecFor(Class<C> configType) {
        return (CustomRateImplSpec<C>)customRateImplSpecs.get(configType);
    }

    /**
     * You can extend {@link DefaultMetricRegistry} with custom histogram implementations
     * (the CountAndTotalSumScalingHistogram* classes, the HistogramSample,
     * and the TimerSample classes can be found in the metrics-facade-samples module):
     * <ol>
     *   <li>Define an implementation type: {@code class I extends HistogramImpl}.
     *       <p>Example: {@code CountAndTotalSumScalingHistogramImpl}.
     *
     *   <li>Define an implementation configuration type: {@code C extends HistogramImplConfig}.
     *       <p>Example: {@code CountAndTotalSumScalingHistogramImplConfig}.
     *
     *   <li>Define an implementation configuration builder type: {@code CB extends HistogramImplConfigBuilder<C>}.
     *       <p>Example: {@code CountAndTotalSumScalingHistogramConfigBuilder}.
     *
     *   <li>Define an implementation maker type: {@code IM extends CustomHistogramImplMaker<C>}.
     *       <p>Example: {@code CountAndTotalSumScalingHistogramImplMaker}.
     *
     *   <li>Register the implementation maker type: {@code registry.extendWith(new IM())}.
     *       <p>Example: {@code registry.extendWith(new CountAndTotalSumScalingHistogramImplMaker())}.
     *       <p>You can also use automatic discovery of implementation maker types:
     * <pre>
     * {@code
     * DefaultMetricRegistry registry = new DefaultMetricRegistryBuilder()
     *   .withCustomMetricImplsFromPackages("package.to.scan.1", ...)
     *   .build();
     * }
     * </pre>
     * </ol>
     *
     * You can choose the specific implementation using the
     * {@link HistogramConfigBuilder#impl(MetricImplConfigBuilder)} method.
     * <p>See an example in the {@code HistogramSample} class:
     * <pre>
     * {@code
     * .impl(hdr()
     *   .resetByChunks(6, Duration.ofMinutes(2))
     *   .highestTrackableValue(1000, REDUCE_TO_HIGHEST_TRACKABLE)
     *   .significantDigits(3)
     *   .snapshotTtl(30, SECONDS))
     * // .impl(countAndTotalSumScaling().factor(2)) // custom impl
     * }
     * </pre>
     *
     * Another example can be seen in the {@code TimerSample} class:
     * <pre>
     * {@code
     * .impl(hdr()
     *   .resetByChunks(6, Duration.ofMinutes(2))
     *   .lowestDiscernibleValue(MILLISECONDS.toNanos(1))
     *   .highestTrackableValue(DAYS.toNanos(7), REDUCE_TO_HIGHEST_TRACKABLE)
     *   .significantDigits(2)
     *   .snapshotTtl(30, SECONDS))
     * // .impl(countAndTotalSumScaling().factor(2)) // custom impl
     * }
     * </pre>
     */
    public void extendWith(CustomHistogramImplMaker<?> implMaker) {
        Class<? extends HistogramImplConfig> configType = configTypeForImplMakerClass(
            HistogramImplConfig.class,
            CustomHistogramImplMaker.class,
            implMaker.getClass());

        logCustomMetricImplRegistered("histogram", configType, implMaker);
        customHistogramImplSpecs.put(configType, new CustomHistogramImplSpec<>(implMaker));
    }

    @SuppressWarnings("unchecked")
    public <C extends HistogramImplConfig> CustomHistogramImplSpec<C> customHistogramImplSpecFor(Class<C> configType) {
        return (CustomHistogramImplSpec<C>)customHistogramImplSpecs.get(configType);
    }

    @SuppressWarnings("rawtypes")
    private static <C extends MetricImplConfig> Class<C> configTypeForImplMakerClass(
        Class<? extends MetricImplConfig> implConfigInterface,
        Class<? extends CustomMetricImplMaker> implMakerInterface,
        Class<?> implMakerClass) {

        Class<C> configType;

        for (Type gi : implMakerClass.getGenericInterfaces()) {
            configType = configTypeForGenericType(implConfigInterface, implMakerInterface, gi);

            if (configType != null) {
                return configType;
            }
        }

        configType = configTypeForGenericType(implConfigInterface, implMakerInterface, implMakerClass.getGenericSuperclass());

        return
            configType != null ?
            configType :
            configTypeForImplMakerClass(implConfigInterface, implMakerInterface, implMakerClass.getSuperclass());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <C extends MetricImplConfig> Class<C> configTypeForGenericType(
        Class<? extends MetricImplConfig> implConfigInterface,
        Class<? extends CustomMetricImplMaker> implMakerInterface,
        Type genericType) {

        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)genericType;

            if (pt.getRawType() instanceof Class && implMakerInterface.isAssignableFrom((Class<?>)pt.getRawType())) {
                Type configType = configTypeForParametrizedType(implConfigInterface, pt);

                if (configType != null) {
                    return (Class<C>)configType;
                }
            }
        }

        return null;
    }

    private static Type configTypeForParametrizedType(
        Class<? extends MetricImplConfig> implConfigInterface,
        ParameterizedType parametrizedType) {

        return Arrays
            .stream(parametrizedType.getActualTypeArguments())
            .filter(typeArg -> typeArg instanceof Class && implConfigInterface.isAssignableFrom((Class<?>)typeArg))
            .findFirst().orElse(null);
    }

    private void logCustomMetricImplRegistered(
        String metricTypeName,
        Class<? extends MetricImplConfig> configType,
        CustomMetricImplMaker<?> implMaker) {

        logger.info(
            "Custom {} impl registered: config type = {}, impl maker type = {}",
            metricTypeName,
            configType.getName(),
            implMaker.getClass().getName());
    }
}