package com.ringcentral.platform.metrics.rate;

import com.ringcentral.platform.metrics.Meter;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.measurables.MeasurableType;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;
import static com.ringcentral.platform.metrics.measurables.MeasurableType.*;

public interface Rate extends Meter {
    class MeanRate implements RateMeasurable {

        @Override
        public MeasurableType type() {
            return DOUBLE;
        }
    }

    MeanRate MEAN_RATE = new MeanRate();

    class OneMinuteRate implements RateMeasurable {

        @Override
        public MeasurableType type() {
            return DOUBLE;
        }
    }

    OneMinuteRate ONE_MINUTE_RATE = new OneMinuteRate();

    class FiveMinutesRate implements RateMeasurable {

        @Override
        public MeasurableType type() {
            return DOUBLE;
        }
    }

    FiveMinutesRate FIVE_MINUTES_RATE = new FiveMinutesRate();

    class FifteenMinutesRate implements RateMeasurable {

        @Override
        public MeasurableType type() {
            return DOUBLE;
        }
    }

    FifteenMinutesRate FIFTEEN_MINUTES_RATE = new FifteenMinutesRate();

    class RateUnit implements RateMeasurable {

        @Override
        public MeasurableType type() {
            return STRING;
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