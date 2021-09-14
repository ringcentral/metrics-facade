package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.measurables.MeasurableValues;
import com.ringcentral.platform.metrics.names.MetricNamed;

import java.util.*;

import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public interface MetricInstance extends MetricNamed {
    default boolean hasDimensionValues() {
        return dimensionValues() != null && !dimensionValues().isEmpty();
    }

    List<MetricDimensionValue> dimensionValues();

    default boolean hasDimension(MetricDimension dimension) {
        return hasDimensionValues() && dimensionToValue().containsKey(dimension);
    }

    default String valueOf(MetricDimension dimension) {
        if (!hasDimensionValues()) {
            return null;
        }

        MetricDimensionValue dv = dimensionToValue().get(dimension);
        return dv != null ? dv.value() : null;
    }

    default Map<MetricDimension, MetricDimensionValue> dimensionToValue() {
        return
            hasDimensionValues() ?
            dimensionValues().stream().collect(toMap(MetricDimensionValue::dimension, dv -> dv)) :
            emptyMap();
    }

    default List<MetricDimensionValue> dimensionValuesWithout(MetricDimension dimension, MetricDimension... dimensions) {
        if (!hasDimensionValues()) {
            return emptyList();
        }

        return dimensionValues().stream().filter(dv -> {
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

    boolean isTotalInstance();
    boolean isDimensionalTotalInstance();
    boolean isLevelInstance();

    Set<Measurable> measurables();
    MeasurableValues measurableValues();
    <V> V valueOf(Measurable measurable) throws NotMeasuredException;
}
