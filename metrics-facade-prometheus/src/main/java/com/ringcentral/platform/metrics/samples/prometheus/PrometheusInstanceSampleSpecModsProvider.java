package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.infoProviders.MaskTreeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicateBuilder;
import com.ringcentral.platform.metrics.samples.InstanceSampleSpecProvider;

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

        return addMod(null, predicateBuilder, mod);
    }

    /**
     * Adds the specified modification by the specified key.
     * You can further remove the modification using the {@link #removeMod} method after it's no longer needed.
     *
     * @param key key with which the modification is to be associated.
     *            May be null, in which case there will be no association, and you won't be able to remove the modification.
     */
    public PrometheusInstanceSampleSpecModsProvider addMod(
        String key,
        MetricNamedPredicateBuilder<?> predicateBuilder,
        InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> mod) {

        return addMod(key, predicateBuilder.build(), mod);
    }

    public PrometheusInstanceSampleSpecModsProvider addMod(
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> mod) {

        return addMod(null, predicate, mod);
    }

    /**
     * Adds the specified modification by the specified key.
     * You can further remove the modification using the {@link #removeMod} method after it's no longer needed.
     *
     * @param key key with which the modification is to be associated.
     *            May be null, in which case there will be no association, and you won't be able to remove the modification.
     */
    public PrometheusInstanceSampleSpecModsProvider addMod(
        String key,
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> mod) {

        return addInfo(key, predicate, mod);
    }

    @Override
    public PrometheusInstanceSampleSpecModsProvider addInfo(
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> info) {

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
    public PrometheusInstanceSampleSpecModsProvider addInfo(
        String key,
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<PrometheusInstanceSampleSpec> info) {

        parent.addInfo(key, predicate, info);
        return this;
    }

    /**
     * Removes the modification previously added by the specified key.
     */
    public PrometheusInstanceSampleSpecModsProvider removeMod(String key) {
        return removeInfo(key);
    }

    @Override
    public PrometheusInstanceSampleSpecModsProvider removeInfo(String key) {
        parent.removeInfo(key);
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
