package com.ringcentral.platform.metrics.labels;

import java.util.List;
import java.util.Map;

import static com.ringcentral.platform.metrics.utils.CollectionUtils.isNonEmpty;
import static com.ringcentral.platform.metrics.utils.CollectionUtils.isNullOrEmpty;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class LabelUtils {

    public static boolean hasLabel(List<LabelValue> labelValues, Label label) {
        return labelValueOf(labelValues, label) != null;
    }

    public static String valueOf(List<LabelValue> labelValues, Label label) {
        LabelValue lv = labelValueOf(labelValues, label);
        return lv != null ? lv.value() : null;
    }

    public static LabelValue labelValueOf(List<LabelValue> labelValues, Label label) {
        if (isNullOrEmpty(labelValues)) {
            return null;
        }

        for (int i = 0; i < labelValues.size(); ++i) {
            LabelValue lv = labelValues.get(i);

            if (lv.label().equals(label)) {
                return lv;
            }
        }

        return null;
    }

    public static Map<Label, LabelValue> labelToValue(List<LabelValue> labelValues) {
        return
            isNonEmpty(labelValues) ?
            labelValues.stream().collect(toMap(LabelValue::label, lv -> lv)) :
            emptyMap();
    }

    public static List<LabelValue> labelValuesWithout(
        List<LabelValue> labelValues,
        Label label,
        Label... labels) {

        if (isNullOrEmpty(labelValues)) {
            return emptyList();
        }

        return labelValues.stream().filter(lv -> {
            if (label.equals(lv.label())) {
                return false;
            }

            if (labels != null && labels.length > 0) {
                for (int i = 0; i < labels.length; ++i) {
                    if (labels[i].equals(lv.label())) {
                        return false;
                    }
                }
            }

            return true;
        }).collect(toList());
    }
}
