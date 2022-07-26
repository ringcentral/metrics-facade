package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.histogram.*;
import com.ringcentral.platform.metrics.histogram.Histogram.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
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

    // TODO: support customizing the suffixes.
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
        Measurable m = spec.measurable();

        if (m instanceof Histogram.Min) {
            childInstanceSampleNameSuffix = DEFAULT_MIN_CHILD_NAME_SUFFIX;
            childInstanceSampleType = GAUGE;
        } else if (m instanceof Histogram.Max) {
            childInstanceSampleNameSuffix = DEFAULT_MAX_CHILD_NAME_SUFFIX;
            childInstanceSampleType = GAUGE;
        } else if (m instanceof Histogram.Mean) {
            childInstanceSampleNameSuffix = DEFAULT_MEAN_CHILD_NAME_SUFFIX;
            childInstanceSampleType = GAUGE;
        }

        String nameSuffix = null;

        if (instance instanceof TimerInstance || instance instanceof HistogramInstance) {
            if (m instanceof Counter.Count) {
                nameSuffix = "_count";
            } else if (m instanceof Histogram.TotalSum) {
                nameSuffix = "_sum";
            } else if (m instanceof Histogram.Bucket) {
                nameSuffix = "_bucket";
            }
        }

        List<String> labelNames;
        List<String> labelValues;

        if (instanceSampleSpec.hasDimensionValues()) {
            labelNames = instanceSampleSpec.dimensionValues().stream().map(dv -> dv.dimension().name()).collect(toList());
            labelValues = instanceSampleSpec.dimensionValues().stream().map(MetricDimensionValue::value).collect(toList());

            if (m instanceof Histogram.Percentile) {
                labelNames.add("quantile");
                Percentile p = (Histogram.Percentile)m;
                labelValues.add(p.quantileAsString());
            } else if (m instanceof Histogram.Bucket) {
                labelNames.add("le");
                labelValues.add(leValue(instance, (Histogram.Bucket)m));
            }
        } else if (m instanceof Histogram.Percentile) {
            labelNames = singletonList("quantile");
            Percentile p = (Percentile)m;
            labelValues = singletonList(p.quantileAsString());
        } else if (m instanceof Histogram.Bucket) {
            labelNames = singletonList("le");
            labelValues = singletonList(leValue(instance, (Histogram.Bucket)m));
        } else {
            labelNames = emptyList();
            labelValues = emptyList();
        }

        return new PrometheusSample(
            childInstanceSampleNameSuffix,
            childInstanceSampleType,
            null,
            nameSuffix,
            labelNames,
            labelValues,
            spec.value());
    }

    private String leValue(MetricInstance instance, Bucket bucket) {
        if (bucket.isInf()) {
            return "+Inf";
        } else if (bucket.isNegativeInf()) {
            return "-Inf";
        } else {
            return
                instance instanceof TimerInstance ?
                bucket.upperBoundSecAsString() :
                bucket.upperBoundAsString();
        }
    }
}