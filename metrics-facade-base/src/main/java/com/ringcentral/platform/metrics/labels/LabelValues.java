package com.ringcentral.platform.metrics.labels;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class LabelValues {

    public static final LabelValues NO_LABEL_VALUES = labelValues();

    private final List<LabelValue> list;

    public static LabelValues noLabelValues() {
        return NO_LABEL_VALUES;
    }

    public static LabelValues forLabelValues(LabelValue... values) {
        return labelValues(values);
    }

    public static LabelValues forLabelValues(List<LabelValue> values) {
        return labelValues(values);
    }

    public static LabelValues labelValues(LabelValue... values) {
        return new LabelValues(values);
    }

    public static LabelValues labelValues(List<LabelValue> values) {
        return new LabelValues(values);
    }

    public static LabelValues labelValues(LabelValues prefix, LabelValue... suffix) {
        if (suffix == null || suffix.length == 0) {
            return prefix;
        }

        if (prefix == null || prefix.isEmpty()) {
            return labelValues(suffix);
        }

        List<LabelValue> values = new ArrayList<>(prefix.size() + suffix.length);
        values.addAll(prefix.list());
        values.addAll(List.of(suffix));
        return new LabelValues(values);
    }

    private LabelValues(Collection<? extends LabelValue> values) {
        this.list = List.copyOf(values);
    }

    private LabelValues(LabelValue[] values) {
        this.list = List.of(values);
    }

    public List<LabelValue> list() {
        return list;
    }

    /**
     * @return list of the underlying {@link Label}s
     */
    public List<Label> labels() {
        return list.stream().map(LabelValue::label).collect(toList());
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        LabelValues that = (LabelValues)other;
        return list.equals(that.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public String toString() {
        return "LabelValues{" +
            "list=" + list +
            '}';
    }
}
