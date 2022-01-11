package com.ringcentral.platform.metrics.histogram;

import com.ringcentral.platform.metrics.Meter;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.measurables.MeasurableType;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.NO_DIMENSION_VALUES;
import static com.ringcentral.platform.metrics.measurables.MeasurableType.*;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static com.ringcentral.platform.metrics.utils.TimeUnitUtils.NANOS_PER_SEC;
import static java.lang.Math.*;
import static java.util.concurrent.TimeUnit.*;

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
        private final double percentile;

        public Percentile(double quantile) {
            checkArgument(
                !(quantile < 0.0 || quantile > 1.0 || Double.isNaN(quantile)),
                quantile + " is not in [0.0 .. 1.0]");

            this.quantile = quantile;
            this.quantileAsString = Double.toString(quantile);
            String afterPoint = this.quantileAsString.substring(this.quantileAsString.indexOf(".") + 1);
            this.quantileDecimalPartAsString = afterPoint.length() > 1 ? afterPoint : afterPoint + "0";
            this.percentile = min(max(quantile * 100.0, 0.0), 100.0);
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

        public double percentile() {
            return percentile;
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

        private final double upperBoundInUnits;
        private final TimeUnit upperBoundUnit;
        private final long upperBoundAsLong;
        private final String upperBoundAsString;
        private final String upperBoundAsStringWithUnit;
        private final String upperBoundSecAsString;

        public Bucket(double upperBound) {
            this(upperBound, null);
        }

        public Bucket(double upperBoundInUnits, TimeUnit upperBoundUnit) {
            this.upperBoundInUnits = upperBoundInUnits;
            this.upperBoundUnit = upperBoundUnit;

            double upperBound =
                Double.isInfinite(upperBoundInUnits) || upperBoundUnit == null || upperBoundUnit == NANOSECONDS ?
                upperBoundInUnits :
                upperBoundUnit.toNanos(1L) * upperBoundInUnits;

            this.upperBoundAsLong = Math.round(upperBound);
            this.upperBoundAsString = upperBoundAsString(upperBound);

            if (Double.isInfinite(upperBoundInUnits)) {
                this.upperBoundAsStringWithUnit = upperBoundAsString(upperBoundInUnits);
            } else {
                String upperBoundUnitAsString;

                if (upperBoundUnit == null || upperBoundUnit == NANOSECONDS) {
                    upperBoundUnitAsString = "ns";
                } else if (upperBoundUnit == MICROSECONDS) {
                    upperBoundUnitAsString = "us";
                } else if (upperBoundUnit == MILLISECONDS) {
                    upperBoundUnitAsString = "ms";
                } else if (upperBoundUnit == SECONDS) {
                    upperBoundUnitAsString = "sec";
                } else if (upperBoundUnit == HOURS) {
                    upperBoundUnitAsString = "h";
                } else if (upperBoundUnit == DAYS) {
                    upperBoundUnitAsString = "d";
                } else  {
                    upperBoundUnitAsString = upperBoundUnit.toString().toLowerCase(Locale.ENGLISH);
                }

                this.upperBoundAsStringWithUnit = upperBoundAsString(upperBoundInUnits) + upperBoundUnitAsString;
            }

            this.upperBoundSecAsString = String.valueOf(
                Double.isInfinite(upperBoundInUnits) || upperBoundUnit == SECONDS ?
                upperBoundInUnits :
                upperBoundInUnits * ((1.0 * (upperBoundUnit != null ? upperBoundUnit : NANOSECONDS).toNanos(1L)) / NANOS_PER_SEC));
        }

        static String upperBoundAsString(double b) {
            if (b == Double.POSITIVE_INFINITY) {
                return "inf";
            }

            if (b == Double.NEGATIVE_INFINITY) {
                return "negativeInf";
            }

            return Double.toString(b)
                .replaceAll("\\.0+$", "")
                .replace('.', 'p');
        }

        public static Bucket of(double upperBound) {
            return new Bucket(upperBound);
        }

        public static Bucket of(double upperBoundInUnits, TimeUnit upperBoundUnit) {
            return new Bucket(upperBoundInUnits, upperBoundUnit);
        }

        @Override
        public MeasurableType type() {
            return LONG;
        }

        /**
         * @return inclusive upper bound in units
         */
        public double upperBoundInUnits() {
            return upperBoundInUnits;
        }

        public TimeUnit upperBoundUnit() {
            return upperBoundUnit;
        }

        public long upperBoundAsLong() {
            return upperBoundAsLong;
        }

        public String upperBoundAsString() {
            return upperBoundAsString;
        }

        public String upperBoundAsStringWithUnit() {
            return upperBoundAsStringWithUnit;
        }

        public String upperBoundSecAsString() {
            return upperBoundSecAsString;
        }
    }

    Bucket MS_5_BUCKET = Bucket.of(5, MILLISECONDS);
    Bucket MS_10_BUCKET = Bucket.of(10, MILLISECONDS);
    Bucket MS_25_BUCKET = Bucket.of(25, MILLISECONDS);
    Bucket MS_50_BUCKET = Bucket.of(50, MILLISECONDS);
    Bucket MS_75_BUCKET = Bucket.of(75, MILLISECONDS);
    Bucket MS_100_BUCKET = Bucket.of(100, MILLISECONDS);
    Bucket MS_250_BUCKET = Bucket.of(250, MILLISECONDS);
    Bucket MS_500_BUCKET = Bucket.of(500, MILLISECONDS);
    Bucket MS_750_BUCKET = Bucket.of(750, MILLISECONDS);
    Bucket SEC_1_BUCKET = Bucket.of(1, SECONDS);
    Bucket SEC_2p5_BUCKET = Bucket.of(2.5, SECONDS);
    Bucket SEC_5_BUCKET = Bucket.of(5, SECONDS);
    Bucket SEC_7p5_BUCKET = Bucket.of(7.5, SECONDS);
    Bucket SEC_10_BUCKET = Bucket.of(10, SECONDS);
    Bucket SEC_20_BUCKET = Bucket.of(20, SECONDS);
    Bucket SEC_30_BUCKET = Bucket.of(30, SECONDS);
    Bucket INF_BUCKET = Bucket.of(Double.POSITIVE_INFINITY);

    default void update(long value) {
        update(value, NO_DIMENSION_VALUES);
    }

    void update(long value, MetricDimensionValues dimensionValues);
}