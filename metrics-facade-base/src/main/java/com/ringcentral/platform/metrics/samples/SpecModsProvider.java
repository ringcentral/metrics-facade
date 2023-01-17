package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicateBuilder;

import java.util.List;

public interface SpecModsProvider<M, MP extends SpecModsProvider<M, MP>> extends PredicativeMetricNamedInfoProvider<M> {
    default MP addMod(MetricNamedPredicateBuilder<?> predicateBuilder, M mod) {
        return addMod(null, predicateBuilder, mod);
    }

    /**
     * @see #addInfo(String, MetricNamedPredicate, Object)
     */
    default MP addMod(String key, MetricNamedPredicateBuilder<?> predicateBuilder, M mod) {
        return addMod(key, predicateBuilder.build(), mod);
    }

    default MP addMod(MetricNamedPredicate predicate, M mod) {
        return addMod(null, predicate, mod);
    }

    /**
     * @see #addInfo(String, MetricNamedPredicate, Object)
     */
    default MP addMod(String key, MetricNamedPredicate predicate, M mod) {
        return addInfo(key, predicate, mod);
    }

    @Override
    default MP addInfo(MetricNamedPredicate predicate, M info) {
        return addInfo(null, predicate, info);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MP addInfo(String key, MetricNamedPredicate predicate, M info);

    /**
     * @see #removeInfo(String)
     */
    default MP removeMod(String key) {
        return removeInfo(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MP removeInfo(String key);

    default List<M> modsFor(MetricInstance instance) {
        return infosFor(instance);
    }
}
