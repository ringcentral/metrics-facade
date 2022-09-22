package com.ringcentral.platform.metrics.measurables;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.*;
import com.ringcentral.platform.metrics.histogram.*;
import com.ringcentral.platform.metrics.rate.*;
import com.ringcentral.platform.metrics.timer.*;
import com.ringcentral.platform.metrics.var.*;

import static java.util.Locale.ENGLISH;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class DefaultMeasurableNameProvider implements MeasurableNameProvider {
    private static final String SEPARATOR = ".";

    public static final String PERCENTILE_75 = "75_percentile";
    public static final String PERCENTILE_95 = "95_percentile";
    public static final String PERCENTILE_98 = "98_percentile";
    public static final String PERCENTILE_99 = "99_percentile";
    public static final String PERCENTILE_999 = "999_percentile";
    public static final String DURATION = "duration";
    public static final String MEAN = "mean";
    public static final String MAX = "max";
    public static final String MIN = "min";
    public static final String MEDIAN = "median";
    public static final String STD_DEV = "stdDev";
    public static final String COUNT = "count";
    public static final String RATE = "rate";
    public static final String UNIT = "unit";
    public static final String TOTAL_SUM = "totalSum";
    public static final String DURATION_MEAN = DURATION + SEPARATOR + MEAN;
    public static final String DURATION_MAX = DURATION + SEPARATOR + MAX;
    public static final String DURATION_MIN = DURATION + SEPARATOR + MIN;
    public static final String DURATION_MEDIAN = DURATION + SEPARATOR + MEDIAN;
    public static final String DURATION_STD_DEV = DURATION + SEPARATOR + STD_DEV;
    public static final String DURATION_75_PERCENTILE = DURATION + SEPARATOR + PERCENTILE_75;
    public static final String DURATION_95_PERCENTILE = DURATION + SEPARATOR + PERCENTILE_95;
    public static final String DURATION_98_PERCENTILE = DURATION + SEPARATOR + PERCENTILE_98;
    public static final String DURATION_99_PERCENTILE = DURATION + SEPARATOR + PERCENTILE_99;
    public static final String DURATION_999_PERCENTILE = DURATION + SEPARATOR + PERCENTILE_999;
    public static final String RATE_MEAN = RATE + SEPARATOR + MEAN;
    public static final String RATE_1_MINUTE = RATE + SEPARATOR + "1_minute";
    public static final String RATE_5_MINUTES = RATE + SEPARATOR + "5_minutes";
    public static final String RATE_15_MINUTES = RATE + SEPARATOR + "15_minutes";
    public static final String RATE_UNIT = RATE + SEPARATOR + UNIT;

    public static final DefaultMeasurableNameProvider INSTANCE = new DefaultMeasurableNameProvider();

    @Override
    public String nameFor(MetricInstance instance, Measurable measurable) {
         if (instance instanceof TimerInstance) {
             return nameForTimerInstance(measurable);
         } else if (instance instanceof CounterInstance) {
             return nameForCounterInstance(measurable);
         } else if (instance instanceof HistogramInstance) {
             return nameForHistogramInstance(measurable);
         } else if (instance instanceof RateInstance) {
             return nameForRateInstance(measurable);
         } else if (instance instanceof VarInstance) {
             return nameForVarInstance(measurable);
         } else {
            return defaultFor(instance) + SEPARATOR + defaultFor(measurable);
        }
    }

    protected String nameForTimerInstance(Measurable measurable) {
        if (measurable instanceof Counter.Count) {
            return COUNT;
        } else if (measurable instanceof Rate.MeanRate) {
            return RATE_MEAN;
        } else if (measurable instanceof Rate.OneMinuteRate) {
            return RATE_1_MINUTE;
        } else if (measurable instanceof Rate.FiveMinutesRate) {
            return RATE_5_MINUTES;
        } else if (measurable instanceof Rate.FifteenMinutesRate) {
            return RATE_15_MINUTES;
        } else if (measurable instanceof Rate.RateUnit) {
            return RATE_UNIT;
        } else if (measurable instanceof Histogram.TotalSum) {
            return DURATION + SEPARATOR + TOTAL_SUM;
        } else if (measurable instanceof Histogram.Min) {
            return DURATION_MIN;
        } else if (measurable instanceof Histogram.Max) {
            return DURATION_MAX;
        } else if (measurable instanceof Histogram.Mean) {
            return DURATION_MEAN;
        } else if (measurable instanceof Histogram.StandardDeviation) {
            return DURATION_STD_DEV;
        } else if (measurable instanceof Histogram.Percentile) {
            Histogram.Percentile p = (Histogram.Percentile)measurable;
            return DURATION + SEPARATOR + p.quantileDecimalPartAsString() + "_percentile";
        } else if (measurable instanceof Histogram.Bucket) {
            Histogram.Bucket b = (Histogram.Bucket)measurable;

            String upperBoundAsStringWithUnit =
                b.upperBoundUnit() != null ?
                b.upperBoundAsStringWithUnit() :
                b.upperBoundAsStringWithUnit(MILLISECONDS);

            return DURATION + SEPARATOR + upperBoundAsStringWithUnit + "_bucket";
        } else if (measurable instanceof Timer.DurationUnit) {
            return DURATION + SEPARATOR + UNIT;
        } else {
            return defaultFor(measurable);
        }
    }

    protected String nameForCounterInstance(Measurable measurable) {
        if (measurable instanceof Counter.Count) {
            return COUNT;
        } else {
            return defaultFor(measurable);
        }
    }

    protected String nameForHistogramInstance(Measurable measurable) {
        if (measurable instanceof Counter.Count) {
            return COUNT;
        } else if (measurable instanceof Histogram.TotalSum) {
            return TOTAL_SUM;
        } else if (measurable instanceof Histogram.Min) {
            return MIN;
        } else if (measurable instanceof Histogram.Max) {
            return MAX;
        } else if (measurable instanceof Histogram.Mean) {
            return MEAN;
        } else if (measurable instanceof Histogram.StandardDeviation) {
            return STD_DEV;
        } else if (measurable instanceof Histogram.Percentile) {
            Histogram.Percentile p = (Histogram.Percentile)measurable;
            return p.quantileDecimalPartAsString() + "_percentile";
        } else if (measurable instanceof Histogram.Bucket) {
            Histogram.Bucket b = (Histogram.Bucket)measurable;
            return b.upperBoundAsString() + "_bucket";
        } else {
            return defaultFor(measurable);
        }
    }

    protected String nameForRateInstance(Measurable measurable) {
        if (measurable instanceof Counter.Count) {
            return COUNT;
        } else if (measurable instanceof Rate.MeanRate) {
            return RATE_MEAN;
        } else if (measurable instanceof Rate.OneMinuteRate) {
            return RATE_1_MINUTE;
        } else if (measurable instanceof Rate.FiveMinutesRate) {
            return RATE_5_MINUTES;
        } else if (measurable instanceof Rate.FifteenMinutesRate) {
            return RATE_15_MINUTES;
        } else if (measurable instanceof Rate.RateUnit) {
            return RATE_UNIT;
        } else {
            return defaultFor(measurable);
        }
    }

    protected String nameForVarInstance(Measurable measurable) {
        if (measurable instanceof Var.Value) {
            return "value";
        } else {
            return defaultFor(measurable);
        }
    }

    protected static String defaultFor(Object o) {
        return o.getClass().getSimpleName().toLowerCase(ENGLISH);
    }
}