package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;

public class DefaultInstanceSamplesProvider extends AbstractInstanceSamplesProvider<
    DefaultSample,
    DefaultSampleSpec,
    SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>,
    SampleMaker<DefaultSample, DefaultSampleSpec, DefaultInstanceSampleSpec, InstanceSample<DefaultSample>>,
    InstanceSample<DefaultSample>,
    DefaultInstanceSampleSpec,
    InstanceSampleSpecProvider<DefaultInstanceSampleSpec>,
    InstanceSampleMaker<DefaultSample, InstanceSample<DefaultSample>, DefaultInstanceSampleSpec>> {

    public DefaultInstanceSamplesProvider(MetricRegistry metricRegistry) {
        this(
            null,
            (PredicativeMetricNamedInfoProvider<SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>>)null,
            metricRegistry);
    }

    public DefaultInstanceSamplesProvider(
        PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<DefaultInstanceSampleSpec>> instanceSampleSpecModsProvider,
        MetricRegistry metricRegistry) {

        this(
            instanceSampleSpecModsProvider,
            (PredicativeMetricNamedInfoProvider<SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>>)null,
            metricRegistry);
    }

    public DefaultInstanceSamplesProvider(
        PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<DefaultInstanceSampleSpec>> instanceSampleSpecModsProvider,
        PredicativeMetricNamedInfoProvider<SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>> sampleSpecModsProvider,
        MetricRegistry metricRegistry) {

        this(
            instanceSampleSpecModsProvider,
            sampleSpecModsProvider,
            new DefaultSampleSpecProvider(),
            metricRegistry);
    }

    public DefaultInstanceSamplesProvider(
        PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<DefaultInstanceSampleSpec>> instanceSampleSpecModsProvider,
        SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec> sampleSpecProvider,
        MetricRegistry metricRegistry) {

        this(
            instanceSampleSpecModsProvider,
            null,
            sampleSpecProvider,
            metricRegistry);
    }

    public DefaultInstanceSamplesProvider(
        PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<DefaultInstanceSampleSpec>> instanceSampleSpecModsProvider,
        PredicativeMetricNamedInfoProvider<SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>> sampleSpecModsProvider,
        SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec> sampleSpecProvider,
        MetricRegistry metricRegistry) {

        this(
            new DefaultInstanceSampleSpecProvider(),
            instanceSampleSpecModsProvider,
            new DefaultInstanceSampleMaker(),
            sampleSpecProvider,
            sampleSpecModsProvider,
            new DefaultSampleMaker(),
            metricRegistry);
    }

    public DefaultInstanceSamplesProvider(
        InstanceSampleSpecProvider<DefaultInstanceSampleSpec> instanceSampleSpecProvider,
        PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<DefaultInstanceSampleSpec>> instanceSampleSpecModsProvider,
        InstanceSampleMaker<DefaultSample, InstanceSample<DefaultSample>, DefaultInstanceSampleSpec> instanceSampleMaker,
        SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec> sampleSpecProvider,
        PredicativeMetricNamedInfoProvider<SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>> sampleSpecModsProvider,
        SampleMaker<DefaultSample, DefaultSampleSpec, DefaultInstanceSampleSpec, InstanceSample<DefaultSample>> sampleMaker,
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
