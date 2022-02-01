package com.ringcentral.platform.metrics.x;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.counter.configs.CounterConfig;
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
import com.ringcentral.platform.metrics.x.counter.XCounter;
import com.ringcentral.platform.metrics.x.histogram.*;
import com.ringcentral.platform.metrics.x.histogram.configs.XHistogramImplConfig;
import com.ringcentral.platform.metrics.x.rate.*;
import com.ringcentral.platform.metrics.x.rate.configs.XRateImplConfig;
import com.ringcentral.platform.metrics.x.timer.XTimer;
import com.ringcentral.platform.metrics.x.var.doubleVar.*;
import com.ringcentral.platform.metrics.x.var.longVar.*;
import com.ringcentral.platform.metrics.x.var.objectVar.*;
import com.ringcentral.platform.metrics.x.var.stringVar.*;

import java.util.concurrent.*;
import java.util.function.Supplier;

@SuppressWarnings("SameParameterValue")
public class XMetricRegistry extends AbstractMetricRegistry {

    public static class MetricMakerImpl implements MetricMaker {

        public static final MetricMakerImpl INSTANCE = new MetricMakerImpl();

        @Override
        public ObjectVar makeObjectVar(
            MetricName name,
            VarConfig config,
            Supplier<Object> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new XObjectVar(
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

            return new XCachingObjectVar(
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

            return new XLongVar(
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

            return new XCachingLongVar(
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

            return new XDoubleVar(
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

            return new XCachingDoubleVar(
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

            return new XStringVar(
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

            return new XCachingStringVar(
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

            return new XCounter(
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

            return new XRate(
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

            return new XHistogram(
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

            return new XTimer(
                name,
                config,
                timeMsProvider,
                executor,
                registry);
        }
    }

    private final ConcurrentMap<Class<? extends XRateImplConfig>, CustomXRateImplMaker<?>> customXRateImplMakers = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends XHistogramImplConfig>, CustomXHistogramImplMaker<?>> customXHistogramImplMakers = new ConcurrentHashMap<>();

    public XMetricRegistry() {
        super(MetricMakerImpl.INSTANCE);
    }

    public XMetricRegistry(ScheduledExecutorService executor) {
        super(MetricMakerImpl.INSTANCE, executor);
    }

    public XMetricRegistry(
        PredicativeMetricNamedInfoProvider<MetricMod> preModsProvider,
        PredicativeMetricNamedInfoProvider<MetricMod> postModsProvider) {

        super(
            MetricMakerImpl.INSTANCE,
            preModsProvider,
            postModsProvider);
    }

    public XMetricRegistry(
        PredicativeMetricNamedInfoProvider<MetricMod> preModsProvider,
        PredicativeMetricNamedInfoProvider<MetricMod> postModsProvider,
        ScheduledExecutorService executor) {

        super(
            MetricMakerImpl.INSTANCE,
            preModsProvider,
            postModsProvider,
            executor);
    }

    public XMetricRegistry(
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

    /* ****************************** */

    public <C extends XHistogramImplConfig> void extendWith(Class<C> configType, CustomXHistogramImplMaker<C> implMaker) {
        customXHistogramImplMakers.put(configType, implMaker);
    }

    @SuppressWarnings("unchecked")
    public <C extends XHistogramImplConfig> CustomXHistogramImplMaker<C> customXHistogramImplMakerFor(Class<C> configType) {
        return (CustomXHistogramImplMaker<C>)customXHistogramImplMakers.get(configType);
    }

    public <C extends XRateImplConfig> void extendWith(Class<C> configType, CustomXRateImplMaker<C> implMaker) {
        customXRateImplMakers.put(configType, implMaker);
    }

    @SuppressWarnings("unchecked")
    public <C extends XRateImplConfig> CustomXRateImplMaker<C> customXRateImplMakerFor(Class<C> configType) {
        return (CustomXRateImplMaker<C>)customXRateImplMakers.get(configType);
    }
}