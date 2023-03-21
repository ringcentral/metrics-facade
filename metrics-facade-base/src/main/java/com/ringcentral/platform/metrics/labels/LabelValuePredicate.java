package com.ringcentral.platform.metrics.labels;

public interface LabelValuePredicate {
    Label label();
    boolean matches(String value);
}
