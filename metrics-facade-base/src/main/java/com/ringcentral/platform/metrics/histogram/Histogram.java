package com.ringcentral.platform.metrics.histogram;

import com.ringcentral.platform.metrics.Meter;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.measurables.MeasurableType;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.NO_DIMENSION_VALUES;
import static com.ringcentral.platform.metrics.measurables.MeasurableType.*;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.concurrent.TimeUnit.SECONDS;

public interface Histogram extends Meter {
    class TotalSum implements HistogramMeasurable {

        @Override
        public MeasurableType type() {
            return LONG;
        }
    }

    TotalSum TOTAL_SUM = new TotalSum();

    class Min implements HistogramMeasurable {

        @Override
        public MeasurableType type() {
            return LONG;
        }
    }

    Min MIN = new Min();

    class Max implements HistogramMeasurable {

        @Override
        public MeasurableType type() {
            return LONG;
        }
    }

    Max MAX = new Max();

    class Mean implements HistogramMeasurable {

        @Override
        public MeasurableType type() {
            return DOUBLE;
        }
    }

    Mean MEAN = new Mean();

    class StandardDeviation implements HistogramMeasurable {

        @Override
        public MeasurableType type() {
            return DOUBLE;
        }
    }

    StandardDeviation STANDARD_DEVIATION = new StandardDeviation();

    class Percentile implements HistogramMeasurable {

        private final double quantile;
        private final String quantileAsString;
        private final String quantileDecimalPartAsString;

        public Percentile(double quantile) {
            checkArgument(
                !(quantile < 0.0 || quantile > 1.0 || Double.isNaN(quantile)),
                quantile + " is not in [0.0 .. 1.0]");

            this.quantile = quantile;
            this.quantileAsString = Double.toString(quantile);
            String afterPoint = this.quantileAsString.substring(this.quantileAsString.indexOf(".") + 1);
            this.quantileDecimalPartAsString = afterPoint.length() > 1 ? afterPoint : afterPoint + "0";
        }

        public static Percentile of(double quantile) {
            return new Percentile(quantile);
        }

        @Override
        public MeasurableType type() {
            return DOUBLE;
        }

        public double quantile() {
            return quantile;
        }

        public String quantileAsString() {
            return quantileAsString;
        }

        public String quantileDecimalPartAsString() {
            return quantileDecimalPartAsString;
        }
    }

    Percentile PERCENTILE_5 = Percentile.of(0.05);
    Percentile PERCENTILE_10 = Percentile.of(0.1);
    Percentile PERCENTILE_15 = Percentile.of(0.15);
    Percentile PERCENTILE_20 = Percentile.of(0.2);
    Percentile PERCENTILE_25 = Percentile.of(0.25);
    Percentile PERCENTILE_30 = Percentile.of(0.3);
    Percentile PERCENTILE_35 = Percentile.of(0.35);
    Percentile PERCENTILE_40 = Percentile.of(0.4);
    Percentile PERCENTILE_45 = Percentile.of(0.45);
    Percentile PERCENTILE_50 = Percentile.of(0.5);
    Percentile PERCENTILE_55 = Percentile.of(0.55);
    Percentile PERCENTILE_60 = Percentile.of(0.6);
    Percentile PERCENTILE_65 = Percentile.of(0.65);
    Percentile PERCENTILE_70 = Percentile.of(0.7);
    Percentile PERCENTILE_75 = Percentile.of(0.75);
    Percentile PERCENTILE_80 = Percentile.of(0.8);
    Percentile PERCENTILE_85 = Percentile.of(0.85);
    Percentile PERCENTILE_90 = Percentile.of(0.9);
    Percentile PERCENTILE_95 = Percentile.of(0.95);
    Percentile PERCENTILE_99 = Percentile.of(0.99);
    Percentile PERCENTILE_999 = Percentile.of(0.999);

    class Bucket implements HistogramMeasurable {

        private final double inclusiveUpperBound;
        private final String inclusiveUpperBoundAsString;

        public Bucket(double inclusiveUpperBound) {
            this.inclusiveUpperBound = inclusiveUpperBound;
            this.inclusiveUpperBoundAsString = inclusiveUpperBoundAsString(inclusiveUpperBound);
        }

        static String inclusiveUpperBoundAsString(double b) {
            if (b == Double.POSITIVE_INFINITY) {
                return "+Inf";
            }

            if (b == Double.NEGATIVE_INFINITY) {
                return "-Inf";
            }

            return Double.toString(b);
        }

        public static Bucket of(double inclusiveUpperBound) {
            return new Bucket(inclusiveUpperBound);
        }

        @Override
        public MeasurableType type() {
            return LONG;
        }

        public double inclusiveUpperBound() {
            return inclusiveUpperBound;
        }

        public String inclusiveUpperBoundAsString() {
            return inclusiveUpperBoundAsString;
        }
    }

    long NANOS_PER_SEC = SECONDS.toNanos(1L);

    Bucket MS_5_BUCKET = new Bucket(.005 * NANOS_PER_SEC);
    Bucket MS_10_BUCKET = new Bucket(.01 * NANOS_PER_SEC);
    Bucket MS_25_BUCKET = new Bucket(.025 * NANOS_PER_SEC);
    Bucket MS_50_BUCKET = new Bucket(.05 * NANOS_PER_SEC);
    Bucket MS_75_BUCKET = new Bucket(.075 * NANOS_PER_SEC);
    Bucket MS_100_BUCKET = new Bucket(.1 * NANOS_PER_SEC);
    Bucket MS_250_BUCKET = new Bucket(.25 * NANOS_PER_SEC);
    Bucket MS_500_BUCKET = new Bucket(.5 * NANOS_PER_SEC);
    Bucket MS_750_BUCKET = new Bucket(.75 * NANOS_PER_SEC);
    Bucket SEC_1_BUCKET = new Bucket(1.0 * NANOS_PER_SEC);
    Bucket SEC_2_5_BUCKET = new Bucket(2.5 * NANOS_PER_SEC);
    Bucket SEC_5_BUCKET = new Bucket(5.0 * NANOS_PER_SEC);
    Bucket SEC_7_5_BUCKET = new Bucket(7.5 * NANOS_PER_SEC);
    Bucket SEC_10_BUCKET = new Bucket(10.0 * NANOS_PER_SEC);

    default void update(long value) {
        update(value, NO_DIMENSION_VALUES);
    }

    void update(long value, MetricDimensionValues dimensionValues);
}