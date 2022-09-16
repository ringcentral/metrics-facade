package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.samples.*;

public class PrometheusInstanceSamplesProvider extends AbstractInstanceSamplesProvider<
    PrometheusSample,
    PrometheusSampleSpec,
    SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>,
    SampleMaker<PrometheusSample, PrometheusSampleSpec, PrometheusInstanceSampleSpec, PrometheusInstanceSample>,
    PrometheusInstanceSample,
    PrometheusInstanceSampleSpec,
    InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>,
    InstanceSampleMaker<PrometheusSample, PrometheusInstanceSample, PrometheusInstanceSampleSpec>> {

    public PrometheusInstanceSamplesProvider(MetricRegistry metricRegistry) {
        this(
            null,
            (PredicativeMetricNamedInfoProvider<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>>)null,
            metricRegistry);
    }

    public PrometheusInstanceSamplesProvider(
        InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> instanceSampleSpecProvider,
        MetricRegistry metricRegistry) {

        this(
            instanceSampleSpecProvider,
            null,
            new PrometheusInstanceSampleMaker(),
            new PrometheusSampleSpecProvider(),
            null,
            new PrometheusSampleMaker(),
            metricRegistry);
    }

    public PrometheusInstanceSamplesProvider(
        PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> instanceSampleSpecModsProvider,
        MetricRegistry metricRegistry) {

        this(
            instanceSampleSpecModsProvider,
            (PredicativeMetricNamedInfoProvider<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>>)null,
            metricRegistry);
    }

    public PrometheusInstanceSamplesProvider(
        PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> instanceSampleSpecModsProvider,
        PredicativeMetricNamedInfoProvider<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>> sampleSpecModsProvider,
        MetricRegistry metricRegistry) {

        this(
            instanceSampleSpecModsProvider,
            sampleSpecModsProvider,
            new PrometheusSampleSpecProvider(),
            metricRegistry);
    }

    public PrometheusInstanceSamplesProvider(
        PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> instanceSampleSpecModsProvider,
        SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec> sampleSpecProvider,
        MetricRegistry metricRegistry) {

        this(
            instanceSampleSpecModsProvider,
            null,
            sampleSpecProvider,
            metricRegistry);
    }

    public PrometheusInstanceSamplesProvider(
        PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> instanceSampleSpecModsProvider,
        PredicativeMetricNamedInfoProvider<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>> sampleSpecModsProvider,
        SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec> sampleSpecProvider,
        MetricRegistry metricRegistry) {

        this(
            new PrometheusInstanceSampleSpecProvider(),
            instanceSampleSpecModsProvider,
            new PrometheusInstanceSampleMaker(),
            sampleSpecProvider,
            sampleSpecModsProvider,
            new PrometheusSampleMaker(),
            metricRegistry);
    }

    public PrometheusInstanceSamplesProvider(
        InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> instanceSampleSpecProvider,
        PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> instanceSampleSpecModsProvider,
        InstanceSampleMaker<PrometheusSample, PrometheusInstanceSample, PrometheusInstanceSampleSpec> instanceSampleMaker,
        SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec> sampleSpecProvider,
        PredicativeMetricNamedInfoProvider<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>> sampleSpecModsProvider,
        SampleMaker<PrometheusSample, PrometheusSampleSpec, PrometheusInstanceSampleSpec, PrometheusInstanceSample> sampleMaker,
        MetricRegistry metricRegistry) {

        super(
            instanceSampleSpecProvider,
            instanceSampleSpecModsProvider,
            instanceSampleMaker,
            sampleSpecProvider,
            sampleSpecModsProvider,
            sampleMaker,
            metricRegistry);
    }
}
