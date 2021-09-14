package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.infoProviders.*;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.*;
import com.ringcentral.platform.metrics.samples.SampleSpecProvider;

import java.util.List;

public class PrometheusSampleSpecModsProvider implements PredicativeMetricNamedInfoProvider<
    SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>> {

    private final PredicativeMetricNamedInfoProvider<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>> parent;

    public PrometheusSampleSpecModsProvider() {
        this(new MaskTreeMetricNamedInfoProvider<>());
    }

    public PrometheusSampleSpecModsProvider(PredicativeMetricNamedInfoProvider<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>> parent) {
        this.parent = parent;
    }

    public PrometheusSampleSpecModsProvider addMod(
        MetricNamedPredicateBuilder<?> predicateBuilder,
        SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec> mod) {

        return addMod(predicateBuilder.build(), mod);
    }

    public PrometheusSampleSpecModsProvider addMod(
        MetricNamedPredicate predicate,
        SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec> mod) {

        parent.addInfo(predicate, mod);
        return this;
    }

    @Override
    public PrometheusSampleSpecModsProvider addInfo(
        MetricNamedPredicate predicate,
        SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec> info) {

        parent.addInfo(predicate, info);
        return this;
    }

    public List<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>> modsFor(MetricInstance instance) {
        return infosFor(instance);
    }

    @Override
    public List<SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec>> infosFor(MetricNamed named) {
        return parent.infosFor(named);
    }
}
