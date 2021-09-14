package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.configs.builders.*;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.counter.configs.CounterConfig;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.histogram.configs.HistogramConfig;
import com.ringcentral.platform.metrics.predicates.*;
import com.ringcentral.platform.metrics.rate.Rate;
import com.ringcentral.platform.metrics.rate.configs.RateConfig;
import com.ringcentral.platform.metrics.timer.Timer;
import com.ringcentral.platform.metrics.timer.configs.TimerConfig;
import com.ringcentral.platform.metrics.var.doubleVar.*;
import com.ringcentral.platform.metrics.var.doubleVar.configs.*;
import com.ringcentral.platform.metrics.var.longVar.*;
import com.ringcentral.platform.metrics.var.longVar.configs.*;
import com.ringcentral.platform.metrics.var.objectVar.*;
import com.ringcentral.platform.metrics.var.objectVar.configs.*;
import com.ringcentral.platform.metrics.var.stringVar.*;
import com.ringcentral.platform.metrics.var.stringVar.configs.*;

import java.util.Map;
import java.util.function.Supplier;

public interface MetricRegistry {
    void addListener(MetricRegistryListener listener);

    Map<MetricKey, Metric> metrics();
    void remove(MetricKey key);

    default void preConfigure(MetricNamedPredicateBuilder<?> predicateBuilder, MetricModBuilder modBuilder) {
        preConfigure(predicateBuilder.build(), modBuilder);
    }

    void preConfigure(MetricNamedPredicate predicate, MetricModBuilder modBuilder);

    default void postConfigure(MetricNamedPredicateBuilder<?> predicateBuilder, MetricModBuilder modBuilder) {
        postConfigure(predicateBuilder.build(), modBuilder);
    }

    void postConfigure(MetricNamedPredicate predicate, MetricModBuilder modBuilder);

    /* Object var */

    ObjectVar objectVar(MetricKey key, Supplier<Object> valueSupplier);

    <C extends ObjectVarConfig> ObjectVar objectVar(
        MetricKey key,
        Supplier<Object> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    ObjectVar newObjectVar(MetricKey key, Supplier<Object> valueSupplier);

    <C extends ObjectVarConfig> ObjectVar newObjectVar(
        MetricKey key,
        Supplier<Object> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    /* Caching object var */

    CachingObjectVar cachingObjectVar(MetricKey key, Supplier<Object> valueSupplier);

    <C extends CachingObjectVarConfig> CachingObjectVar cachingObjectVar(
        MetricKey key,
        Supplier<Object> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    CachingObjectVar newCachingObjectVar(MetricKey key, Supplier<Object> valueSupplier);

    <C extends CachingObjectVarConfig> CachingObjectVar newCachingObjectVar(
        MetricKey key,
        Supplier<Object> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    /* Long var */

    LongVar longVar(MetricKey key, Supplier<Long> valueSupplier);

    <C extends LongVarConfig> LongVar longVar(
        MetricKey key,
        Supplier<Long> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    LongVar newLongVar(MetricKey key, Supplier<Long> valueSupplier);

    <C extends LongVarConfig> LongVar newLongVar(
        MetricKey key,
        Supplier<Long> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    /* Caching long var */

    CachingLongVar cachingLongVar(MetricKey key, Supplier<Long> valueSupplier);

    <C extends CachingLongVarConfig> CachingLongVar cachingLongVar(
        MetricKey key,
        Supplier<Long> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    CachingLongVar newCachingLongVar(MetricKey key, Supplier<Long> valueSupplier);

    <C extends CachingLongVarConfig> CachingLongVar newCachingLongVar(
        MetricKey key,
        Supplier<Long> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    /* Double var */

    DoubleVar doubleVar(MetricKey key, Supplier<Double> valueSupplier);

    <C extends DoubleVarConfig> DoubleVar doubleVar(
        MetricKey key,
        Supplier<Double> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    DoubleVar newDoubleVar(MetricKey key, Supplier<Double> valueSupplier);

    <C extends DoubleVarConfig> DoubleVar newDoubleVar(
        MetricKey key,
        Supplier<Double> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    /* Caching double var */

    CachingDoubleVar cachingDoubleVar(MetricKey key, Supplier<Double> valueSupplier);

    <C extends CachingDoubleVarConfig> CachingDoubleVar cachingDoubleVar(
        MetricKey key,
        Supplier<Double> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    CachingDoubleVar newCachingDoubleVar(MetricKey key, Supplier<Double> valueSupplier);

    <C extends CachingDoubleVarConfig> CachingDoubleVar newCachingDoubleVar(
        MetricKey key,
        Supplier<Double> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    /* String var */

    StringVar stringVar(MetricKey key, Supplier<String> valueSupplier);

    <C extends StringVarConfig> StringVar stringVar(
        MetricKey key,
        Supplier<String> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    StringVar newStringVar(MetricKey key, Supplier<String> valueSupplier);

    <C extends StringVarConfig> StringVar newStringVar(
        MetricKey key,
        Supplier<String> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    /* Caching string var */

    CachingStringVar cachingStringVar(MetricKey key, Supplier<String> valueSupplier);

    <C extends CachingStringVarConfig> CachingStringVar cachingStringVar(
        MetricKey key,
        Supplier<String> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    CachingStringVar newCachingStringVar(MetricKey key, Supplier<String> valueSupplier);

    <C extends CachingStringVarConfig> CachingStringVar newCachingStringVar(
        MetricKey key,
        Supplier<String> valueSupplier,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    /* Counter */

    Counter counter(MetricKey key);

    <C extends CounterConfig> Counter counter(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    Counter newCounter(MetricKey key);

    <C extends CounterConfig> Counter newCounter(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    /* Rate */

    Rate rate(MetricKey key);

    <C extends RateConfig> Rate rate(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    Rate newRate(MetricKey key);

    <C extends RateConfig> Rate newRate(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    /* Histogram */

    Histogram histogram(MetricKey key);

    <C extends HistogramConfig> Histogram histogram(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    Histogram newHistogram(MetricKey key);

    <C extends HistogramConfig> Histogram newHistogram(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    /* Timer */

    Timer timer(MetricKey key);

    <C extends TimerConfig> Timer timer(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);

    Timer newTimer(MetricKey key);

    <C extends TimerConfig> Timer newTimer(
        MetricKey key,
        Supplier<? extends MetricConfigBuilderProvider<? extends MetricConfigBuilder<C>>> configBuilderProviderSupplier);
}