package com.ringcentral.platform.metrics.measurables;

import com.ringcentral.platform.metrics.NotMeasuredException;

public interface MeasurableValues {
    <V> V valueOf(Measurable measurable) throws NotMeasuredException;
}
