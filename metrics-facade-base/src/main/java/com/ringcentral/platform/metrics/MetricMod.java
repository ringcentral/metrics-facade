package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.configs.builders.*;
import com.ringcentral.platform.metrics.counter.configs.CounterConfig;
import com.ringcentral.platform.metrics.histogram.configs.HistogramConfig;
import com.ringcentral.platform.metrics.rate.configs.RateConfig;
import com.ringcentral.platform.metrics.timer.configs.TimerConfig;
import com.ringcentral.platform.metrics.var.configs.builders.*;

public class MetricMod {

    private final BaseMetricConfigBuilder metricConfigBuilder;
    private final BaseVarConfigBuilder varConfigBuilder;
    private final BaseCachingVarConfigBuilder cachingVarConfigBuilder;
    private final BaseMeterConfigBuilder meterConfigBuilder;
    private final MetricConfigBuilder<? extends CounterConfig> counterConfigBuilder;
    private final MetricConfigBuilder<? extends RateConfig> rateConfigBuilder;
    private final MetricConfigBuilder<? extends HistogramConfig> histogramConfigBuilder;
    private final MetricConfigBuilder<? extends TimerConfig> timerConfigBuilder;

    public MetricMod(
        MetricConfigBuilderProvider<BaseMetricConfigBuilder> metricConfigBuilderProvider,
        MetricConfigBuilderProvider<BaseVarConfigBuilder> varConfigBuilderProvider,
        MetricConfigBuilderProvider<BaseCachingVarConfigBuilder> cachingVarConfigBuilderProvider,
        MetricConfigBuilderProvider<BaseMeterConfigBuilder> meterConfigBuilderProvider,
        MetricConfigBuilderProvider<? extends MetricConfigBuilder<? extends CounterConfig>> counterConfigBuilderProvider,
        MetricConfigBuilderProvider<? extends MetricConfigBuilder<? extends RateConfig>> rateConfigBuilderProvider,
        MetricConfigBuilderProvider<? extends MetricConfigBuilder<? extends HistogramConfig>> histogramConfigBuilderProvider,
        MetricConfigBuilderProvider<? extends MetricConfigBuilder<? extends TimerConfig>> timerConfigBuilderProvider) {

        this.metricConfigBuilder =
            metricConfigBuilderProvider != null ?
            metricConfigBuilderProvider.builder() :
            null;

        if (varConfigBuilderProvider != null) {
            this.varConfigBuilder = varConfigBuilderProvider.builder();

            if (this.metricConfigBuilder != null) {
                this.varConfigBuilder.rebase(this.metricConfigBuilder);
            }
        } else {
            this.varConfigBuilder = null;
        }

        if (cachingVarConfigBuilderProvider != null) {
            this.cachingVarConfigBuilder = cachingVarConfigBuilderProvider.builder();

            if (this.varConfigBuilder != null) {
                this.cachingVarConfigBuilder.rebase(this.varConfigBuilder);
            } else if (this.metricConfigBuilder != null) {
                this.cachingVarConfigBuilder.rebase(this.metricConfigBuilder);
            }
        } else {
            this.cachingVarConfigBuilder = null;
        }

        if (meterConfigBuilderProvider != null) {
            this.meterConfigBuilder = meterConfigBuilderProvider.builder();

            if (this.metricConfigBuilder != null) {
                this.meterConfigBuilder.rebase(this.metricConfigBuilder);
            }
        } else {
            this.meterConfigBuilder = null;
        }

        if (counterConfigBuilderProvider != null) {
            this.counterConfigBuilder = counterConfigBuilderProvider.builder();

            if (this.meterConfigBuilder != null) {
                this.counterConfigBuilder.rebase(this.meterConfigBuilder);
            } else if (this.metricConfigBuilder != null) {
                this.counterConfigBuilder.rebase(this.metricConfigBuilder);
            }
        } else {
            this.counterConfigBuilder = null;
        }

        if (rateConfigBuilderProvider != null) {
            this.rateConfigBuilder = rateConfigBuilderProvider.builder();

            if (this.meterConfigBuilder != null) {
                this.rateConfigBuilder.rebase(this.meterConfigBuilder);
            } else if (this.metricConfigBuilder != null) {
                this.rateConfigBuilder.rebase(this.metricConfigBuilder);
            }
        } else {
            this.rateConfigBuilder = null;
        }

        if (histogramConfigBuilderProvider != null) {
            this.histogramConfigBuilder = histogramConfigBuilderProvider.builder();

            if (this.meterConfigBuilder != null) {
                this.histogramConfigBuilder.rebase(this.meterConfigBuilder);
            } else if (this.metricConfigBuilder != null) {
                this.histogramConfigBuilder.rebase(this.metricConfigBuilder);
            }
        } else {
            this.histogramConfigBuilder = null;
        }

        if (timerConfigBuilderProvider != null) {
            this.timerConfigBuilder = timerConfigBuilderProvider.builder();

            if (this.meterConfigBuilder != null) {
                this.timerConfigBuilder.rebase(this.meterConfigBuilder);
            } else if (this.metricConfigBuilder != null) {
                this.timerConfigBuilder.rebase(this.metricConfigBuilder);
            }
        } else {
            this.timerConfigBuilder = null;
        }
    }

    public boolean hasMetricConfigBuilder() {
        return metricConfigBuilder != null;
    }

    public BaseMetricConfigBuilder metricConfigBuilder() {
        return metricConfigBuilder;
    }

    public boolean hasVarConfigBuilder() {
        return varConfigBuilder != null;
    }

    public BaseVarConfigBuilder varConfigBuilder() {
        return varConfigBuilder;
    }

    public boolean hasCachingVarConfigBuilder() {
        return cachingVarConfigBuilder != null;
    }

    public BaseCachingVarConfigBuilder cachingVarConfigBuilder() {
        return cachingVarConfigBuilder;
    }

    public boolean hasMeterConfigBuilder() {
        return meterConfigBuilder != null;
    }

    public BaseMeterConfigBuilder meterConfigBuilder() {
        return meterConfigBuilder;
    }

    public boolean hasCounterConfigBuilder() {
        return counterConfigBuilder != null;
    }

    public MetricConfigBuilder<? extends CounterConfig> counterConfigBuilder() {
        return counterConfigBuilder;
    }

    public boolean hasRateConfigBuilder() {
        return rateConfigBuilder != null;
    }

    public MetricConfigBuilder<? extends RateConfig> rateConfigBuilder() {
        return rateConfigBuilder;
    }

    public boolean hasHistogramConfigBuilder() {
        return histogramConfigBuilder != null;
    }

    public MetricConfigBuilder<? extends HistogramConfig> histogramConfigBuilder() {
        return histogramConfigBuilder;
    }

    public boolean hasTimerConfigBuilder() {
        return timerConfigBuilder != null;
    }

    public MetricConfigBuilder<? extends TimerConfig> timerConfigBuilder() {
        return timerConfigBuilder;
    }
}
