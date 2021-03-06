package com.ringcentral.platform.metrics.defaultImpl;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.counter.configs.CounterConfig;
import com.ringcentral.platform.metrics.defaultImpl.counter.DefaultCounter;
import com.ringcentral.platform.metrics.defaultImpl.histogram.*;
import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.HistogramImplConfig;
import com.ringcentral.platform.metrics.defaultImpl.rate.*;
import com.ringcentral.platform.metrics.defaultImpl.rate.configs.RateImplConfig;
import com.ringcentral.platform.metrics.defaultImpl.timer.DefaultTimer;
import com.ringcentral.platform.metrics.defaultImpl.var.doubleVar.*;
import com.ringcentral.platform.metrics.defaultImpl.var.longVar.*;
import com.ringcentral.platform.metrics.defaultImpl.var.objectVar.*;
import com.ringcentral.platform.metrics.defaultImpl.var.stringVar.*;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.histogram.configs.HistogramConfig;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.rate.configs.RateConfig;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.timer.configs.TimerConfig;
import com.ringcentral.platform.metrics.utils.TimeMsProvider;
import com.ringcentral.platform.metrics.var.configs.*;
import com.ringcentral.platform.metrics.var.doubleVar.*;
import com.ringcentral.platform.metrics.var.longVar.*;
import com.ringcentral.platform.metrics.var.objectVar.*;
import com.ringcentral.platform.metrics.var.stringVar.*;

import java.util.concurrent.*;
import java.util.function.Supplier;

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

    public <C extends HistogramImplConfig> void extendWith(Class<C> configType, CustomHistogramImplMaker<C> implMaker) {
        customHistogramImplSpecs.put(configType, new CustomHistogramImplSpec<>(implMaker));
    }

    @SuppressWarnings("unchecked")
    public <C extends HistogramImplConfig> CustomHistogramImplSpec<C> customHistogramImplSpecFor(Class<C> configType) {
        return (CustomHistogramImplSpec<C>)customHistogramImplSpecs.get(configType);
    }

    public <C extends RateImplConfig> void extendWith(Class<C> configType, CustomRateImplMaker<C> implMaker) {
        customRateImplSpecs.put(configType, new CustomRateImplSpec<>(implMaker));
    }

    @SuppressWarnings("unchecked")
    public <C extends RateImplConfig> CustomRateImplSpec<C> customRateImplSpecFor(Class<C> configType) {
        return (CustomRateImplSpec<C>)customRateImplSpecs.get(configType);
    }
}