package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.infoProviders.MaskTreeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;

import java.util.List;

public class DefaultSampleSpecModsProvider implements SpecModsProvider<
    SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>,
    DefaultSampleSpecModsProvider> {

    private final PredicativeMetricNamedInfoProvider<SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>> parent;

    public DefaultSampleSpecModsProvider() {
        this(new MaskTreeMetricNamedInfoProvider<>());
    }

    public DefaultSampleSpecModsProvider(PredicativeMetricNamedInfoProvider<SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>> parent) {
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultSampleSpecModsProvider addInfo(
        String key,
        MetricNamedPredicate predicate,
        SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec> info) {

        parent.addInfo(key, predicate, info);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultSampleSpecModsProvider removeInfo(String key) {
        parent.removeInfo(key);
        return this;
    }

    @Override
    public List<SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>> infosFor(MetricNamed named) {
        return parent.infosFor(named);
    }
}
