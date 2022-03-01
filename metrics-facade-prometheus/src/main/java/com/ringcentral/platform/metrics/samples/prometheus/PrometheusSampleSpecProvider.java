package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.*;
import com.ringcentral.platform.metrics.histogram.*;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.rate.RateInstance;
import com.ringcentral.platform.metrics.samples.SampleSpecProvider;
import com.ringcentral.platform.metrics.timer.TimerInstance;
import com.ringcentral.platform.metrics.var.*;

import java.util.concurrent.TimeUnit;

import static com.ringcentral.platform.metrics.utils.TimeUnitUtils.NANOS_PER_SEC;
import static java.util.concurrent.TimeUnit.*;

public class PrometheusSampleSpecProvider implements SampleSpecProvider<
    PrometheusSampleSpec,
    PrometheusInstanceSampleSpec> {

    // ms to sec
    public static final double MS_DURATION_FACTOR = (1.0 * MILLISECONDS.toNanos(1L)) / NANOS_PER_SEC;

    @Override
    public PrometheusSampleSpec sampleSpecFor(
        PrometheusInstanceSampleSpec instanceSampleSpec,
        MetricInstance instance,
        MeasurableValues measurableValues,
        Measurable measurable) {

        if (instance instanceof TimerInstance || instance instanceof HistogramInstance) {
            if (!(measurable instanceof Counter.Count
                || measurable instanceof Histogram.TotalSum
                || measurable instanceof Histogram.Min
                || measurable instanceof Histogram.Max
                || measurable instanceof Histogram.Mean
                || measurable instanceof Histogram.Percentile
                || measurable instanceof Histogram.Bucket)) {

                return null;
            }
        } else if (instance instanceof CounterInstance || instance instanceof RateInstance) {
            if (!(measurable instanceof Counter.Count)) {
                return null;
            }
        } else if (instance instanceof VarInstance) {
            if (!(measurable instanceof Var.Value)) {
                return null;
            }
        } else {
            return null;
        }

        Object measurableValue = measurableValues.valueOf(measurable);

        if (!(measurableValue instanceof Number)) {
            return null;
        }

        double value = ((Number)measurableValue).doubleValue();

        if (instance instanceof TimerInstance) {
            if (measurable instanceof Histogram.TotalSum
                || measurable instanceof Histogram.Min
                || measurable instanceof Histogram.Max
                || measurable instanceof Histogram.Mean
                || measurable instanceof Histogram.Percentile) {

                TimerInstance timerInstance = (TimerInstance)instance;
                TimeUnit unit = timerInstance.durationUnit();

                if (unit == MILLISECONDS) {
                    value *= MS_DURATION_FACTOR;
                } else if (unit != SECONDS) {
                    value *= (1.0 * unit.toNanos(1L)) / NANOS_PER_SEC;
                }
            }
        }

        return new PrometheusSampleSpec(
            Boolean.TRUE,
            measurable,
            value);
    }
}
