package com.ringcentral.platform.metrics.rate;

import com.ringcentral.platform.metrics.Meter;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.measurables.MeasurableType;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.NO_DIMENSION_VALUES;
import static com.ringcentral.platform.metrics.measurables.MeasurableType.*;

public interface Rate extends Meter {
    class MeanRate implements RateMeasurable {

        static final int HASH_CODE = "Rate.MeanRate".hashCode();

        @Override
        public MeasurableType type() {
            return DOUBLE;
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

    MeanRate MEAN_RATE = new MeanRate();

    class OneMinuteRate implements RateMeasurable {

        static final int HASH_CODE = "Rate.OneMinuteRate".hashCode();

        @Override
        public MeasurableType type() {
            return DOUBLE;
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

    OneMinuteRate ONE_MINUTE_RATE = new OneMinuteRate();

    class FiveMinutesRate implements RateMeasurable {

        static final int HASH_CODE = "Rate.FiveMinutesRate".hashCode();

        @Override
        public MeasurableType type() {
            return DOUBLE;
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

    FiveMinutesRate FIVE_MINUTES_RATE = new FiveMinutesRate();

    class FifteenMinutesRate implements RateMeasurable {

        static final int HASH_CODE = "Rate.FifteenMinutesRate".hashCode();

        @Override
        public MeasurableType type() {
            return DOUBLE;
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

    FifteenMinutesRate FIFTEEN_MINUTES_RATE = new FifteenMinutesRate();

    class RateUnit implements RateMeasurable {

        static final int HASH_CODE = "Rate.RateUnit".hashCode();

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

    RateUnit RATE_UNIT = new RateUnit();

    default void mark() {
        mark(NO_DIMENSION_VALUES);
    }

    default void mark(MetricDimensionValues dimensionValues) {
        mark(1L, dimensionValues);
    }

    default void mark(long count) {
        mark(count, NO_DIMENSION_VALUES);
    }

    void mark(long count, MetricDimensionValues dimensionValues);
}