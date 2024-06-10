package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.configs.builders.MetricConfigBuilder;
import com.ringcentral.platform.metrics.configs.builders.MetricConfigBuilderProvider;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.counter.configs.CounterConfig;
import com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.histogram.configs.HistogramConfig;
import com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder;
import com.ringcentral.platform.metrics.infoProviders.ConcurrentMaskTreeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.rate.configs.RateConfig;
import com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.timer.configs.TimerConfig;
import com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder;
import com.ringcentral.platform.metrics.utils.BasicThreadFactory;
import com.ringcentral.platform.metrics.utils.SystemTimeMsProvider;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.doubleVar.CachingDoubleVar;
import com.ringcentral.platform.metrics.var.doubleVar.DoubleVar;
import com.ringcentral.platform.metrics.var.doubleVar.configs.CachingDoubleVarConfig;
import com.ringcentral.platform.metrics.var.doubleVar.configs.DoubleVarConfig;
import com.ringcentral.platform.metrics.var.doubleVar.configs.builders.CachingDoubleVarConfigBuilder;
import com.ringcentral.platform.metrics.var.doubleVar.configs.builders.DoubleVarConfigBuilder;
import com.ringcentral.platform.metrics.var.longVar.CachingLongVar;
import com.ringcentral.platform.metrics.var.longVar.LongVar;
import com.ringcentral.platform.metrics.var.longVar.configs.CachingLongVarConfig;
import com.ringcentral.platform.metrics.var.longVar.configs.LongVarConfig;
import com.ringcentral.platform.metrics.var.longVar.configs.builders.CachingLongVarConfigBuilder;
import com.ringcentral.platform.metrics.var.longVar.configs.builders.LongVarConfigBuilder;
import com.ringcentral.platform.metrics.var.objectVar.CachingObjectVar;
import com.ringcentral.platform.metrics.var.objectVar.ObjectVar;
import com.ringcentral.platform.metrics.var.objectVar.configs.CachingObjectVarConfig;
import com.ringcentral.platform.metrics.var.objectVar.configs.ObjectVarConfig;
import com.ringcentral.platform.metrics.var.objectVar.configs.builders.CachingObjectVarConfigBuilder;
import com.ringcentral.platform.metrics.var.objectVar.configs.builders.ObjectVarConfigBuilder;
import com.ringcentral.platform.metrics.var.stringVar.CachingStringVar;
import com.ringcentral.platform.metrics.var.stringVar.StringVar;
import com.ringcentral.platform.metrics.var.stringVar.configs.CachingStringVarConfig;
import com.ringcentral.platform.metrics.var.stringVar.configs.StringVarConfig;
import com.ringcentral.platform.metrics.var.stringVar.configs.builders.CachingStringVarConfigBuilder;
import com.ringcentral.platform.metrics.var.stringVar.configs.builders.StringVarConfigBuilder;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.configs.builders.BaseMetricConfigBuilder.withMetric;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings({ "SameParameterValue", "WeakerAccess" })
public abstract class AbstractMetricRegistry implements MetricRegistry {

    protected interface MetricMaker {
        ObjectVar makeObjectVar(
            MetricName name,
            VarConfig config,
            Supplier<Object> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry);

        CachingObjectVar makeCachingObjectVar(
            MetricName name,
            CachingVarConfig config,
            Supplier<Object> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry);

        LongVar makeLongVar(
            MetricName name,
            VarConfig config,
            Supplier<Long> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry);

        CachingLongVar makeCachingLongVar(
            MetricName name,
            CachingVarConfig config,
            Supplier<Long> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry);

        DoubleVar makeDoubleVar(
            MetricName name,
            VarConfig config,
            Supplier<Double> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry);

        CachingDoubleVar makeCachingDoubleVar(
            MetricName name,
            CachingVarConfig config,
            Supplier<Double> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry);

        StringVar makeStringVar(
            MetricName name,
            VarConfig config,
            Supplier<String> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry);

        CachingStringVar makeCachingStringVar(
            MetricName name,
            CachingVarConfig config,
            Supplier<String> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry);

        Counter makeCounter(
            MetricName name,
            CounterConfig config,
            TimeMsProvider timeMsProvider,
            ScheduledExecutorService executor,
            MetricRegistry registry);

        Rate makeRate(
            MetricName name,
            RateConfig config,
            TimeMsProvider timeMsProvider,
            ScheduledExecutorService executor,
            MetricRegistry registry);

        Histogram makeHistogram(
            MetricName name,
            HistogramConfig config,
            TimeMsProvider timeMsProvider,
            ScheduledExecutorService executor,
            MetricRegistry registry);

        Timer makeTimer(
            MetricName name,
            TimerConfig config,
            TimeMsProvider timeMsProvider,
            ScheduledExecutorService executor,
            MetricRegistry registry);
    }

