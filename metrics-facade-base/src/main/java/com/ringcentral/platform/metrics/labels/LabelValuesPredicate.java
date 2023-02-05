package com.ringcentral.platform.metrics.labels;

public interface LabelValuesPredicate {
    boolean matches(LabelValues labelValues);
}
