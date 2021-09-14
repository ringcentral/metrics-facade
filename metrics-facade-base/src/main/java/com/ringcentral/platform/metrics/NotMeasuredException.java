package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.measurables.Measurable;

public class NotMeasuredException extends RuntimeException {

    public static NotMeasuredException forMeasurable(Measurable m) {
        return new NotMeasuredException(m + " is not measured");
    }

    public NotMeasuredException(String message) {
        super(message);
    }
}
