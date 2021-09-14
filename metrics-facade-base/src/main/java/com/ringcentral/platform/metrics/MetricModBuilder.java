package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.configs.builders.*;
import com.ringcentral.platform.metrics.counter.configs.CounterConfig;
import com.ringcentral.platform.metrics.histogram.configs.HistogramConfig;
import com.ringcentral.platform.metrics.rate.configs.RateConfig;
import com.ringcentral.platform.metrics.timer.configs.TimerConfig;
import com.ringcentral.platform.metrics.var.configs.builders.*;

public class MetricModBuilder {

    private MetricConfigBuilderProvider<BaseMetricConfigBuilder> metricConfigBuilderProvider;
    private MetricConfigBuilderProvider<BaseVarConfigBuilder> varConfigBuilderProvider;
    private MetricConfigBuilderProvider<BaseCachingVarConfigBuilder> cachingVarConfigBuilderProvider;
    private MetricConfigBuilderProvider<BaseMeterConfigBuilder> meterConfigBuilderProvider;
    private MetricConfigBuilderProvider<? extends MetricConfigBuilder<? extends CounterConfig>> counterConfigBuilderProvider;
    private MetricConfigBuilderProvider<? extends MetricConfigBuilder<? extends RateConfig>> rateConfigBuilderProvider;
    private MetricConfigBuilderProvider<? extends MetricConfigBuilder<? extends HistogramConfig>> histogramConfigBuilderProvider;
    private MetricConfigBuilderProvider<? extends MetricConfigBuilder<? extends TimerConfig>> timerConfigBuilderProvider;

    public static MetricModBuilder metricMod() {
        return metricModBuilder();
    }

    public static MetricModBuilder modifying() {
        return metricModBuilder();
    }

    public static MetricModBuilder metricModBuilder() {
        return new MetricModBuilder();
    }

    public MetricModBuilder metric(MetricConfigBuilderProvider<BaseMetricConfigBuilder> metricConfigBuilderProvider) {
        this.metricConfigBuilderProvider = metricConfigBuilderProvider;
        return this;
    }

    public MetricModBuilder variable(MetricConfigBuilderProvider<BaseVarConfigBuilder> varConfigBuilderProvider) {
        this.varConfigBuilderProvider = varConfigBuilderProvider;
        return this;
    }

    public MetricModBuilder cachingVar(MetricConfigBuilderProvider<BaseCachingVarConfigBuilder> cachingVarConfigBuilderProvider) {
        this.cachingVarConfigBuilderProvider = cachingVarConfigBuilderProvider;
        return this;
    }

    public MetricModBuilder meter(MetricConfigBuilderProvider<BaseMeterConfigBuilder> meterConfigBuilderProvider) {
        this.meterConfigBuilderProvider = meterConfigBuilderProvider;
        return this;
    }

    public MetricModBuilder counter(MetricConfigBuilderProvider<? extends MetricConfigBuilder<? extends CounterConfig>> counterConfigBuilderProvider) {
        this.counterConfigBuilderProvider = counterConfigBuilderProvider;
        return this;
    }

    public MetricModBuilder rate(MetricConfigBuilderProvider<? extends MetricConfigBuilder<? extends RateConfig>> rateConfigBuilderProvider) {
        this.rateConfigBuilderProvider = rateConfigBuilderProvider;
        return this;
    }

    public MetricModBuilder histogram(MetricConfigBuilderProvider<? extends MetricConfigBuilder<? extends HistogramConfig>> histogramConfigBuilderProvider) {
        this.histogramConfigBuilderProvider = histogramConfigBuilderProvider;
        return this;
    }

    public MetricModBuilder timer(MetricConfigBuilderProvider<? extends MetricConfigBuilder<TimerConfig>> timerConfigBuilderProvider) {
        this.timerConfigBuilderProvider = timerConfigBuilderProvider;
        return this;
    }

    public MetricMod build() {
        return new MetricMod(
            metricConfigBuilderProvider,
            varConfigBuilderProvider,
            cachingVarConfigBuilderProvider,
            meterConfigBuilderProvider,
            counterConfigBuilderProvider,
            rateConfigBuilderProvider,
            histogramConfigBuilderProvider,
            timerConfigBuilderProvider);
    }
}
