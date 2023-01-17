package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.infoProviders.MaskTreeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;
import com.ringcentral.platform.metrics.samples.SampleSpecProvider;
import com.ringcentral.platform.metrics.samples.SpecModsProvider;

import java.util.List;

public class PrometheusSampleSpecModsProvider implements SpecModsProvider<
    SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>,
    PrometheusSampleSpecModsProvider> {

    private final PredicativeMetricNamedInfoProvider<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>> parent;

    public PrometheusSampleSpecModsProvider() {
        this(new MaskTreeMetricNamedInfoProvider<>());
    }

    public PrometheusSampleSpecModsProvider(PredicativeMetricNamedInfoProvider<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>> parent) {
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrometheusSampleSpecModsProvider addInfo(
        String key,
        MetricNamedPredicate predicate,
        SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec> info) {

        parent.addInfo(key, predicate, info);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrometheusSampleSpecModsProvider removeInfo(String key) {
        parent.removeInfo(key);
        return this;
    }

    @Override
    public List<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>> infosFor(MetricNamed named) {
        return parent.infosFor(named);
    }
}
