package com.ringcentral.platform.metrics.labels;

import java.util.function.Predicate;

import static java.util.Objects.*;

public class DefaultLabelValuePredicate implements LabelValuePredicate {

    private final Label label;
    private final Predicate<String> predicate;

    public DefaultLabelValuePredicate(Label label, Predicate<String> predicate) {
        this.label = requireNonNull(label);
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public Label label() {
        return label;
    }

    @Override
    public boolean matches(String value) {
        return predicate.test(value);
    }
}
