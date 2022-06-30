package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionUtils;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.measurables.MeasurableValues;
import com.ringcentral.platform.metrics.names.MetricNamed;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MetricInstance extends MetricNamed {
    default boolean hasDimensionValues() {
        return MetricDimensionUtils.hasDimensionValues(dimensionValues());
    }

    List<MetricDimensionValue> dimensionValues();

    default boolean hasDimension(MetricDimension dimension) {
        return MetricDimensionUtils.hasDimension(dimensionValues(), dimension);
    }

    default String valueOf(MetricDimension dimension) {
        return MetricDimensionUtils.valueOf(dimensionValues(), dimension);
    }

    default Map<MetricDimension, MetricDimensionValue> dimensionToValue() {
        return MetricDimensionUtils.dimensionToValue(dimensionValues());
    }

    default List<MetricDimensionValue> dimensionValuesWithout(MetricDimension dimension, MetricDimension... dimensions) {
        return MetricDimensionUtils.dimensionValuesWithout(dimensionValues(), dimension, dimensions);
    }

    boolean isTotalInstance();
    boolean isDimensionalTotalInstance();
    boolean isLevelInstance();

    Set<Measurable> measurables();
    MeasurableValues measurableValues();
    <V> V valueOf(Measurable measurable) throws NotMeasuredException;

    default void metricInstanceAdded() {}
    default void metricInstanceRemoved() {}
}
