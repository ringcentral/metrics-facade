package com.ringcentral.platform.metrics.histogram;

import com.ringcentral.platform.metrics.Meter;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.measurables.MeasurableType;
import com.ringcentral.platform.metrics.scale.Scale;
import com.ringcentral.platform.metrics.scale.ScaleBuilder;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.labels.LabelValues.NO_LABEL_VALUES;
import static com.ringcentral.platform.metrics.measurables.MeasurableType.*;
import static com.ringcentral.platform.metrics.scale.ExpScaleBuilder.expScale;
import static com.ringcentral.platform.metrics.scale.LinearScaleBuilder.linearScale;
import static com.ringcentral.platform.metrics.utils.ObjectUtils.hashCodeFor;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static com.ringcentral.platform.metrics.utils.TimeUnitUtils.convertTimeUnit;
import static java.lang.Math.*;
import static java.util.concurrent.TimeUnit.*;
import static java.util.stream.Collectors.toSet;

public interface Histogram extends Meter {
    class TotalSum implements HistogramMeasurable {

        static final int HASH_CODE = "Histogram.TotalSum".hashCode();

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

    TotalSum TOTAL_SUM = new TotalSum();

    class Min implements HistogramMeasurable {

        static final int HASH_CODE = "Histogram.Min".hashCode();

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

    Min MIN = new Min();

    class Max implements HistogramMeasurable {

        static final int HASH_CODE = "Histogram.Max".hashCode();

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

    Max MAX = new Max();

    class Mean implements HistogramMeasurable {

        static final int HASH_CODE = "Histogram.Mean".hashCode();

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

    Mean MEAN = new Mean();

    class StandardDeviation implements HistogramMeasurable {

        static final int HASH_CODE = "Histogram.StandardDeviation".hashCode();

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

    StandardDeviation STANDARD_DEVIATION = new StandardDeviation();

    class Percentile implements HistogramMeasurable, Comparable<Percentile> {

        private static final BigDecimal BIG_DECIMAL_100 = BigDecimal.valueOf(100.0);

        private final double quantile;
        private final String quantileAsString;
        private final String quantileDecimalPartAsString;
        private final double percentile;
        private final int hashCode;

        public Percentile(double quantile) {
            checkArgument(
                !(quantile < 0.0 || quantile > 1.0 || Double.isNaN(quantile)),
                quantile + " is not in [0.0 .. 1.0]");

            this.quantile = quantile;
            this.quantileAsString = Double.toString(quantile);
            String afterPoint = this.quantileAsString.substring(this.quantileAsString.indexOf(".") + 1);
            this.quantileDecimalPartAsString = afterPoint.length() > 1 ? afterPoint : afterPoint + "0";
            this.percentile = min(max(BigDecimal.valueOf(quantile).multiply(BIG_DECIMAL_100).doubleValue(), 0.0), 100.0);
            this.hashCode = hashCodeFor("Histogram.Percentile", quantile);
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

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            Percentile that = (Percentile)other;

            if (hashCode != that.hashCode) {
                return false;
            }

            return quantile == that.quantile;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public int compareTo(Percentile right) {
            return Double.compare(quantile, right.quantile);
        }
    }

    Percentile PERCENTILE_1 = Percentile.of(0.01);
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

    class Bucket implements HistogramMeasurable, Comparable<Bucket> {

        private final double upperBoundInUnits;
        private final TimeUnit upperBoundUnit;
        private final double upperBound;
        private final long upperBoundAsLong;
        private final boolean inf;
        private final boolean negativeInf;
        private final String upperBoundAsString;
        private final String upperBoundAsStringWithUnit;
        private final Map<TimeUnit, String> unitToUpperBoundAsStringWithUnit = new EnumMap<>(TimeUnit.class);
        private final String upperBoundAsNumberString;
        private final String upperBoundSecAsNumberString;
        private final int hashCode;

        public Bucket(double upperBound) {
            this(upperBound, null);
        }

