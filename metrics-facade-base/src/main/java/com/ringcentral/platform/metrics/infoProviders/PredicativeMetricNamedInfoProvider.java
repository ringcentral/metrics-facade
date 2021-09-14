package com.ringcentral.platform.metrics.infoProviders;

import com.ringcentral.platform.metrics.predicates.*;

public interface PredicativeMetricNamedInfoProvider<I> extends MetricNamedInfoProvider<I> {
    default PredicativeMetricNamedInfoProvider<I> addInfo(MetricNamedPredicateBuilder<?> predicateBuilder, I info) {
        return addInfo(predicateBuilder.build(), info);
    }

    PredicativeMetricNamedInfoProvider<I> addInfo(MetricNamedPredicate predicate, I info);
}