    static final Supplier<MetricConfigBuilderProvider<ObjectVarConfigBuilder>> DEFAULT_OBJECT_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER = ObjectVarConfigBuilder::new;
    static final Supplier<MetricConfigBuilderProvider<CachingObjectVarConfigBuilder>> DEFAULT_CACHING_OBJECT_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER = CachingObjectVarConfigBuilder::new;

    static final Supplier<MetricConfigBuilderProvider<LongVarConfigBuilder>> DEFAULT_LONG_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER = LongVarConfigBuilder::new;
    static final Supplier<MetricConfigBuilderProvider<CachingLongVarConfigBuilder>> DEFAULT_CACHING_LONG_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER = CachingLongVarConfigBuilder::new;

    static final Supplier<MetricConfigBuilderProvider<DoubleVarConfigBuilder>> DEFAULT_DOUBLE_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER = DoubleVarConfigBuilder::new;
    static final Supplier<MetricConfigBuilderProvider<CachingDoubleVarConfigBuilder>> DEFAULT_CACHING_DOUBLE_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER = CachingDoubleVarConfigBuilder::new;

    static final Supplier<MetricConfigBuilderProvider<StringVarConfigBuilder>> DEFAULT_STRING_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER = StringVarConfigBuilder::new;
    static final Supplier<MetricConfigBuilderProvider<CachingStringVarConfigBuilder>> DEFAULT_CACHING_STRING_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER = CachingStringVarConfigBuilder::new;

    static final Supplier<MetricConfigBuilderProvider<CounterConfigBuilder>> DEFAULT_COUNTER_CONFIG_BUILDER_PROVIDER_SUPPLIER = () -> CounterConfigBuilder::new;
    static final Supplier<MetricConfigBuilderProvider<RateConfigBuilder>> DEFAULT_RATE_CONFIG_BUILDER_PROVIDER_SUPPLIER = () -> RateConfigBuilder::new;
    static final Supplier<MetricConfigBuilderProvider<HistogramConfigBuilder>> DEFAULT_HISTOGRAM_CONFIG_BUILDER_PROVIDER_SUPPLIER = () -> HistogramConfigBuilder::new;
    static final Supplier<MetricConfigBuilderProvider<TimerConfigBuilder>> DEFAULT_TIMER_CONFIG_BUILDER_PROVIDER_SUPPLIER = () -> TimerConfigBuilder::new;

    private final MetricMaker metricMaker;
    private final CopyOnWriteArrayList<MetricRegistryListener> listeners = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<MetricKey, Metric> metrics = new ConcurrentHashMap<>();
    private final Map<MetricKey, Metric> unmodifiableMetrics = unmodifiableMap(metrics);
    private final PredicativeMetricNamedInfoProvider<MetricMod> preModsProvider;
    private final PredicativeMetricNamedInfoProvider<MetricMod> postModsProvider;
    private final TimeMsProvider timeMsProvider;
    private final ScheduledExecutorService executor;

    private static final Logger logger = getLogger(AbstractMetricRegistry.class);

    protected AbstractMetricRegistry(MetricMaker metricMaker) {
        this(metricMaker, makeDefaultExecutor());
    }

    /**
     * @param executor MUST be single-threaded.
     */
    protected AbstractMetricRegistry(MetricMaker metricMaker, ScheduledExecutorService executor) {
        this(
            metricMaker,
            makeDefaultModsProvider(),
            makeDefaultModsProvider(),
            executor);
    }

    protected AbstractMetricRegistry(
        MetricMaker metricMaker,
        PredicativeMetricNamedInfoProvider<MetricMod> preModsProvider,
        PredicativeMetricNamedInfoProvider<MetricMod> postModsProvider) {

        this(
            metricMaker,
            preModsProvider,
            postModsProvider,
            makeDefaultExecutor());
    }

    /**
     * @param executor MUST be single-threaded.
     */
    protected AbstractMetricRegistry(
        MetricMaker metricMaker,
        PredicativeMetricNamedInfoProvider<MetricMod> preModsProvider,
        PredicativeMetricNamedInfoProvider<MetricMod> postModsProvider,
        ScheduledExecutorService executor) {

        this(
            metricMaker,
            preModsProvider,
            postModsProvider,
            SystemTimeMsProvider.INSTANCE,
            executor);
    }

    /**
     * @param executor MUST be single-threaded.
     */
    protected AbstractMetricRegistry(
        MetricMaker metricMaker,
        PredicativeMetricNamedInfoProvider<MetricMod> preModsProvider,
        PredicativeMetricNamedInfoProvider<MetricMod> postModsProvider,
        TimeMsProvider timeMsProvider,
        ScheduledExecutorService executor) {

        this.metricMaker = requireNonNull(metricMaker);
        this.preModsProvider = requireNonNull(preModsProvider);
        this.postModsProvider = requireNonNull(postModsProvider);
        this.timeMsProvider = requireNonNull(timeMsProvider);
        this.executor = requireNonNull(executor);
    }