        public Bucket(double upperBoundInUnits, TimeUnit upperBoundUnit) {
            this.upperBoundInUnits = upperBoundInUnits;
            this.upperBoundUnit = upperBoundUnit;

            this.upperBound =
                Double.isInfinite(upperBoundInUnits) || upperBoundUnit == null || upperBoundUnit == NANOSECONDS ?
                upperBoundInUnits :
                upperBoundUnit.toNanos(1L) * upperBoundInUnits;

            if (Double.isNaN(this.upperBound)) {
                throw new IllegalArgumentException(
                    upperBoundInUnits
                    + (upperBoundUnit != null ? " " + upperBoundUnit.name().toLowerCase(Locale.ENGLISH) : "")
                    + " in nanos results in an overflow");
            }

            this.upperBoundAsLong = round(this.upperBound);
            this.inf = (this.upperBoundAsLong == Long.MAX_VALUE);
            this.negativeInf = (this.upperBoundAsLong == Long.MIN_VALUE);
            this.upperBoundAsString = upperBoundAsString(this.upperBound);
            TimeUnit resolvedUpperBoundUnit = upperBoundUnit != null ? upperBoundUnit : NANOSECONDS;

            if (Double.isInfinite(upperBoundInUnits)) {
                this.upperBoundAsStringWithUnit = upperBoundAsString(upperBoundInUnits);

                for (TimeUnit unit : TimeUnit.values()) {
                    unitToUpperBoundAsStringWithUnit.put(unit, this.upperBoundAsStringWithUnit);
                }
            } else {
                for (TimeUnit unit : TimeUnit.values()) {
                    String unitAsString;

                    if (unit == NANOSECONDS) {
                        unitAsString = "ns";
                    } else if (unit == MICROSECONDS) {
                        unitAsString = "us";
                    } else if (unit == MILLISECONDS) {
                        unitAsString = "ms";
                    } else if (unit == SECONDS) {
                        unitAsString = "sec";
                    } else if (unit == HOURS) {
                        unitAsString = "h";
                    } else if (unit == DAYS) {
                        unitAsString = "d";
                    } else  {
                        unitAsString = unit.name().toLowerCase(Locale.ENGLISH);
                    }

                    double convertedUpperBoundInUnits;

                    if (unit == resolvedUpperBoundUnit) {
                        convertedUpperBoundInUnits = upperBoundInUnits;
                    } else if (unit == MILLISECONDS) {
                        convertedUpperBoundInUnits = BigDecimal.valueOf(upperBoundInUnits).multiply(
                            BigDecimal.valueOf(resolvedUpperBoundUnit.toNanos(1L)).multiply(BigDecimal.valueOf(0.000001))).doubleValue();
                    } else if (unit == SECONDS) {
                        convertedUpperBoundInUnits = BigDecimal.valueOf(upperBoundInUnits).multiply(
                            BigDecimal.valueOf(resolvedUpperBoundUnit.toNanos(1L)).multiply(BigDecimal.valueOf(0.000000001))).doubleValue();
                    } else {
                        convertedUpperBoundInUnits = convertTimeUnit(upperBoundInUnits, resolvedUpperBoundUnit, unit);
                    }

                    unitToUpperBoundAsStringWithUnit.put(
                        unit,
                        upperBoundAsString(convertedUpperBoundInUnits) + unitAsString);
                }

                this.upperBoundAsStringWithUnit = unitToUpperBoundAsStringWithUnit.get(resolvedUpperBoundUnit);
            }

            this.upperBoundAsNumberString = upperBoundAsNumberString(this.upperBound);

            this.upperBoundSecAsNumberString = withoutTrailingZeros(String.valueOf(
                Double.isInfinite(upperBoundInUnits) || upperBoundUnit == SECONDS ?
                upperBoundInUnits :
                // instead of upperBoundInUnits * ((1.0 * (upperBoundUnit != null ? upperBoundUnit : NANOSECONDS).toNanos(1L)) / NANOS_PER_SEC)
                // for example, 1.7000000000000002 -> 1.7
                BigDecimal.valueOf(upperBoundInUnits).multiply(
                    BigDecimal.valueOf(resolvedUpperBoundUnit.toNanos(1L)).multiply(BigDecimal.valueOf(0.000000001))).doubleValue()));

            this.hashCode = hashCodeFor("Histogram.Bucket", upperBoundInUnits, resolvedUpperBoundUnit);
        }

        static String upperBoundAsString(double b) {
            if (b == Double.POSITIVE_INFINITY) {
                return "inf";
            }

            if (b == Double.NEGATIVE_INFINITY) {
                return "negativeInf";
            }

            return upperBoundAsNumberString(b).replace('.', 'p');
        }

        static String upperBoundAsNumberString(double b) {
            return withoutTrailingZeros(Double.toString(b));
        }

