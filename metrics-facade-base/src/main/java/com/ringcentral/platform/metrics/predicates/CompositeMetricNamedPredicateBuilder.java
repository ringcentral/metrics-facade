package com.ringcentral.platform.metrics.predicates;

import java.util.*;

public class CompositeMetricNamedPredicateBuilder implements MetricNamedPredicateBuilder<CompositeMetricNamedPredicate> {

    private List<MetricNamedPredicate> inPredicates;
    private List<MetricNamedPredicate> exPredicates;

    public static CompositeMetricNamedPredicateBuilder forMetrics() {
        return metrics();
    }

    public static CompositeMetricNamedPredicateBuilder metrics() {
        return compositeMetricNamedPredicateBuilder();
    }

    public static CompositeMetricNamedPredicateBuilder compositeMetricNamedPredicateBuilder() {
        return new CompositeMetricNamedPredicateBuilder(null, null);
    }

    public CompositeMetricNamedPredicateBuilder(
        List<MetricNamedPredicate> inPredicates,
        List<MetricNamedPredicate> exPredicates) {

        this.inPredicates = inPredicates;
        this.exPredicates = exPredicates;
    }

    public CompositeMetricNamedPredicateBuilder including(MetricNamedPredicate predicate) {
        if (inPredicates == null) {
            inPredicates = new ArrayList<>();
        }

        inPredicates.add(predicate);
        return this;
    }

    public CompositeMetricNamedPredicateBuilder excluding(MetricNamedPredicate predicate) {
        if (exPredicates == null) {
            exPredicates = new ArrayList<>();
        }

        exPredicates.add(predicate);
        return this;
    }

    @Override
    public CompositeMetricNamedPredicate build() {
        return new CompositeMetricNamedPredicate(inPredicates, exPredicates);
    }
}
