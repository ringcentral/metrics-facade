package com.ringcentral.platform.metrics.labels;

import java.util.function.Predicate;
import static java.util.Objects.*;

public class Label {

    private final String name;
    private final int hashCode;

    public Label(String name) {
        this.name = requireNonNull(name);
        this.hashCode = name.hashCode();
    }

    public String name() {
        return name;
    }

    public LabelValue value(String v) {
        return new LabelValue(this, v);
    }

    public LabelValueMask mask(String m) {
        return LabelValueMask.of(this, m);
    }

    public LabelValuePredicate predicate(Predicate<String> p) {
        return new DefaultLabelValuePredicate(this, p);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Label that = (Label)other;

        if (hashCode != that.hashCode) {
            return false;
        }

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "Label{" +
            "name='" + name + '\'' +
            '}';
    }
}
