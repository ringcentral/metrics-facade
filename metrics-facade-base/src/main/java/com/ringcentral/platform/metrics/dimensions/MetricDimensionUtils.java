package com.ringcentral.platform.metrics.dimensions;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class MetricDimensionUtils {

    public static boolean hasDimensionValues(List<MetricDimensionValue> dimensionValues) {
        return dimensionValues != null && !dimensionValues.isEmpty();
    }

    public static boolean hasDimension(List<MetricDimensionValue> dimensionValues, MetricDimension dimension) {
        return dimensionValueOf(dimensionValues, dimension) != null;
    }

    public static String valueOf(List<MetricDimensionValue> dimensionValues, MetricDimension dimension) {
        MetricDimensionValue dv = dimensionValueOf(dimensionValues, dimension);
        return dv != null ? dv.value() : null;
    }

    public static MetricDimensionValue dimensionValueOf(List<MetricDimensionValue> dimensionValues, MetricDimension dimension) {
        if (!hasDimensionValues(dimensionValues)) {
            return null;
        }

        for (int i = 0; i < dimensionValues.size(); ++i) {
            MetricDimensionValue dv = dimensionValues.get(i);

            if (dv.dimension().equals(dimension)) {
                return dv;
            }
        }

        return null;
    }

    public static Map<MetricDimension, MetricDimensionValue> dimensionToValue(List<MetricDimensionValue> dimensionValues) {
        return
            hasDimensionValues(dimensionValues) ?
            dimensionValues.stream().collect(toMap(MetricDimensionValue::dimension, dv -> dv)) :
            emptyMap();
    }

    public static List<MetricDimensionValue> dimensionValuesWithout(
        List<MetricDimensionValue> dimensionValues,
        MetricDimension dimension,
        MetricDimension... dimensions) {

        if (!hasDimensionValues(dimensionValues)) {
            return emptyList();
        }

        return dimensionValues.stream().filter(dv -> {
            if (dimension.equals(dv.dimension())) {
                return false;
            }

            if (dimensions != null && dimensions.length > 0) {
                for (int i = 0; i < dimensions.length; ++i) {
                    if (dimensions[i].equals(dv.dimension())) {
                        return false;
                    }
                }
            }

            return true;
        }).collect(toList());
    }
}
