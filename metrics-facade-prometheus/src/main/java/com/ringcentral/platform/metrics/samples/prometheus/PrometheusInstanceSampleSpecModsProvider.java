package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.infoProviders.MaskTreeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;
import com.ringcentral.platform.metrics.samples.InstanceSampleSpecProvider;
import com.ringcentral.platform.metrics.samples.SpecModsProvider;

import java.util.List;
import java.util.function.Predicate;

public class PrometheusInstanceSampleSpecModsProvider implements SpecModsProvider<
    InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>,
    PrometheusInstanceSampleSpecModsProvider> {

    private final PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> parent;

    public PrometheusInstanceSampleSpecModsProvider() {
        this(new MaskTreeMetricNamedInfoProvider<>());
    }

    public PrometheusInstanceSampleSpecModsProvider(PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> parent) {
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrometheusInstanceSampleSpecModsProvider addInfo(
        String key,
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> info) {

        parent.addInfo(key, predicate, info);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrometheusInstanceSampleSpecModsProvider removeInfo(String key) {
        parent.removeInfo(key);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrometheusInstanceSampleSpecModsProvider removeInfos(Predicate<String> keyPredicate) {
        parent.removeInfos(keyPredicate);
        return this;
    }

    @Override
    public List<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> infosFor(MetricNamed named) {
        return parent.infosFor(named);
    }
}
