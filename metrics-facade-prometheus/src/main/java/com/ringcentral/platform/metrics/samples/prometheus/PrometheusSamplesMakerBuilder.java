package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.names.MetricName;

import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusSamplesProducer.*;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;

public class PrometheusSamplesMakerBuilder {

    private boolean separateHistogramAndSummary = DEFAULT_SEPARATE_HISTOGRAM_AND_SUMMARY;

    private MetricName histogramChildInstanceSampleNameSuffix = DEFAULT_HISTOGRAM_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX;
    private MetricName summaryChildInstanceSampleNameSuffix = DEFAULT_SUMMARY_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX;
    private MetricName minChildInstanceSampleNameSuffix = DEFAULT_MIN_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX;
    private MetricName maxChildInstanceSampleNameSuffix = DEFAULT_MAX_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX;
    private MetricName meanChildInstanceSampleNameSuffix = DEFAULT_MEAN_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX;
    private MetricName standardDeviationChildInstanceSampleNameSuffix = DEFAULT_STANDARD_DEVIATION_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX;

    public static PrometheusSamplesMakerBuilder prometheusSamplesMakerBuilder() {
        return new PrometheusSamplesMakerBuilder();
    }

    public PrometheusSamplesMakerBuilder separateHistogramAndSummary(boolean separateHistogramAndSummary) {
        this.separateHistogramAndSummary = separateHistogramAndSummary;
        return this;
    }

    public PrometheusSamplesMakerBuilder histogramChildInstanceSampleNameSuffix(MetricName histogramChildInstanceSampleNameSuffix) {
        checkArgument(!histogramChildInstanceSampleNameSuffix.isEmpty(), "histogramChildInstanceSampleNameSuffix is empty");
        this.histogramChildInstanceSampleNameSuffix = histogramChildInstanceSampleNameSuffix;
        return this;
    }

    public PrometheusSamplesMakerBuilder summaryChildInstanceSampleNameSuffix(MetricName summaryChildInstanceSampleNameSuffix) {
        checkArgument(!summaryChildInstanceSampleNameSuffix.isEmpty(), "summaryChildInstanceSampleNameSuffix is empty");
        this.summaryChildInstanceSampleNameSuffix = summaryChildInstanceSampleNameSuffix;
        return this;
    }

    public PrometheusSamplesMakerBuilder minChildInstanceSampleNameSuffix(MetricName minChildInstanceSampleNameSuffix) {
        checkArgument(!minChildInstanceSampleNameSuffix.isEmpty(), "minChildInstanceSampleNameSuffix is empty");
        this.minChildInstanceSampleNameSuffix = minChildInstanceSampleNameSuffix;
        return this;
    }

    public PrometheusSamplesMakerBuilder maxChildInstanceSampleNameSuffix(MetricName maxChildInstanceSampleNameSuffix) {
        checkArgument(!maxChildInstanceSampleNameSuffix.isEmpty(), "maxChildInstanceSampleNameSuffix is empty");
        this.maxChildInstanceSampleNameSuffix = maxChildInstanceSampleNameSuffix;
        return this;
    }

    public PrometheusSamplesMakerBuilder meanChildInstanceSampleNameSuffix(MetricName meanChildInstanceSampleNameSuffix) {
        checkArgument(!meanChildInstanceSampleNameSuffix.isEmpty(), "meanChildInstanceSampleNameSuffix is empty");
        this.meanChildInstanceSampleNameSuffix = meanChildInstanceSampleNameSuffix;
        return this;
    }

    public PrometheusSamplesMakerBuilder standardDeviationChildInstanceSampleNameSuffix(MetricName standardDeviationChildInstanceSampleNameSuffix) {
        checkArgument(!standardDeviationChildInstanceSampleNameSuffix.isEmpty(), "standardDeviationChildInstanceSampleNameSuffix is empty");
        this.standardDeviationChildInstanceSampleNameSuffix = standardDeviationChildInstanceSampleNameSuffix;
        return this;
    }

    public PrometheusSamplesProducer build() {
        return new PrometheusSamplesProducer(
            separateHistogramAndSummary,
            histogramChildInstanceSampleNameSuffix,
            summaryChildInstanceSampleNameSuffix,
            minChildInstanceSampleNameSuffix,
            maxChildInstanceSampleNameSuffix,
            meanChildInstanceSampleNameSuffix,
            standardDeviationChildInstanceSampleNameSuffix);
    }
}
