package com.ringcentral.platform.metrics.timer;

import com.ringcentral.platform.metrics.Meter;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.measurables.MeasurableType;

import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.labels.LabelValues.*;
import static com.ringcentral.platform.metrics.measurables.MeasurableType.*;

public interface Timer extends Meter {
    class DurationUnit implements TimerMeasurable {

        static final int HASH_CODE = "Timer.DurationUnit".hashCode();

        @Override
        public MeasurableType type() {
            return STRING;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other != null && getClass() == other.getClass());
        }

        @Override
        public int hashCode() {
            return HASH_CODE;
        }
    }

    DurationUnit DURATION_UNIT = new DurationUnit();

    default void update(long duration) {
        update(duration, NO_LABEL_VALUES);
    }

    default void update(long duration, TimeUnit unit) {
        update(duration, unit, NO_LABEL_VALUES);
    }

    default void update(long duration, TimeUnit unit, LabelValues labelValues) {
        update(unit.toNanos(duration), labelValues);
    }

    void update(long duration, LabelValues labelValues);

    default Stopwatch stopwatch() {
        return stopwatch(null);
    }

    Stopwatch stopwatch(LabelValues labelValues);
}
