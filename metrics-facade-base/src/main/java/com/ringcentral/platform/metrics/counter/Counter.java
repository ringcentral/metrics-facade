package com.ringcentral.platform.metrics.counter;

import com.ringcentral.platform.metrics.Meter;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.measurables.MeasurableType;

import static com.ringcentral.platform.metrics.labels.LabelValues.NO_LABEL_VALUES;
import static com.ringcentral.platform.metrics.measurables.MeasurableType.LONG;

public interface Counter extends Meter {
    class Count implements CounterMeasurable {

        static final int HASH_CODE = "Counter.Count".hashCode();

        @Override
        public MeasurableType type() {
            return LONG;
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

    Count COUNT = new Count();

    default void inc() {
        inc(NO_LABEL_VALUES);
    }

    default void inc(LabelValues labelValues) {
        inc(1L, labelValues);
    }

    default void inc(long count) {
        inc(count, NO_LABEL_VALUES);
    }

    void inc(long count, LabelValues labelValues);

    default void dec() {
        dec(NO_LABEL_VALUES);
    }

    default void dec(LabelValues labelValues) {
        dec(1L, labelValues);
    }

    default void dec(long count) {
        dec(count, NO_LABEL_VALUES);
    }

    void dec(long count, LabelValues labelValues);
}