    public static ScheduledExecutorService makeDefaultExecutor() {
        return newSingleThreadScheduledExecutor(new BasicThreadFactory("metric-registry-executor-%d", true));
    }

    public static PredicativeMetricNamedInfoProvider<MetricMod> makeDefaultModsProvider() {
        return new ConcurrentMaskTreeMetricNamedInfoProvider<>();
    }

    @Override
    public synchronized void addListener(MetricRegistryListener listener) {
        listeners.add(listener);
        metrics.values().forEach(m -> notifyMetricAdded(listener, m.name(), m));
    }

    @Override
    public Map<MetricKey, Metric> metrics() {
        return unmodifiableMetrics;
    }

    @Override
    public synchronized void remove(MetricKey key) {
        Metric removedMetric = metrics.remove(key);

        if (removedMetric != null) {
            notifyMetricRemoved(key, removedMetric);
        }
    }

    @Override
    public synchronized void preConfigure(MetricNamedPredicate predicate, MetricModBuilder modBuilder) {
        preModsProvider.addInfo(predicate, modBuilder.build());
    }

    @Override
    public synchronized void postConfigure(MetricNamedPredicate predicate, MetricModBuilder modBuilder) {
        postModsProvider.addInfo(predicate, modBuilder.build());
    }

    /* Object var */

