package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.samples.InstanceSampleMaker;
import com.ringcentral.platform.metrics.samples.InstanceSampleSpecProvider;
import com.ringcentral.platform.metrics.samples.SampleMaker;
import com.ringcentral.platform.metrics.samples.SampleSpecProvider;

import static java.util.Objects.requireNonNull;

public class PrometheusInstanceSamplesProviderBuilder {

    private InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> instanceSampleSpecProvider;
    private PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> instanceSampleSpecModsProvider;
    private InstanceSampleMaker<PrometheusSample, PrometheusInstanceSample, PrometheusInstanceSampleSpec> instanceSampleMaker;
    private SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec> sampleSpecProvider;
    private PredicativeMetricNamedInfoProvider<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>> sampleSpecModsProvider;
    private SampleMaker<PrometheusSample, PrometheusSampleSpec, PrometheusInstanceSampleSpec, PrometheusInstanceSample> sampleMaker;
    private final MetricRegistry metricRegistry;

    public PrometheusInstanceSamplesProviderBuilder(MetricRegistry metricRegistry) {
        this.metricRegistry = requireNonNull(metricRegistry);
        this.instanceSampleSpecProvider = new PrometheusInstanceSampleSpecProvider();
        this.instanceSampleMaker = new PrometheusInstanceSampleMaker();
        this.sampleSpecProvider = new PrometheusSampleSpecProvider();
        this.sampleMaker = new PrometheusSampleMaker();
    }

    public static PrometheusInstanceSamplesProviderBuilder prometheusInstanceSamplesProvider(MetricRegistry metricRegistry) {
        return new PrometheusInstanceSamplesProviderBuilder(metricRegistry);
    }

    public PrometheusInstanceSamplesProviderBuilder instanceSampleSpecProvider(InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> instanceSampleSpecProvider) {
        this.instanceSampleSpecProvider = requireNonNull(instanceSampleSpecProvider);
        return this;
    }

    public PrometheusInstanceSamplesProviderBuilder instanceSampleSpecModsProvider(PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> instanceSampleSpecModsProvider) {
        this.instanceSampleSpecModsProvider = instanceSampleSpecModsProvider;
        return this;
    }

    public PrometheusInstanceSamplesProviderBuilder instanceSampleMaker(InstanceSampleMaker<PrometheusSample, PrometheusInstanceSample, PrometheusInstanceSampleSpec> instanceSampleMaker) {
        this.instanceSampleMaker = requireNonNull(instanceSampleMaker);
        return this;
    }

    public PrometheusInstanceSamplesProviderBuilder sampleSpecProvider(SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec> sampleSpecProvider) {
        this.sampleSpecProvider = requireNonNull(sampleSpecProvider);
        return this;
    }

    public PrometheusInstanceSamplesProviderBuilder sampleSpecModsProvider(PredicativeMetricNamedInfoProvider<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>> sampleSpecModsProvider) {
        this.sampleSpecModsProvider = sampleSpecModsProvider;
        return this;
    }

    public PrometheusInstanceSamplesProviderBuilder sampleMaker(SampleMaker<PrometheusSample, PrometheusSampleSpec, PrometheusInstanceSampleSpec, PrometheusInstanceSample> sampleMaker) {
        this.sampleMaker = requireNonNull(sampleMaker);
        return this;
    }

    public PrometheusInstanceSamplesProvider build() {
        return new PrometheusInstanceSamplesProvider(
            instanceSampleSpecProvider,
            instanceSampleSpecModsProvider,
            instanceSampleMaker,
            sampleSpecProvider,
            sampleSpecModsProvider,
            sampleMaker,
            metricRegistry);
    }
}
