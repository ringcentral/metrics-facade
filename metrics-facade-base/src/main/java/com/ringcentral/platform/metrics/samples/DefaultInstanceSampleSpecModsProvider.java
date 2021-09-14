package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.infoProviders.*;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.*;

import java.util.List;

public class DefaultInstanceSampleSpecModsProvider implements PredicativeMetricNamedInfoProvider<
    InstanceSampleSpecProvider<DefaultInstanceSampleSpec>> {

    private final PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<DefaultInstanceSampleSpec>> parent;

    public DefaultInstanceSampleSpecModsProvider() {
        this(new MaskTreeMetricNamedInfoProvider<>());
    }

    public DefaultInstanceSampleSpecModsProvider(PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<DefaultInstanceSampleSpec>> parent) {
        this.parent = parent;
    }

    public DefaultInstanceSampleSpecModsProvider addMod(
        MetricNamedPredicateBuilder<?> predicateBuilder,
        InstanceSampleSpecProvider<DefaultInstanceSampleSpec> mod) {

        return addMod(predicateBuilder.build(), mod);
    }

    public DefaultInstanceSampleSpecModsProvider addMod(
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<DefaultInstanceSampleSpec> mod) {

        return addInfo(predicate, mod);
    }

    @Override
    public DefaultInstanceSampleSpecModsProvider addInfo(
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<DefaultInstanceSampleSpec> mod) {

        parent.addInfo(predicate, mod);
        return this;
    }

    public List<InstanceSampleSpecProvider<DefaultInstanceSampleSpec>> modsFor(MetricInstance instance) {
        return infosFor(instance);
    }

    @Override
    public List<InstanceSampleSpecProvider<DefaultInstanceSampleSpec>> infosFor(MetricNamed named) {
        return parent.infosFor(named);
    }
}
