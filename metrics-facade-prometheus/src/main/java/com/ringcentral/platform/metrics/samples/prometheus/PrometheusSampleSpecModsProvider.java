package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.infoProviders.MaskTreeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicateBuilder;
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

        return addMod(null, predicateBuilder, mod);
    }

    /**
     * Adds the specified modification by the specified key.
     * You can further remove the modification using the {@link #removeMod} method after it's no longer needed.
     *
     * @param key key with which the modification is to be associated.
     *            May be null, in which case there will be no association, and you won't be able to remove the modification.
     */
    public PrometheusSampleSpecModsProvider addMod(
        String key,
        MetricNamedPredicateBuilder<?> predicateBuilder,
        SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec> mod) {

        return addMod(key, predicateBuilder.build(), mod);
    }

    public PrometheusSampleSpecModsProvider addMod(
        MetricNamedPredicate predicate,
        SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec> mod) {

        return addMod(null, predicate, mod);
    }

    /**
     * Adds the specified modification by the specified key.
     * You can further remove the modification using the {@link #removeMod} method after it's no longer needed.
     *
     * @param key key with which the modification is to be associated.
     *            May be null, in which case there will be no association, and you won't be able to remove the modification.
     */
    public PrometheusSampleSpecModsProvider addMod(
        String key,
        MetricNamedPredicate predicate,
        SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec> mod) {

        return addInfo(key, predicate, mod);
    }

    @Override
    public PrometheusSampleSpecModsProvider addInfo(
        MetricNamedPredicate predicate,
        SampleSpecProvider<PrometheusSampleSpec, PrometheusInstanceSampleSpec> info) {

        return addInfo(null, predicate, info);
    }

    /**
     * Adds the specified modification by the specified key.
     * You can further remove the modification using the {@link #removeMod} method after it's no longer needed.
     *
     * @param key key with which the modification is to be associated.
     *            May be null, in which case there will be no association, and you won't be able to remove the modification.
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
     * Removes the modification previously added by the specified key.
     */
    public PrometheusSampleSpecModsProvider removeMod(String key) {
        return removeInfo(key);
    }

    @Override
    public PrometheusSampleSpecModsProvider removeInfo(String key) {
        parent.removeInfo(key);
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
