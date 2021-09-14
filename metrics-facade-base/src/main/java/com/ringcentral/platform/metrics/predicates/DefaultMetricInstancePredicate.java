package com.ringcentral.platform.metrics.predicates;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.names.MetricNameMask;

import java.util.function.Predicate;

public class DefaultMetricInstancePredicate extends DefaultMetricNamedPredicate implements MetricInstancePredicate {

    public static DefaultMetricInstancePredicate forMetricInstancesMatching(MetricNameMask nameMask) {
        return metricInstancesMatching(nameMask, null);
    }

    public static DefaultMetricInstancePredicate metricInstancesMatching(MetricNameMask nameMask) {
        return metricInstancesMatching(nameMask, null);
    }

    public static DefaultMetricInstancePredicate forMetricInstancesMatching(MetricNameMask nameMask, Predicate<MetricInstance> additionalPredicate) {
        return metricInstancesMatching(nameMask, additionalPredicate);
    }

    public static DefaultMetricInstancePredicate metricInstancesMatching(MetricNameMask nameMask, Predicate<MetricInstance> additionalPredicate) {
        return new DefaultMetricInstancePredicate(nameMask, additionalPredicate);
    }

    public DefaultMetricInstancePredicate(MetricNameMask nameMask, Predicate<MetricInstance> additionalPredicate) {
        super(nameMask, additionalPredicate != null ? toMetricNamedPredicate(additionalPredicate) : null);
    }

    private static MetricNamedPredicate toMetricNamedPredicate(Predicate<MetricInstance> additionalPredicate) {
        return n -> n instanceof MetricInstance && additionalPredicate.test((MetricInstance)n);
    }
}
