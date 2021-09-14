package com.ringcentral.platform.metrics.predicates;

public interface MetricNamedPredicateBuilder<P extends MetricNamedPredicate> {
    P build();
}
