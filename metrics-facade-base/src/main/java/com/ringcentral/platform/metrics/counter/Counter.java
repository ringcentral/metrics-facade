package com.ringcentral.platform.metrics.counter;

import com.ringcentral.platform.metrics.Meter;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.measurables.MeasurableType;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;
import static com.ringcentral.platform.metrics.measurables.MeasurableType.*;

public interface Counter extends Meter {
    class Count implements CounterMeasurable {

        @Override
        public MeasurableType type() {
            return LONG;
        }
    }

    Count COUNT = new Count();

    default void inc() {
        inc(NO_DIMENSION_VALUES);
    }

    default void inc(MetricDimensionValues dimensionValues) {
        inc(1L, dimensionValues);
    }

    default void inc(long count) {
        inc(count, NO_DIMENSION_VALUES);
    }

    void inc(long count, MetricDimensionValues dimensionValues);

    default void dec() {
        dec(NO_DIMENSION_VALUES);
    }

    default void dec(MetricDimensionValues dimensionValues) {
        dec(1L, dimensionValues);
    }

    default void dec(long count) {
        dec(count, NO_DIMENSION_VALUES);
    }

    void dec(long count, MetricDimensionValues dimensionValues);
}