        static String withoutTrailingZeros(String s) {
            return s.replaceAll("\\.0+$", "");
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

        public boolean isInf() {
            return inf;
        }

        public boolean isNegativeInf() {
            return negativeInf;
        }

        public String upperBoundAsString() {
            return upperBoundAsString;
        }

        public String upperBoundAsStringWithUnit() {
            return upperBoundAsStringWithUnit;
        }

        public String upperBoundAsStringWithUnit(TimeUnit unit) {
            return unitToUpperBoundAsStringWithUnit.get(unit);
        }

        /**
         * @return the string representation of the upper bound that can converted to {@code Double} via {@code Double.valueOf()}.
         *         For example, Bucket.of(25.5).upperBoundAsNumberString().equals("25.5").
         *
         */
        public String upperBoundAsNumberString() {
            return upperBoundAsNumberString;
        }

        public String upperBoundSecAsNumberString() {
            return upperBoundSecAsNumberString;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            Bucket that = (Bucket)other;

            if (hashCode != that.hashCode) {
                return false;
            }

            return upperBoundInUnits == that.upperBoundInUnits
                && upperBoundUnit == that.upperBoundUnit;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public int compareTo(Bucket right) {
            if (isInf()) {
                return right.isInf() ? 0 : 1;
            }

            if (right.isInf()) {
                return -1;
            }

            if (isNegativeInf()) {
                return right.isNegativeInf() ? 0 : -1;
            }

            if (right.isNegativeInf()) {
                return 1;
            }

            return Double.compare(upperBound, right.upperBound);
        }

        @Override
        public String toString() {
            return "Bucket{" +
                "upperBoundAsString='" + upperBoundAsString + '\'' +
                '}';
        }
    }

    Bucket MS_1_BUCKET = Bucket.of(1, MILLISECONDS);
    Bucket MS_5_BUCKET = Bucket.of(5, MILLISECONDS);
    Bucket MS_10_BUCKET = Bucket.of(10, MILLISECONDS);
    Bucket MS_15_BUCKET = Bucket.of(15, MILLISECONDS);
    Bucket MS_20_BUCKET = Bucket.of(20, MILLISECONDS);
    Bucket MS_25_BUCKET = Bucket.of(25, MILLISECONDS);
    Bucket MS_30_BUCKET = Bucket.of(30, MILLISECONDS);
    Bucket MS_35_BUCKET = Bucket.of(35, MILLISECONDS);
    Bucket MS_40_BUCKET = Bucket.of(40, MILLISECONDS);
    Bucket MS_45_BUCKET = Bucket.of(45, MILLISECONDS);
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
    Bucket NEGATIVE_INF_BUCKET = Bucket.of(Double.NEGATIVE_INFINITY);

    class Buckets implements HistogramMeasurable {

        private final Set<Bucket> buckets;
        private final int hashCode;

        public Buckets(Set<Bucket> buckets) {
            checkArgument(
                buckets != null && !buckets.isEmpty(),
                "buckets is null or empty");

            this.buckets = buckets;
            this.hashCode = hashCodeFor("Histogram.Buckets", buckets);
        }

        public static Buckets linear(long from, long step, long stepCount) {
            return linear(from, step, stepCount, null);
        }

        public static Buckets linear(long from, long step, long stepCount, TimeUnit unit) {
            return of(linearScale().from(from).steps(step, stepCount).withInf(), unit);
        }

        public static Buckets exp(long from, double factor, long stepCount) {
            return exp(from, factor, stepCount, null);
        }

        public static Buckets exp(long from, double factor, long stepCount, TimeUnit unit) {
            return of(expScale().from(from).factor(factor).steps(stepCount).withInf(), unit);
        }

        public static Buckets of(ScaleBuilder<?> scaleBuilder) {
            return of(scaleBuilder, null);
        }

        public static Buckets of(ScaleBuilder<?> scaleBuilder, TimeUnit unit) {
            return of(scaleBuilder.build(), unit);
        }

        public static Buckets of(Scale scale) {
            return of(scale, null);
        }

        public static Buckets of(Scale scale, TimeUnit unit) {
            return new Buckets(scale.points().stream().map(p -> Bucket.of(p.doubleValue(), unit)).collect(toSet()));
        }

        @Override
        public MeasurableType type() {
            return OBJECT;
        }

        public Set<Bucket> buckets() {
            return buckets;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            Buckets that = (Buckets)other;

            if (hashCode != that.hashCode) {
                return false;
            }

            return buckets.equals(that.buckets);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    default void update(long value) {
        update(value, NO_LABEL_VALUES);
    }

    void update(long value, LabelValues labelValues);
}