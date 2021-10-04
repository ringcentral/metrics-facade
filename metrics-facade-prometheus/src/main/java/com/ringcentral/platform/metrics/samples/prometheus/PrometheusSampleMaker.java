package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.histogram.*;
import com.ringcentral.platform.metrics.histogram.Histogram.Percentile;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.samples.SampleMaker;
import com.ringcentral.platform.metrics.timer.TimerInstance;
import io.prometheus.client.Collector;

import java.util.List;

import static io.prometheus.client.Collector.Type.GAUGE;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toList;

public class PrometheusSampleMaker implements SampleMaker<
    PrometheusSample,
    PrometheusSampleSpec,
    PrometheusInstanceSampleSpec> {

    public static final MetricName DEFAULT_MIN_CHILD_NAME_SUFFIX = MetricName.of("min");
    public static final MetricName DEFAULT_MAX_CHILD_NAME_SUFFIX = MetricName.of("max");
    public static final MetricName DEFAULT_MEAN_CHILD_NAME_SUFFIX = MetricName.of("mean");

    @Override
    public PrometheusSample makeSample(PrometheusSampleSpec spec, PrometheusInstanceSampleSpec instanceSampleSpec) {
        if (!spec.isEnabled() || !spec.hasMeasurable() || !spec.hasValue()) {
            return null;
        }

        MetricInstance instance = instanceSampleSpec.instance();
        MetricName childInstanceSampleNameSuffix = null;
        Collector.Type childInstanceSampleType = null;

        // TODO: support exporting Histogram.Min/Max/Mean as a regular sample with a special name suffix.
        if (spec.measurable() instanceof Histogram.Min) {
            // TODO: support customizing the suffix.
            childInstanceSampleNameSuffix = DEFAULT_MIN_CHILD_NAME_SUFFIX;
            childInstanceSampleType = GAUGE;
        } else if (spec.measurable() instanceof Histogram.Max) {
            // TODO: support customizing the suffix.
            childInstanceSampleNameSuffix = DEFAULT_MAX_CHILD_NAME_SUFFIX;
            childInstanceSampleType = GAUGE;
        } else if (spec.measurable() instanceof Histogram.Mean) {
            // TODO: support customizing the suffix.
            childInstanceSampleNameSuffix = DEFAULT_MEAN_CHILD_NAME_SUFFIX;
            childInstanceSampleType = GAUGE;
        }

        String nameSuffix = null;

        if ((instance instanceof TimerInstance || instance instanceof HistogramInstance)
            && spec.measurable() instanceof Count) {

            nameSuffix = "_count";
        }

        List<String> labelNames;
        List<String> labelValues;

        if (instanceSampleSpec.hasDimensionValues()) {
            labelNames = instanceSampleSpec.dimensionValues().stream().map(dv -> dv.dimension().name()).collect(toList());
            labelValues = instanceSampleSpec.dimensionValues().stream().map(MetricDimensionValue::value).collect(toList());

            if (spec.measurable() instanceof Percentile) {
                labelNames.add("quantile");
                Percentile p = (Percentile)spec.measurable();
                labelValues.add(p.quantileAsString());
            }
        } else if (spec.measurable() instanceof Percentile) {
            labelNames = singletonList("quantile");
            Percentile p = (Percentile)spec.measurable();
            labelValues = singletonList(p.quantileAsString());
        } else {
            labelNames = emptyList();
            labelValues = emptyList();
        }

        return new PrometheusSample(
            childInstanceSampleNameSuffix,
            childInstanceSampleType,
            nameSuffix,
            labelNames,
            labelValues,
            spec.value());
    }
}