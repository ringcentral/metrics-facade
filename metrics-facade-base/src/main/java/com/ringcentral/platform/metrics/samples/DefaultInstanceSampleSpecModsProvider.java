package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.infoProviders.MaskTreeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;

import java.util.List;

public class DefaultInstanceSampleSpecModsProvider implements SpecModsProvider<
    InstanceSampleSpecProvider<DefaultInstanceSampleSpec>,
    DefaultInstanceSampleSpecModsProvider> {

    private final PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<DefaultInstanceSampleSpec>> parent;

    public DefaultInstanceSampleSpecModsProvider() {
        this(new MaskTreeMetricNamedInfoProvider<>());
    }

    public DefaultInstanceSampleSpecModsProvider(PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<DefaultInstanceSampleSpec>> parent) {
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultInstanceSampleSpecModsProvider addInfo(
        String key,
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<DefaultInstanceSampleSpec> info) {

        parent.addInfo(key, predicate, info);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultInstanceSampleSpecModsProvider removeInfo(String key) {
        parent.removeInfo(key);
        return this;
    }

    @Override
    public List<InstanceSampleSpecProvider<DefaultInstanceSampleSpec>> infosFor(MetricNamed named) {
        return parent.infosFor(named);
    }
}
