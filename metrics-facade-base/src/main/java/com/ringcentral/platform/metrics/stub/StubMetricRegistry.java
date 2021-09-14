package com.ringcentral.platform.metrics.stub;

import com.ringcentral.platform.metrics.AbstractMetricRegistry;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.counter.configs.CounterConfig;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.histogram.configs.HistogramConfig;
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

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class StubMetricRegistry extends AbstractMetricRegistry {

    public StubMetricRegistry() {
        super(new StubMetricMaker());
    }

    private static class StubMetricMaker implements MetricMaker {

        @Override
        public ObjectVar makeObjectVar(MetricName name, VarConfig config, Supplier<Object> valueSupplier, ScheduledExecutorService executor) {
            return new StubObjectVar(name, config, valueSupplier, executor);
        }

        @Override
        public CachingObjectVar makeCachingObjectVar(MetricName name, CachingVarConfig config, Supplier<Object> valueSupplier, ScheduledExecutorService executor) {
            return new StubCachingObjectVar(name, config, valueSupplier, executor);
        }

        @Override
        public LongVar makeLongVar(MetricName name, VarConfig config, Supplier<Long> valueSupplier, ScheduledExecutorService executor) {
            return new StubLongVar(name, config, valueSupplier, executor);
        }

        @Override
        public CachingLongVar makeCachingLongVar(MetricName name, CachingVarConfig config, Supplier<Long> valueSupplier, ScheduledExecutorService executor) {
            return new StubCachingLongVar(name, config, valueSupplier, executor);
        }

        @Override
        public DoubleVar makeDoubleVar(MetricName name, VarConfig config, Supplier<Double> valueSupplier, ScheduledExecutorService executor) {
            return new StubDoubleVar(name, config, valueSupplier, executor);
        }

        @Override
        public CachingDoubleVar makeCachingDoubleVar(MetricName name, CachingVarConfig config, Supplier<Double> valueSupplier, ScheduledExecutorService executor) {
            return new StubCachingDoubleVar(name, config, valueSupplier, executor);
        }

        @Override
        public StringVar makeStringVar(MetricName name, VarConfig config, Supplier<String> valueSupplier, ScheduledExecutorService executor) {
            return new StubStringVar(name, config, valueSupplier, executor);
        }

        @Override
        public CachingStringVar makeCachingStringVar(MetricName name, CachingVarConfig config, Supplier<String> valueSupplier, ScheduledExecutorService executor) {
            return new StubCachingStringVar(name, config, valueSupplier, executor);
        }

        @Override
        public Counter makeCounter(MetricName name, CounterConfig config, TimeMsProvider timeMsProvider, ScheduledExecutorService executor) {
            return new StubCounter(name, config, timeMsProvider, executor);
        }

        @Override
        public Rate makeRate(MetricName name, RateConfig config, TimeMsProvider timeMsProvider, ScheduledExecutorService executor) {
            return new StubRate(name, config, timeMsProvider, executor);
        }

        @Override
        public Histogram makeHistogram(MetricName name, HistogramConfig config, TimeMsProvider timeMsProvider, ScheduledExecutorService executor) {
            return new StubHistogram(name, config, timeMsProvider, executor);
        }

        @Override
        public Timer makeTimer(MetricName name, TimerConfig config, TimeMsProvider timeMsProvider, ScheduledExecutorService executor) {
            return new StubTimer(name, config, timeMsProvider, executor);
        }
    }
}
