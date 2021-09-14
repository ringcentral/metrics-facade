package com.ringcentral.platform.metrics.measurables;

import com.ringcentral.platform.metrics.NotMeasuredException;

import java.util.Set;

public abstract class AbstractMeasurableValues implements MeasurableValues {

    private final Set<? extends Measurable> measurables;

    protected AbstractMeasurableValues(Set<? extends Measurable> measurables) {
        this.measurables = measurables;
    }

    @Override
    public <V> V valueOf(Measurable measurable) throws NotMeasuredException {
        if (measurables.contains(measurable)) {
            return valueOfImpl(measurable);
        } else {
            throw NotMeasuredException.forMeasurable(measurable);
        }
    }

    protected abstract <V> V valueOfImpl(Measurable measurable);
}
