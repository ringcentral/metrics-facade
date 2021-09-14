package com.ringcentral.platform.metrics.timer;

import com.ringcentral.platform.metrics.Meter;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.measurables.MeasurableType;

import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;
import static com.ringcentral.platform.metrics.measurables.MeasurableType.*;

public interface Timer extends Meter {
    class DurationUnit implements TimerMeasurable {

        @Override
        public MeasurableType type() {
            return STRING;
        }
    }

    DurationUnit DURATION_UNIT = new DurationUnit();

    default void update(long duration) {
        update(duration, NO_DIMENSION_VALUES);
    }

    default void update(long duration, TimeUnit unit) {
        update(duration, unit, NO_DIMENSION_VALUES);
    }

    default void update(long duration, TimeUnit unit, MetricDimensionValues dimensionValues) {
        update(unit.toNanos(duration), dimensionValues);
    }

    void update(long duration, MetricDimensionValues dimensionValues);

    default Stopwatch stopwatch() {
        return stopwatch(NO_DIMENSION_VALUES);
    }

    Stopwatch stopwatch(MetricDimensionValues dimensionValues);
}
