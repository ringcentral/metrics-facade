package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.infoProviders.MaskTreeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicateBuilder;

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

        return addMod(null, predicateBuilder, mod);
    }

    /**
     * Adds the specified modification by the specified key.
     * You can further remove the modification using the {@link #removeMod} method after it's no longer needed.
     *
     * @param key key with which the modification is to be associated.
     *            May be null, in which case there will be no association, and you won't be able to remove the modification.
     */
    public DefaultSampleSpecModsProvider addMod(
        String key,
        MetricNamedPredicateBuilder<?> predicateBuilder,
        SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec> mod) {

        return addMod(key, predicateBuilder.build(), mod);
    }

    public DefaultSampleSpecModsProvider addMod(
        MetricNamedPredicate predicate,
        SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec> mod) {

        return addMod(null, predicate, mod);
    }

    /**
     * Adds the specified modification by the specified key.
     * You can further remove the modification using the {@link #removeMod} method after it's no longer needed.
     *
     * @param key key with which the modification is to be associated.
     *            May be null, in which case there will be no association, and you won't be able to remove the modification.
     */
    public DefaultSampleSpecModsProvider addMod(
        String key,
        MetricNamedPredicate predicate,
        SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec> mod) {

        return addInfo(key, predicate, mod);
    }

    @Override
    public DefaultSampleSpecModsProvider addInfo(
        MetricNamedPredicate predicate,
        SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec> info) {

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
    public DefaultSampleSpecModsProvider addInfo(
        String key,
        MetricNamedPredicate predicate,
        SampleSpecProvider<DefaultSampleSpec, DefaultInstanceSampleSpec> info) {

        parent.addInfo(key, predicate, info);
        return this;
    }

    /**
     * Removes the modification previously added by the specified key.
     */
    public DefaultSampleSpecModsProvider removeMod(String key) {
        return removeMod(key);
    }

    @Override
    public DefaultSampleSpecModsProvider removeInfo(String key) {
        parent.removeInfo(key);
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
