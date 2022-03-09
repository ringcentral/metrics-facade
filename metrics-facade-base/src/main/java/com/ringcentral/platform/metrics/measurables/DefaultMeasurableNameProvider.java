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
            return defaultFor(instance) + "." + defaultFor(measurable);
        }
    }

    protected String nameForTimerInstance(Measurable measurable) {
        if (measurable instanceof Counter.Count) {
            return "count";
        } else if (measurable instanceof Rate.MeanRate) {
            return "rate.mean";
        } else if (measurable instanceof Rate.OneMinuteRate) {
            return "rate.1_minute";
        } else if (measurable instanceof Rate.FiveMinutesRate) {
            return "rate.5_minutes";
        } else if (measurable instanceof Rate.FifteenMinutesRate) {
            return "rate.15_minutes";
        } else if (measurable instanceof Rate.RateUnit) {
            return "rate.unit";
        } else if (measurable instanceof Histogram.TotalSum) {
            return "duration.totalSum";
        } else if (measurable instanceof Histogram.Min) {
            return "duration.min";
        } else if (measurable instanceof Histogram.Max) {
            return "duration.max";
        } else if (measurable instanceof Histogram.Mean) {
            return "duration.mean";
        } else if (measurable instanceof Histogram.StandardDeviation) {
            return "duration.stdDev";
        } else if (measurable instanceof Histogram.Percentile) {
            Histogram.Percentile p = (Histogram.Percentile)measurable;
            return "duration." + p.quantileDecimalPartAsString() + "_percentile";
        } else if (measurable instanceof Histogram.Bucket) {
            Histogram.Bucket b = (Histogram.Bucket)measurable;

            String upperBoundAsStringWithUnit =
                b.upperBoundUnit() != null ?
                b.upperBoundAsStringWithUnit() :
                b.upperBoundAsStringWithUnit(MILLISECONDS);

            return "duration." + upperBoundAsStringWithUnit + "_bucket";
        } else if (measurable instanceof Timer.DurationUnit) {
            return "duration.unit";
        } else {
            return defaultFor(measurable);
        }
    }

    protected String nameForCounterInstance(Measurable measurable) {
        if (measurable instanceof Counter.Count) {
            return "count";
        } else {
            return defaultFor(measurable);
        }
    }

    protected String nameForHistogramInstance(Measurable measurable) {
        if (measurable instanceof Counter.Count) {
            return "count";
        } else if (measurable instanceof Histogram.TotalSum) {
            return "totalSum";
        } else if (measurable instanceof Histogram.Min) {
            return "min";
        } else if (measurable instanceof Histogram.Max) {
            return "max";
        } else if (measurable instanceof Histogram.Mean) {
            return "mean";
        } else if (measurable instanceof Histogram.StandardDeviation) {
            return "stdDev";
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
            return "count";
        } else if (measurable instanceof Rate.MeanRate) {
            return "rate.mean";
        } else if (measurable instanceof Rate.OneMinuteRate) {
            return "rate.1_minute";
        } else if (measurable instanceof Rate.FiveMinutesRate) {
            return "rate.5_minutes";
        } else if (measurable instanceof Rate.FifteenMinutesRate) {
            return "rate.15_minutes";
        } else if (measurable instanceof Rate.RateUnit) {
            return "rate.unit";
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