package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.infoProviders.*;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.*;

import java.util.List;

public class DefaultSampleSpecModsProvider implements PredicativeMetricNamedInfoProvider<
    SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>> {

    private final PredicativeMetricNamedInfoProvider<SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>> parent;

    public DefaultSampleSpecModsProvider() {
        this(new MaskTreeMetricNamedInfoProvider<>());
    }

    public DefaultSampleSpecModsProvider(PredicativeMetricNamedInfoProvider<SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>> parent) {
        this.parent = parent;
    }

    public DefaultSampleSpecModsProvider addMod(
        MetricNamedPredicateBuilder<?> predicateBuilder,
        SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec> mod) {

        return addMod(predicateBuilder.build(), mod);
    }

    public DefaultSampleSpecModsProvider addMod(
        MetricNamedPredicate predicate,
        SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec> mod) {

        parent.addInfo(predicate, mod);
        return this;
    }

    @Override
    public DefaultSampleSpecModsProvider addInfo(
        MetricNamedPredicate predicate,
        SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec> info) {

        parent.addInfo(predicate, info);
        return this;
    }

    public List<SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>> modsFor(MetricInstance instance) {
        return infosFor(instance);
    }

    @Override
    public List<SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec>> infosFor(MetricNamed named) {
        return parent.infosFor(named);
    }
}
