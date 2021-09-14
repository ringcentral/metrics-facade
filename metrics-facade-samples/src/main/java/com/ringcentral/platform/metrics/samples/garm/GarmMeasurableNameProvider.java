package com.ringcentral.platform.metrics.samples.garm;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.*;
import com.ringcentral.platform.metrics.histogram.*;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.rate.*;
import com.ringcentral.platform.metrics.timer.*;
import com.ringcentral.platform.metrics.var.*;

import static java.util.Locale.*;

public class GarmMeasurableNameProvider implements MeasurableNameProvider {

    public static final GarmMeasurableNameProvider INSTANCE = new GarmMeasurableNameProvider();

    @Override
    public String nameFor(MetricInstance instance, Measurable measurable) {
        if (instance instanceof TimerInstance) {
            if (measurable instanceof Counter.Count) {
                return "Count";
            } else if (measurable instanceof Rate.MeanRate) {
                return "MeanRate";
            } else if (measurable instanceof Rate.OneMinuteRate) {
                return "OneMinuteRate";
            } else if (measurable instanceof Rate.FiveMinutesRate) {
                return "FiveMinuteRate";
            } else if (measurable instanceof Rate.FifteenMinutesRate) {
                return "FifteenMinuteRate";
            } else if (measurable instanceof Rate.RateUnit) {
                return "RateUnit";
            } else if (measurable instanceof Histogram.Min) {
                return "Min";
            } else if (measurable instanceof Histogram.Max) {
                return "Max";
            } else if (measurable instanceof Histogram.Mean) {
                return "Mean";
            } else if (measurable instanceof Histogram.StandardDeviation) {
                return "StdDev";
            } else if (measurable instanceof Histogram.Percentile) {
                Histogram.Percentile p = (Histogram.Percentile)measurable;
                return p.quantileDecimalPartAsString() + "thPercentile";
            } else if (measurable instanceof Timer.DurationUnit) {
                return "DurationUnit";
            } else {
                return defaultFor(measurable);
            }
        } else if (instance instanceof CounterInstance) {
            if (measurable instanceof Counter.Count) {
                return "Count";
            } else {
                return defaultFor(measurable);
            }
        } else if (instance instanceof HistogramInstance) {
            if (measurable instanceof Counter.Count) {
                return "Count";
            } else if (measurable instanceof Histogram.Min) {
                return "Min";
            } else if (measurable instanceof Histogram.Max) {
                return "Max";
            } else if (measurable instanceof Histogram.Mean) {
                return "Mean";
            } else if (measurable instanceof Histogram.StandardDeviation) {
                return "StdDev";
            } else if (measurable instanceof Histogram.Percentile) {
                Histogram.Percentile p = (Histogram.Percentile)measurable;
                return p.quantileDecimalPartAsString() + "thPercentile";
            } else {
                return defaultFor(measurable);
            }
        } else if (instance instanceof RateInstance) {
            if (measurable instanceof Counter.Count) {
                return "Count";
            } else if (measurable instanceof Rate.MeanRate) {
                return "MeanRate";
            } else if (measurable instanceof Rate.OneMinuteRate) {
                return "OneMinuteRate";
            } else if (measurable instanceof Rate.FiveMinutesRate) {
                return "FiveMinuteRate";
            } else if (measurable instanceof Rate.FifteenMinutesRate) {
                return "FifteenMinuteRate";
            } else if (measurable instanceof Rate.RateUnit) {
                return "RateUnit";
            } else {
                return defaultFor(measurable);
            }
        } else if (instance instanceof VarInstance) {
            if (measurable instanceof Var.Value) {
                return "Value";
            } else {
                return defaultFor(measurable);
            }
        } else {
            return defaultFor(instance) + "." + defaultFor(measurable);
        }
    }

    static String defaultFor(Object o) {
        return o.getClass().getSimpleName().toLowerCase(ENGLISH);
    }
}
