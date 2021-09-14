package com.ringcentral.platform.metrics.predicates;

import com.ringcentral.platform.metrics.names.MetricNamed;

public interface MetricNamedPredicate {
    boolean matches(MetricNamed named);
}
