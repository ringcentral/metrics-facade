package com.ringcentral.platform.metrics.dropwizard;

import com.codahale.metrics.Metric;
import com.codahale.metrics.*;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.counter.configs.CounterConfig;
import com.ringcentral.platform.metrics.dropwizard.counter.DropwizardCounter;
import com.ringcentral.platform.metrics.dropwizard.histogram.DropwizardHistogram;
import com.ringcentral.platform.metrics.dropwizard.rate.DropwizardRate;
import com.ringcentral.platform.metrics.dropwizard.timer.DropwizardTimer;
import com.ringcentral.platform.metrics.dropwizard.var.doubleVar.*;
import com.ringcentral.platform.metrics.dropwizard.var.longVar.*;
import com.ringcentral.platform.metrics.dropwizard.var.objectVar.*;
import com.ringcentral.platform.metrics.dropwizard.var.stringVar.*;
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

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;
import static com.ringcentral.platform.metrics.counter.configs.builders.CounterInstanceConfigBuilder.counterInstance;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramInstanceConfigBuilder.histogramInstance;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateConfigBuilder.withRate;
import static com.ringcentral.platform.metrics.rate.configs.builders.RateInstanceConfigBuilder.rateInstance;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerConfigBuilder.withTimer;
import static com.ringcentral.platform.metrics.timer.configs.builders.TimerInstanceConfigBuilder.timerInstance;
import static org.apache.commons.lang3.StringUtils.split;

@SuppressWarnings("SameParameterValue")
public class DropwizardMetricRegistry extends AbstractMetricRegistry {

    public static class MetricMakerImpl implements MetricMaker {

        public static final MetricMakerImpl INSTANCE = new MetricMakerImpl();

        @Override
        public ObjectVar makeObjectVar(
            MetricName name,
            VarConfig config,
            Supplier<Object> valueSupplier,
            ScheduledExecutorService executor,
            MetricRegistry registry) {

            return new DropwizardObjectVar(
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

            return new DropwizardCachingObjectVar(
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

            return new DropwizardLongVar(
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

            return new DropwizardCachingLongVar(
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

            return new DropwizardDoubleVar(
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

            return new DropwizardCachingDoubleVar(
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

            return new DropwizardStringVar(
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

            return new DropwizardCachingStringVar(
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

            return new DropwizardCounter(
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

            return new DropwizardRate(
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

            return new DropwizardHistogram(
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

            return new DropwizardTimer(
                name,
                config,
                timeMsProvider,
                executor,
                registry);
        }
    }

    public DropwizardMetricRegistry() {
        super(MetricMakerImpl.INSTANCE);
    }

    public DropwizardMetricRegistry(ScheduledExecutorService executor) {
        super(MetricMakerImpl.INSTANCE, executor);
    }

    public DropwizardMetricRegistry(
        PredicativeMetricNamedInfoProvider<MetricMod> preModsProvider,
        PredicativeMetricNamedInfoProvider<MetricMod> postModsProvider) {

        super(
            MetricMakerImpl.INSTANCE,
            preModsProvider,
            postModsProvider);
    }

    public DropwizardMetricRegistry(
        PredicativeMetricNamedInfoProvider<MetricMod> preModsProvider,
        PredicativeMetricNamedInfoProvider<MetricMod> postModsProvider,
        ScheduledExecutorService executor) {

        super(
            MetricMakerImpl.INSTANCE,
            preModsProvider,
            postModsProvider,
            executor);
    }

    public DropwizardMetricRegistry(
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

    public void addMetricSet(MetricName namePrefix, MetricSet dwMetricSet) {
        for (Map.Entry<String, Metric> dwMetricEntry : dwMetricSet.getMetrics().entrySet()) {
            MetricName name = MetricName.of(namePrefix, split(dwMetricEntry.getKey(), '.'));
            Metric dwMetric = dwMetricEntry.getValue();

            if (dwMetric instanceof MetricSet) {
                addMetricSet(name, (MetricSet)dwMetric);
            } else {
                if (dwMetric instanceof CachedGauge) {
                    cachingObjectVar(name, ((CachedGauge<?>)dwMetric)::getValue);
                } else if (dwMetric instanceof Gauge) {
                    objectVar(name, ((Gauge<?>)dwMetric)::getValue);
                } else if (dwMetric instanceof com.codahale.metrics.Counter) {
                    getOrAddMetric(
                        name,
                        Counter.class,
                        () -> buildCounter(
                            name,
                            () -> withCounter().allSlice().noLevels().total(counterInstance().put(com.codahale.metrics.Counter.class, dwMetric))));
                } else if (dwMetric instanceof com.codahale.metrics.Meter) {
                    getOrAddMetric(
                        name,
                        Rate.class,
                        () -> buildRate(
                            name,
                            () -> withRate().allSlice().noLevels().total(rateInstance().put(com.codahale.metrics.Meter.class, dwMetric))));
                } else if (dwMetric instanceof com.codahale.metrics.Histogram) {
                    getOrAddMetric(
                        name,
                        Histogram.class,
                        () -> buildHistogram(
                            name,
                            () -> withHistogram().allSlice().noLevels().total(histogramInstance().put(com.codahale.metrics.Histogram.class, dwMetric))));
                } else if (dwMetric instanceof com.codahale.metrics.Timer) {
                    getOrAddMetric(
                        name,
                        Timer.class,
                        () -> buildTimer(
                            name,
                            () -> withTimer().allSlice().noLevels().total(timerInstance().put(com.codahale.metrics.Timer.class, dwMetric))));
                }
            }
        }
    }
}