    @Override
    public ObjectVar objectVar(MetricKey key, Supplier<Object> valueSupplier) {
        return objectVar(
            key,
            valueSupplier,
            DEFAULT_OBJECT_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends ObjectVarConfig> ObjectVar objectVar(
        MetricKey key,
        Supplier<Object> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return getOrAddMetric(
            key,
            ObjectVar.class,
            () -> buildObjectVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    protected <C extends ObjectVarConfig> ObjectVar buildObjectVar(
        MetricKey key,
        Supplier<Object> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return metricMaker.makeObjectVar(
            key.name(),
            prepareVarConfigBuilder(key, configBuilderProviderSupplier).build(),
            valueSupplier,
            executor,
            this);
    }

    private <C extends VarConfig> MetricConfigBuilder<C> prepareVarConfigBuilder(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        MetricConfigBuilder<C> builder = configBuilderProviderSupplier.get().builder();
        List<MetricMod> preMods = preModsProvider.infosFor(key);
        List<MetricMod> postMods = postModsProvider.infosFor(key);

        if (!preMods.isEmpty()) {
            for (int i = preMods.size() - 1; i >= 0; --i) {
                MetricMod preMod = preMods.get(i);

                if (preMod.hasVarConfigBuilder()) {
                    builder.rebase(preMod.varConfigBuilder());
                } else if (preMod.hasMetricConfigBuilder()) {
                    builder.rebase(preMod.metricConfigBuilder());
                }
            }
        }

        if (!postMods.isEmpty()) {
            for (MetricMod postMod : postMods) {
                if (postMod.hasVarConfigBuilder()) {
                    builder.modify(postMod.varConfigBuilder());
                } else if (postMod.hasMetricConfigBuilder()) {
                    builder.modify(postMod.metricConfigBuilder());
                }
            }
        }

        replacePrefixLabelValues(key, builder);
        return builder;
    }

    @Override
    public synchronized ObjectVar newObjectVar(MetricKey key, Supplier<Object> valueSupplier) {
        return newObjectVar(
            key,
            valueSupplier,
            DEFAULT_OBJECT_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends ObjectVarConfig> ObjectVar newObjectVar(
        MetricKey key,
        Supplier<Object> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return replaceOrAddMetric(
            key,
            ObjectVar.class,
            buildObjectVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    /* Caching object var */

    @Override
    public CachingObjectVar cachingObjectVar(MetricKey key, Supplier<Object> valueSupplier) {
        return cachingObjectVar(
            key,
            valueSupplier,
            DEFAULT_CACHING_OBJECT_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends CachingObjectVarConfig> CachingObjectVar cachingObjectVar(
        MetricKey key,
        Supplier<Object> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return getOrAddMetric(
            key,
            CachingObjectVar.class,
            () -> buildCachingObjectVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    protected <C extends CachingObjectVarConfig> CachingObjectVar buildCachingObjectVar(
        MetricKey key,
        Supplier<Object> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return metricMaker.makeCachingObjectVar(
            key.name(),
            prepareCachingVarConfigBuilder(key, configBuilderProviderSupplier).build(),
            valueSupplier,
            executor,
            this);
    }

    private <C extends CachingVarConfig> MetricConfigBuilder<C> prepareCachingVarConfigBuilder(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        MetricConfigBuilder<C> builder = configBuilderProviderSupplier.get().builder();
        List<MetricMod> preMods = preModsProvider.infosFor(key);
        List<MetricMod> postMods = postModsProvider.infosFor(key);

        if (!preMods.isEmpty()) {
            for (int i = preMods.size() - 1; i >= 0; --i) {
                MetricMod preMod = preMods.get(i);

                if (preMod.hasCachingVarConfigBuilder()) {
                    builder.rebase(preMod.cachingVarConfigBuilder());
                } else if (preMod.hasVarConfigBuilder()) {
                    builder.rebase(preMod.varConfigBuilder());
                } else if (preMod.hasMetricConfigBuilder()) {
                    builder.rebase(preMod.metricConfigBuilder());
                }
            }
        }

        if (!postMods.isEmpty()) {
            for (MetricMod postMod : postMods) {
                if (postMod.hasCachingVarConfigBuilder()) {
                    builder.modify(postMod.cachingVarConfigBuilder());
                } else if (postMod.hasVarConfigBuilder()) {
                    builder.modify(postMod.varConfigBuilder());
                } else if (postMod.hasMetricConfigBuilder()) {
                    builder.modify(postMod.metricConfigBuilder());
                }
            }
        }

        replacePrefixLabelValues(key, builder);
        return builder;
    }

    @Override
    public CachingObjectVar newCachingObjectVar(MetricKey key, Supplier<Object> valueSupplier) {
        return newCachingObjectVar(
            key,
            valueSupplier,
            DEFAULT_CACHING_OBJECT_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public synchronized <C extends CachingObjectVarConfig> CachingObjectVar newCachingObjectVar(
        MetricKey key,
        Supplier<Object> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return replaceOrAddMetric(
            key,
            CachingObjectVar.class,
            buildCachingObjectVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    /* Long var */

    @Override
    public LongVar longVar(MetricKey key, Supplier<Long> valueSupplier) {
        return longVar(
            key,
            valueSupplier,
            DEFAULT_LONG_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends LongVarConfig> LongVar longVar(
        MetricKey key,
        Supplier<Long> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return getOrAddMetric(
            key,
            LongVar.class,
            () -> buildLongVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    protected <C extends LongVarConfig> LongVar buildLongVar(
        MetricKey key,
        Supplier<Long> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return metricMaker.makeLongVar(
            key.name(),
            prepareVarConfigBuilder(key, configBuilderProviderSupplier).build(),
            valueSupplier,
            executor,
            this);
    }

    @Override
    public synchronized LongVar newLongVar(MetricKey key, Supplier<Long> valueSupplier) {
        return newLongVar(
            key,
            valueSupplier,
            DEFAULT_LONG_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends LongVarConfig> LongVar newLongVar(
        MetricKey key,
        Supplier<Long> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return replaceOrAddMetric(
            key,
            LongVar.class,
            buildLongVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    /* Caching long var */

    @Override
    public CachingLongVar cachingLongVar(MetricKey key, Supplier<Long> valueSupplier) {
        return cachingLongVar(
            key,
            valueSupplier,
            DEFAULT_CACHING_LONG_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends CachingLongVarConfig> CachingLongVar cachingLongVar(
        MetricKey key,
        Supplier<Long> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return getOrAddMetric(
            key,
            CachingLongVar.class,
            () -> buildCachingLongVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    protected <C extends CachingLongVarConfig> CachingLongVar buildCachingLongVar(
        MetricKey key,
        Supplier<Long> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return metricMaker.makeCachingLongVar(
            key.name(),
            prepareCachingVarConfigBuilder(key, configBuilderProviderSupplier).build(),
            valueSupplier,
            executor,
            this);
    }

    @Override
    public CachingLongVar newCachingLongVar(MetricKey key, Supplier<Long> valueSupplier) {
        return newCachingLongVar(
            key,
            valueSupplier,
            DEFAULT_CACHING_LONG_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public synchronized <C extends CachingLongVarConfig> CachingLongVar newCachingLongVar(
        MetricKey key,
        Supplier<Long> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return replaceOrAddMetric(
            key,
            CachingLongVar.class,
            buildCachingLongVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    /* Double var */

    @Override
    public DoubleVar doubleVar(MetricKey key, Supplier<Double> valueSupplier) {
        return doubleVar(
            key,
            valueSupplier,
            DEFAULT_DOUBLE_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends DoubleVarConfig> DoubleVar doubleVar(
        MetricKey key,
        Supplier<Double> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return getOrAddMetric(
            key,
            DoubleVar.class,
            () -> buildDoubleVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    protected <C extends DoubleVarConfig> DoubleVar buildDoubleVar(
        MetricKey key,
        Supplier<Double> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return metricMaker.makeDoubleVar(
            key.name(),
            prepareVarConfigBuilder(key, configBuilderProviderSupplier).build(),
            valueSupplier,
            executor,
            this);
    }

    @Override
    public synchronized DoubleVar newDoubleVar(MetricKey key, Supplier<Double> valueSupplier) {
        return newDoubleVar(
            key,
            valueSupplier,
            DEFAULT_DOUBLE_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends DoubleVarConfig> DoubleVar newDoubleVar(
        MetricKey key,
        Supplier<Double> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return replaceOrAddMetric(
            key,
            DoubleVar.class,
            buildDoubleVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    /* Caching double var */

    @Override
    public CachingDoubleVar cachingDoubleVar(MetricKey key, Supplier<Double> valueSupplier) {
        return cachingDoubleVar(
            key,
            valueSupplier,
            DEFAULT_CACHING_DOUBLE_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends CachingDoubleVarConfig> CachingDoubleVar cachingDoubleVar(
        MetricKey key,
        Supplier<Double> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return getOrAddMetric(
            key,
            CachingDoubleVar.class,
            () -> buildCachingDoubleVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    protected <C extends CachingDoubleVarConfig> CachingDoubleVar buildCachingDoubleVar(
        MetricKey key,
        Supplier<Double> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return metricMaker.makeCachingDoubleVar(
            key.name(),
            prepareCachingVarConfigBuilder(key, configBuilderProviderSupplier).build(),
            valueSupplier,
            executor,
            this);
    }

    @Override
    public CachingDoubleVar newCachingDoubleVar(MetricKey key, Supplier<Double> valueSupplier) {
        return newCachingDoubleVar(
            key,
            valueSupplier,
            DEFAULT_CACHING_DOUBLE_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public synchronized <C extends CachingDoubleVarConfig> CachingDoubleVar newCachingDoubleVar(
        MetricKey key,
        Supplier<Double> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return replaceOrAddMetric(
            key,
            CachingDoubleVar.class,
            buildCachingDoubleVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    /* String var */

    @Override
    public StringVar stringVar(MetricKey key, Supplier<String> valueSupplier) {
        return stringVar(
            key,
            valueSupplier,
            DEFAULT_STRING_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends StringVarConfig> StringVar stringVar(
        MetricKey key,
        Supplier<String> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return getOrAddMetric(
            key,
            StringVar.class,
            () -> buildStringVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    protected <C extends StringVarConfig> StringVar buildStringVar(
        MetricKey key,
        Supplier<String> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return metricMaker.makeStringVar(
            key.name(),
            prepareVarConfigBuilder(key, configBuilderProviderSupplier).build(),
            valueSupplier,
            executor,
            this);
    }

    @Override
    public synchronized StringVar newStringVar(MetricKey key, Supplier<String> valueSupplier) {
        return newStringVar(
            key,
            valueSupplier,
            DEFAULT_STRING_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends StringVarConfig> StringVar newStringVar(
        MetricKey key,
        Supplier<String> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return replaceOrAddMetric(
            key,
            StringVar.class,
            buildStringVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    /* Caching string var */

    @Override
    public CachingStringVar cachingStringVar(MetricKey key, Supplier<String> valueSupplier) {
        return cachingStringVar(
            key,
            valueSupplier,
            DEFAULT_CACHING_STRING_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends CachingStringVarConfig> CachingStringVar cachingStringVar(
        MetricKey key,
        Supplier<String> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return getOrAddMetric(
            key,
            CachingStringVar.class,
            () -> buildCachingStringVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    protected <C extends CachingStringVarConfig> CachingStringVar buildCachingStringVar(
        MetricKey key,
        Supplier<String> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return metricMaker.makeCachingStringVar(
            key.name(),
            prepareCachingVarConfigBuilder(key, configBuilderProviderSupplier).build(),
            valueSupplier,
            executor,
            this);
    }

    @Override
    public CachingStringVar newCachingStringVar(MetricKey key, Supplier<String> valueSupplier) {
        return newCachingStringVar(
            key,
            valueSupplier,
            DEFAULT_CACHING_STRING_VAR_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public synchronized <C extends CachingStringVarConfig> CachingStringVar newCachingStringVar(
        MetricKey key,
        Supplier<String> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return replaceOrAddMetric(
            key,
            CachingStringVar.class,
            buildCachingStringVar(key, valueSupplier, configBuilderProviderSupplier));
    }

    /* Counter */

    @Override
    public Counter counter(MetricKey key) {
        return counter(key, DEFAULT_COUNTER_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends CounterConfig> Counter counter(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return getOrAddMetric(
            key,
            Counter.class,
            () -> buildCounter(key, configBuilderProviderSupplier));
    }

    protected <C extends CounterConfig> Counter buildCounter(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        MetricConfigBuilder<? extends CounterConfig> builder = configBuilderProviderSupplier.get().builder();
        List<MetricMod> preMods = preModsProvider.infosFor(key);
        List<MetricMod> postMods = postModsProvider.infosFor(key);

        if (!preMods.isEmpty()) {
            for (int i = preMods.size() - 1; i >= 0; --i) {
                MetricMod preMod = preMods.get(i);

                if (preMod.hasCounterConfigBuilder()) {
                    builder.rebase(preMod.counterConfigBuilder());
                } else if (preMod.hasMeterConfigBuilder()) {
                    builder.rebase(preMod.meterConfigBuilder());
                } else if (preMod.hasMetricConfigBuilder()) {
                    builder.rebase(preMod.metricConfigBuilder());
                }
            }
        }

        if (!postMods.isEmpty()) {
            for (MetricMod postMod : postMods) {
                if (postMod.hasCounterConfigBuilder()) {
                    builder.modify(postMod.counterConfigBuilder());
                } else if (postMod.hasMeterConfigBuilder()) {
                    builder.modify(postMod.meterConfigBuilder());
                } else if (postMod.hasMetricConfigBuilder()) {
                    builder.modify(postMod.metricConfigBuilder());
                }
            }
        }

        replacePrefixLabelValues(key, builder);

        return metricMaker.makeCounter(
            key.name(),
            builder.build(),
            timeMsProvider,
            executor,
            this);
    }

    @Override
    public Counter newCounter(MetricKey key) {
        return newCounter(key, DEFAULT_COUNTER_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public synchronized <C extends CounterConfig> Counter newCounter(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return replaceOrAddMetric(
            key,
            Counter.class,
            buildCounter(key, configBuilderProviderSupplier));
    }

    /* Rate */

    @Override
    public Rate rate(MetricKey key) {
        return rate(key, DEFAULT_RATE_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends RateConfig> Rate rate(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return getOrAddMetric(
            key,
            Rate.class,
            () -> buildRate(key, configBuilderProviderSupplier));
    }

    protected <C extends RateConfig> Rate buildRate(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        MetricConfigBuilder<? extends RateConfig> builder = configBuilderProviderSupplier.get().builder();
        List<MetricMod> preMods = preModsProvider.infosFor(key);
        List<MetricMod> postMods = postModsProvider.infosFor(key);

        if (!preMods.isEmpty()) {
            for (int i = preMods.size() - 1; i >= 0; --i) {
                MetricMod preMod = preMods.get(i);

                if (preMod.hasRateConfigBuilder()) {
                    builder.rebase(preMod.rateConfigBuilder());
                } else if (preMod.hasMeterConfigBuilder()) {
                    builder.rebase(preMod.meterConfigBuilder());
                } else if (preMod.hasMetricConfigBuilder()) {
                    builder.rebase(preMod.metricConfigBuilder());
                }
            }
        }

        if (!postMods.isEmpty()) {
            for (MetricMod postMod : postMods) {
                if (postMod.hasRateConfigBuilder()) {
                    builder.modify(postMod.rateConfigBuilder());
                } else if (postMod.hasMeterConfigBuilder()) {
                    builder.modify(postMod.meterConfigBuilder());
                } else if (postMod.hasMetricConfigBuilder()) {
                    builder.modify(postMod.metricConfigBuilder());
                }
            }
        }

        replacePrefixLabelValues(key, builder);

        return metricMaker.makeRate(
            key.name(),
            builder.build(),
            timeMsProvider,
            executor,
            this);
    }

    @Override
    public Rate newRate(MetricKey key) {
        return newRate(key, DEFAULT_RATE_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public synchronized <C extends RateConfig> Rate newRate(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return replaceOrAddMetric(
            key,
            Rate.class,
            buildRate(key, configBuilderProviderSupplier));
    }

    /* Histogram */

    @Override
    public Histogram histogram(MetricKey key) {
        return histogram(key, DEFAULT_HISTOGRAM_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends HistogramConfig> Histogram histogram(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return getOrAddMetric(
            key,
            Histogram.class,
            () -> buildHistogram(key, configBuilderProviderSupplier));
    }

    protected <C extends HistogramConfig> Histogram buildHistogram(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        MetricConfigBuilder<? extends HistogramConfig> builder = configBuilderProviderSupplier.get().builder();
        List<MetricMod> preMods = preModsProvider.infosFor(key);
        List<MetricMod> postMods = postModsProvider.infosFor(key);

        if (!preMods.isEmpty()) {
            for (int i = preMods.size() - 1; i >= 0; --i) {
                MetricMod preMod = preMods.get(i);

                if (preMod.hasHistogramConfigBuilder()) {
                    builder.rebase(preMod.histogramConfigBuilder());
                } else if (preMod.hasMeterConfigBuilder()) {
                    builder.rebase(preMod.meterConfigBuilder());
                } else if (preMod.hasMetricConfigBuilder()) {
                    builder.rebase(preMod.metricConfigBuilder());
                }
            }
        }

        if (!postMods.isEmpty()) {
            for (MetricMod postMod : postMods) {
                if (postMod.hasHistogramConfigBuilder()) {
                    builder.modify(postMod.histogramConfigBuilder());
                } else if (postMod.hasMeterConfigBuilder()) {
                    builder.modify(postMod.meterConfigBuilder());
                } else if (postMod.hasMetricConfigBuilder()) {
                    builder.modify(postMod.metricConfigBuilder());
                }
            }
        }

        replacePrefixLabelValues(key, builder);

        return metricMaker.makeHistogram(
            key.name(),
            builder.build(),
            timeMsProvider,
            executor,
            this);
    }

    @Override
    public Histogram newHistogram(MetricKey key) {
        return newHistogram(key, DEFAULT_HISTOGRAM_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public synchronized <C extends HistogramConfig> Histogram newHistogram(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return replaceOrAddMetric(
            key,
            Histogram.class,
            buildHistogram(key, configBuilderProviderSupplier));
    }

    /* Timer */

    @Override
    public Timer timer(MetricKey key) {
        return timer(key, DEFAULT_TIMER_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public <C extends TimerConfig> Timer timer(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return getOrAddMetric(
            key,
            Timer.class,
            () -> buildTimer(key, configBuilderProviderSupplier));
    }

    protected <C extends TimerConfig> Timer buildTimer(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        MetricConfigBuilder<? extends TimerConfig> builder = configBuilderProviderSupplier.get().builder();
        List<MetricMod> preMods = preModsProvider.infosFor(key);
        List<MetricMod> postMods = postModsProvider.infosFor(key);

        if (!preMods.isEmpty()) {
            for (int i = preMods.size() - 1; i >= 0; --i) {
                MetricMod preMod = preMods.get(i);

                if (preMod.hasTimerConfigBuilder()) {
                    builder.rebase(preMod.timerConfigBuilder());
                } else if (preMod.hasMeterConfigBuilder()) {
                    builder.rebase(preMod.meterConfigBuilder());
                } else if (preMod.hasMetricConfigBuilder()) {
                    builder.rebase(preMod.metricConfigBuilder());
                }
            }
        }

        if (!postMods.isEmpty()) {
            for (MetricMod postMod : postMods) {
                if (postMod.hasTimerConfigBuilder()) {
                    builder.modify(postMod.timerConfigBuilder());
                } else if (postMod.hasMeterConfigBuilder()) {
                    builder.modify(postMod.meterConfigBuilder());
                } else if (postMod.hasMetricConfigBuilder()) {
                    builder.modify(postMod.metricConfigBuilder());
                }
            }
        }

        replacePrefixLabelValues(key, builder);

        return metricMaker.makeTimer(
            key.name(),
            builder.build(),
            timeMsProvider,
            executor,
            this);
    }

    @Override
    public Timer newTimer(MetricKey key) {
        return newTimer(key, DEFAULT_TIMER_CONFIG_BUILDER_PROVIDER_SUPPLIER);
    }

    @Override
    public synchronized <C extends TimerConfig> Timer newTimer(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier) {

        return replaceOrAddMetric(
            key,
            Timer.class,
            buildTimer(key, configBuilderProviderSupplier));
    }

    @SuppressWarnings("unchecked")
    protected synchronized <M extends Metric> M getOrAddMetric(MetricKey key, Class<M> type, Supplier<M> supplier) {
        Metric existing = metrics.get(key);

        if (existing != null) {
            if (type.isAssignableFrom(existing.getClass())) {
                return (M)existing;
            } else {
                throw new MetricCollisionException(
                    key.name(),
                    existing.getClass().getName(),
                    type.getName());
            }
        } else {
            M metric = supplier.get();
            metrics.put(key, metric);
            notifyMetricAdded(key, metric);
            return metric;
        }
    }

    protected synchronized <M extends Metric> M replaceOrAddMetric(MetricKey key, Class<M> type, M metric) {
        Metric existing = metrics.get(key);

        if (existing != null) {
            if (type.isAssignableFrom(existing.getClass())) {
                notifyMetricRemoved(key, existing);
            } else {
                throw new MetricCollisionException(
                    key.name(),
                    existing.getClass().getName(),
                    type.getName());
            }
        }

        metrics.put(key, metric);
        notifyMetricAdded(key, metric);
        return metric;
    }

    private void notifyMetricAdded(MetricKey key, Metric metric) {
        metric.metricAdded();
        listeners.forEach(listener -> notifyMetricAdded(listener, key, metric));
    }

    private void notifyMetricAdded(MetricRegistryListener listener, MetricKey key, Metric metric) {
        try {
            if (isCachingObjectVar(metric)) {
                listener.cachingObjectVarAdded((CachingObjectVar)metric);
            } else if (isObjectVar(metric)) {
                listener.objectVarAdded((ObjectVar)metric);
            } else if (isCachingLongVar(metric)) {
                listener.cachingLongVarAdded((CachingLongVar)metric);
            } else if (isLongVar(metric)) {
                listener.longVarAdded((LongVar)metric);
            } else if (isCachingDoubleVar(metric)) {
                listener.cachingDoubleVarAdded((CachingDoubleVar)metric);
            } else if (isDoubleVar(metric)) {
                listener.doubleVarAdded((DoubleVar)metric);
            } else if (isCachingStringVar(metric)) {
                listener.cachingStringVarAdded((CachingStringVar)metric);
            } else if (isStringVar(metric)) {
                listener.stringVarAdded((StringVar)metric);
            } else if (isCounter(metric)) {
                listener.counterAdded((Counter)metric);
            } else if (isRate(metric)) {
                listener.rateAdded((Rate)metric);
            } else if (isHistogram(metric)) {
                listener.histogramAdded((Histogram)metric);
            } else if (isTimer(metric)) {
                listener.timerAdded((Timer)metric);
            } else {
                throw new IllegalArgumentException("Unknown metric type: {}" + metric.getClass().getName());
            }
        } catch (Exception e) {
            logger.error(
                "Failed to notify listener {} that metric {} added",
                listener.getClass().getName(),
                key.name(),
                e);
        }
    }

    private boolean isObjectVar(Metric metric) {
        return metric instanceof ObjectVar;
    }

    private boolean isCachingObjectVar(Metric metric) {
        return metric instanceof CachingObjectVar;
    }

    private boolean isLongVar(Metric metric) {
        return metric instanceof LongVar;
    }

    private boolean isCachingLongVar(Metric metric) {
        return metric instanceof CachingLongVar;
    }

    private boolean isDoubleVar(Metric metric) {
        return metric instanceof DoubleVar;
    }

    private boolean isCachingDoubleVar(Metric metric) {
        return metric instanceof CachingDoubleVar;
    }

    private boolean isStringVar(Metric metric) {
        return metric instanceof StringVar;
    }

    private boolean isCachingStringVar(Metric metric) {
        return metric instanceof CachingStringVar;
    }

    private boolean isCounter(Metric metric) {
        return metric instanceof Counter;
    }

    private boolean isRate(Metric metric) {
        return metric instanceof Rate;
    }

    private boolean isHistogram(Metric metric) {
        return metric instanceof Histogram;
    }

    private boolean isTimer(Metric metric) {
        return metric instanceof Timer;
    }

    private void notifyMetricRemoved(MetricKey key, Metric metric) {
        metric.metricRemoved();
        listeners.forEach(listener -> notifyMetricRemoved(listener, key, metric));
    }

    private void notifyMetricRemoved(MetricRegistryListener listener, MetricKey key, Metric metric) {
        try {
            if (isCachingObjectVar(metric)) {
                listener.cachingObjectVarRemoved((CachingObjectVar)metric);
            } else if (isObjectVar(metric)) {
                listener.objectVarRemoved((ObjectVar)metric);
            } else if (isCachingLongVar(metric)) {
                listener.cachingLongVarRemoved((CachingLongVar)metric);
            } else if (isLongVar(metric)) {
                listener.longVarRemoved((LongVar)metric);
            } else if (isCachingDoubleVar(metric)) {
                listener.cachingDoubleVarRemoved((CachingDoubleVar)metric);
            } else if (isDoubleVar(metric)) {
                listener.doubleVarRemoved((DoubleVar)metric);
            } else if (isCachingStringVar(metric)) {
                listener.cachingStringVarRemoved((CachingStringVar)metric);
            } else if (isStringVar(metric)) {
                listener.stringVarRemoved((StringVar)metric);
            } else if (isCounter(metric)) {
                listener.counterRemoved((Counter)metric);
            } else if (isRate(metric)) {
                listener.rateRemoved((Rate)metric);
            } else if (isHistogram(metric)) {
                listener.histogramRemoved((Histogram)metric);
            } else if (isTimer(metric)) {
                listener.timerRemoved((Timer)metric);
            } else {
                throw new IllegalArgumentException("Unknown metric type: " + metric.getClass().getName());
            }
        } catch (Exception e) {
            logger.error(
                "Failed to notify listener {} that metric {} removed",
                listener.getClass().getName(),
                key.name(),
                e);
        }
    }

    private void replacePrefixLabelValues(MetricKey key, MetricConfigBuilder<?> builder) {
        if (key instanceof PrefixLabelValuesMetricKey) {
            LabelValues labelValues = ((PrefixLabelValuesMetricKey)key).labelValues();
            builder.modify(withMetric().prefix(labelValues));
        }
    }
}