package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.infoProviders.*;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.*;
import com.ringcentral.platform.metrics.samples.*;

import java.util.List;

public class PrometheusInstanceSampleSpecModsProvider implements PredicativeMetricNamedInfoProvider<
    InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> {

    private final PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> parent;

    public PrometheusInstanceSampleSpecModsProvider() {
        this(new MaskTreeMetricNamedInfoProvider<>());
    }

    public PrometheusInstanceSampleSpecModsProvider(PredicativeMetricNamedInfoProvider<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> parent) {
        this.parent = parent;
    }

    public PrometheusInstanceSampleSpecModsProvider addMod(
        MetricNamedPredicateBuilder<?> predicateBuilder,
        InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> mod) {

        return addMod(predicateBuilder.build(), mod);
    }

    public PrometheusInstanceSampleSpecModsProvider addMod(
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> mod) {

        return addInfo(predicate, mod);
    }

    @Override
    public PrometheusInstanceSampleSpecModsProvider addInfo(
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> mod) {

        parent.addInfo(predicate, mod);
        return this;
    }

    public List<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> modsFor(MetricInstance instance) {
        return infosFor(instance);
    }

    @Override
    public List<InstanceSampleSpecProvider<PrometheusInstanceSampleSpec>> infosFor(MetricNamed named) {
        return parent.infosFor(named);
    }
}
