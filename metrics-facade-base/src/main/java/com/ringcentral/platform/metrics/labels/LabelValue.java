package com.ringcentral.platform.metrics.labels;

import static java.util.Objects.*;

public class LabelValue {

    private final Label label;
    private final String value;
    private final int hashCode;

    public LabelValue(Label label, String value) {
        this.label = requireNonNull(label);
        this.value = requireNonNull(value);
        this.hashCode = hash(label, value);
    }

    public Label label() {
        return label;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        LabelValue that = (LabelValue)other;

        if (hashCode != that.hashCode) {
            return false;
        }

        return label.equals(that.label) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "LabelValue{" +
            "label=" + label +
            ", value='" + value + '\'' +
            '}';
    }
}
