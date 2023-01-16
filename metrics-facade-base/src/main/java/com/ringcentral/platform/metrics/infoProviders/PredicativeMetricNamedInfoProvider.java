package com.ringcentral.platform.metrics.infoProviders;

import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicateBuilder;

public interface PredicativeMetricNamedInfoProvider<I> extends MetricNamedInfoProvider<I> {
    default PredicativeMetricNamedInfoProvider<I> addInfo(MetricNamedPredicateBuilder<?> predicateBuilder, I info) {
        return addInfo(predicateBuilder.build(), info);
    }

    default PredicativeMetricNamedInfoProvider<I> addInfo(MetricNamedPredicate predicate, I info) {
        return addInfo(null, predicate, info);
    }

    default PredicativeMetricNamedInfoProvider<I> addInfo(String key, MetricNamedPredicateBuilder<?> predicateBuilder, I info) {
        return addInfo(key, predicateBuilder.build(), info);
    }

    /**
     * Adds the specified info by the specified key.
     * You can further remove the info using the {@link #removeInfo} method after it's no longer needed.
     *
     * @param key key with which the info is to be associated.
     *            May be null, in which case there will be no association, and you won't be able to remove the info.
     */
    PredicativeMetricNamedInfoProvider<I> addInfo(String key, MetricNamedPredicate predicate, I info);

    /**
     * Removes the info previously added by the specified key.
     */
    PredicativeMetricNamedInfoProvider<I> removeInfo(String key);
}
