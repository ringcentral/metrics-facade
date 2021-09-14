package com.ringcentral.platform.metrics.predicates;

import com.ringcentral.platform.metrics.names.MetricNamed;

import java.util.*;

import static java.util.Collections.*;

public class CompositeMetricNamedPredicate implements MetricNamedPredicate {

    private List<MetricNamedPredicate> inPredicates;
    private List<MetricNamedPredicate> exPredicates;

    public CompositeMetricNamedPredicate(
        List<MetricNamedPredicate> inPredicates,
        List<MetricNamedPredicate> exPredicates) {

        this.inPredicates = inPredicates;
        this.exPredicates = exPredicates;
    }

    public void addInclusionPredicate(MetricNamedPredicate predicate) {
        if (inPredicates == null) {
            inPredicates = new ArrayList<>();
        }

        inPredicates.add(predicate);
    }

    public void addExclusionPredicate(MetricNamedPredicate predicate) {
        if (exPredicates == null) {
            exPredicates = new ArrayList<>();
        }

        exPredicates.add(predicate);
    }

    public boolean hasInclusionPredicates() {
        return inPredicates != null && !inPredicates.isEmpty();
    }

    public List<MetricNamedPredicate> inclusionPredicates() {
        return hasInclusionPredicates() ? inPredicates : emptyList();
    }

    public boolean hasExclusionPredicates() {
        return exPredicates != null && !exPredicates.isEmpty();
    }

    public List<MetricNamedPredicate> exclusionPredicates() {
        return hasExclusionPredicates() ? exPredicates : emptyList();
    }

    @Override
    public boolean matches(MetricNamed named) {
        if (hasExclusionPredicates()) {
            for (MetricNamedPredicate p : exPredicates) {
                if (p.matches(named)) {
                    return false;
                }
            }
        }

        if (hasInclusionPredicates()) {
            for (MetricNamedPredicate p : inPredicates) {
                if (p.matches(named)) {
                    return true;
                }
            }

            return false;
        } else {
            return true;
        }
    }
}
