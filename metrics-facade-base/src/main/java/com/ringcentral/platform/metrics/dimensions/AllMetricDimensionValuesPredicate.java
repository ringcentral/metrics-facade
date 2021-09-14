package com.ringcentral.platform.metrics.dimensions;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

public class AllMetricDimensionValuesPredicate implements MetricDimensionValuesPredicate {

    private final Map<MetricDimension, List<MetricDimensionValuePredicate>> dimensionToPredicates;

    public static AllMetricDimensionValuesPredicate dimensionValuesMatchingAll(Collection<? extends MetricDimensionValuePredicate> dimensionsPredicates) {
        return dimensionValuesMatchingAll(dimensionsPredicates.toArray(new MetricDimensionValuePredicate[0]));
    }

    public static AllMetricDimensionValuesPredicate dimensionValuesMatchingAll(MetricDimensionValuePredicate... dimensionsPredicates) {
        return new AllMetricDimensionValuesPredicate(dimensionsPredicates);
    }

    public AllMetricDimensionValuesPredicate(MetricDimensionValuePredicate[] dimensionsPredicates) {
        this.dimensionToPredicates =
            dimensionsPredicates != null && dimensionsPredicates.length > 0 ?
            Map.copyOf(Arrays.stream(dimensionsPredicates).collect(groupingBy(MetricDimensionValuePredicate::dimension))) :
            null;
    }

    @Override
    public boolean matches(MetricDimensionValues dimensionValues) {
        if (dimensionToPredicates == null) {
            return true;
        }

        List<MetricDimensionValue> valueList = dimensionValues.list();

        for (int i = 0; i < valueList.size(); ++i) {
            MetricDimension dimension = valueList.get(i).dimension();
            List<MetricDimensionValuePredicate> dimensionPredicates = dimensionToPredicates.get(dimension);

            if (dimensionPredicates != null) {
                String value = valueList.get(i).value();

                for (int j = 0; j < dimensionPredicates.size(); ++j) {
                    if (!dimensionPredicates.get(j).matches(value)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
