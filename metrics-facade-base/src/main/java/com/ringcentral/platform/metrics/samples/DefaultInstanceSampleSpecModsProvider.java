package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.infoProviders.MaskTreeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicateBuilder;

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

        return addMod(null, predicateBuilder, mod);
    }

    /**
     * Adds the specified modification by the specified key.
     * You can further remove the modification using the {@link #removeMod} method after it's no longer needed.
     *
     * @param key key with which the modification is to be associated.
     *            May be null, in which case there will be no association, and you won't be able to remove the modification.
     */
    public DefaultInstanceSampleSpecModsProvider addMod(
        String key,
        MetricNamedPredicateBuilder<?> predicateBuilder,
        InstanceSampleSpecProvider<DefaultInstanceSampleSpec> mod) {

        return addMod(key, predicateBuilder.build(), mod);
    }

    public DefaultInstanceSampleSpecModsProvider addMod(
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<DefaultInstanceSampleSpec> mod) {

        return addMod(null, predicate, mod);
    }

    /**
     * Adds the specified modification by the specified key.
     * You can further remove the modification using the {@link #removeMod} method after it's no longer needed.
     *
     * @param key key with which the modification is to be associated.
     *            May be null, in which case there will be no association, and you won't be able to remove the modification.
     */
    public DefaultInstanceSampleSpecModsProvider addMod(
        String key,
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<DefaultInstanceSampleSpec> mod) {

        return addInfo(key, predicate, mod);
    }

    @Override
    public DefaultInstanceSampleSpecModsProvider addInfo(
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<DefaultInstanceSampleSpec> info) {

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
    public DefaultInstanceSampleSpecModsProvider addInfo(
        String key,
        MetricNamedPredicate predicate,
        InstanceSampleSpecProvider<DefaultInstanceSampleSpec> info) {

        parent.addInfo(key, predicate, info);
        return this;
    }

    /**
     * Removes the modification previously added by the specified key.
     */
    public DefaultInstanceSampleSpecModsProvider removeMod(String key) {
        return removeInfo(key);
    }

    @Override
    public DefaultInstanceSampleSpecModsProvider removeInfo(String key) {
        parent.removeInfo(key);
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
