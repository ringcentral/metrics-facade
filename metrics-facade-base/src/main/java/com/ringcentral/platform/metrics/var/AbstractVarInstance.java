package com.ringcentral.platform.metrics.var;

import com.ringcentral.platform.metrics.NotMeasuredException;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;

public abstract class AbstractVarInstance<V> implements VarInstance<V> {

    private final MetricName name;
    private final List<MetricDimensionValue> dimensionValues;
    private final boolean totalInstance;
    private final boolean dimensionalTotalInstance;
    private final boolean nonDecreasing;
    private final Set<Measurable> measurables;
    private final MeasurableValues measurableValues;

    public AbstractVarInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean nonDecreasing,
        Measurable valueMeasurable,
        Supplier<V> valueSupplier) {

        this.name = name;
        this.dimensionValues = dimensionValues != null ? dimensionValues : emptyList();
        this.totalInstance = totalInstance;
        this.dimensionalTotalInstance = dimensionalTotalInstance;
        this.nonDecreasing = nonDecreasing;
        this.measurables = Set.of(valueMeasurable);

        this.measurableValues = new MeasurableValues() {

            @Override
            @SuppressWarnings("unchecked")
            public <T> T valueOf(Measurable measurable) throws NotMeasuredException {
                if (measurable != valueMeasurable) {
                    throw NotMeasuredException.forMeasurable(measurable);
                }

                return (T)valueSupplier.get();
            }
        };
    }

    @Override
    public MetricName name() {
        return name;
    }

    @Override
    public List<MetricDimensionValue> dimensionValues() {
        return dimensionValues;
    }

    @Override
    public boolean isTotalInstance() {
        return totalInstance;
    }

    @Override
    public boolean isDimensionalTotalInstance() {
        return dimensionalTotalInstance;
    }

    @Override
    public boolean isLevelInstance() {
        return false;
    }

    @Override
    public boolean isNonDecreasing() {
        return nonDecreasing;
    }

    @Override
    public Set<Measurable> measurables() {
        return measurables;
    }

    @Override
    public MeasurableValues measurableValues() {
        return measurableValues;
    }

    @Override
    public <T> T valueOf(Measurable measurable) throws NotMeasuredException {
        return measurableValues.valueOf(measurable);
    }
}
