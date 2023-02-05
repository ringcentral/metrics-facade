package com.ringcentral.platform.metrics.labels;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

public class AllLabelValuesPredicate implements LabelValuesPredicate {

    private final Map<Label, List<LabelValuePredicate>> labelToPredicates;

    public static AllLabelValuesPredicate labelValuesMatchingAll(Collection<? extends LabelValuePredicate> labelsPredicates) {
        return labelValuesMatchingAll(labelsPredicates.toArray(new LabelValuePredicate[0]));
    }

    public static AllLabelValuesPredicate labelValuesMatchingAll(LabelValuePredicate... labelsPredicates) {
        return new AllLabelValuesPredicate(labelsPredicates);
    }

    public AllLabelValuesPredicate(LabelValuePredicate[] labelsPredicates) {
        this.labelToPredicates =
            labelsPredicates != null && labelsPredicates.length > 0 ?
            Map.copyOf(Arrays.stream(labelsPredicates).collect(groupingBy(LabelValuePredicate::label))) :
            null;
    }

    @Override
    public boolean matches(LabelValues labelValues) {
        if (labelToPredicates == null) {
            return true;
        }

        List<LabelValue> valueList = labelValues.list();

        for (int i = 0; i < valueList.size(); ++i) {
            Label label = valueList.get(i).label();
            List<LabelValuePredicate> labelPredicates = labelToPredicates.get(label);

            if (labelPredicates != null) {
                String value = valueList.get(i).value();

                for (int j = 0; j < labelPredicates.size(); ++j) {
                    if (!labelPredicates.get(j).matches(value)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
