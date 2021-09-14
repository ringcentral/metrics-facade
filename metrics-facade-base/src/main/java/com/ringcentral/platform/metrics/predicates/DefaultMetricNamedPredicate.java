package com.ringcentral.platform.metrics.predicates;

import com.ringcentral.platform.metrics.names.*;
import static java.util.Objects.*;

public class DefaultMetricNamedPredicate implements MetricNamedPredicate {

    private final MetricNameMask nameMask;
    private final MetricNamedPredicate additionalPredicate;

    public DefaultMetricNamedPredicate(
        MetricNameMask nameMask,
        MetricNamedPredicate additionalPredicate) {

        this.nameMask = requireNonNull(nameMask);
        this.additionalPredicate = additionalPredicate;
    }

    public MetricNameMask nameMask() {
        return nameMask;
    }

    public boolean hasAdditionalPredicate() {
        return additionalPredicate != null;
    }

    public MetricNamedPredicate additionalPredicate() {
        return additionalPredicate;
    }

    @Override
    public boolean matches(MetricNamed named) {
        return nameMask.matches(named)
            && (!hasAdditionalPredicate() || additionalPredicate.matches(named));
    }
}
