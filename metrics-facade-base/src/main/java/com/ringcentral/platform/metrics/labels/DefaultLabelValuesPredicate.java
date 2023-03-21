package com.ringcentral.platform.metrics.labels;

import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class DefaultLabelValuesPredicate implements LabelValuesPredicate {

    private final Predicate<LabelValues> predicate;

    public static DefaultLabelValuesPredicate labelValuesMatching(Predicate<LabelValues> predicate) {
        return new DefaultLabelValuesPredicate(predicate);
    }

    public DefaultLabelValuesPredicate(Predicate<LabelValues> predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public boolean matches(LabelValues labelValues) {
        return predicate.test(labelValues);
    }
}
