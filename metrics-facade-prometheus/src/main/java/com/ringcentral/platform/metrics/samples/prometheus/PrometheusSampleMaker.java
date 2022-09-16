package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.histogram.Histogram.*;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.samples.SampleMaker;
import com.ringcentral.platform.metrics.timer.TimerInstance;
import io.prometheus.client.Collector;

import java.util.List;

import static io.prometheus.client.Collector.Type.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class PrometheusSampleMaker implements SampleMaker<
    PrometheusSample,
    PrometheusSampleSpec,
    PrometheusInstanceSampleSpec,
    PrometheusInstanceSample> {

    public static boolean DEFAULT_SEPARATE_HISTOGRAM_AND_SUMMARY = true;

    public static final MetricName DEFAULT_HISTOGRAM_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX = MetricName.of("histogram");
    public static final MetricName DEFAULT_SUMMARY_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX = MetricName.of("summary");
    public static final MetricName DEFAULT_MIN_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX = MetricName.of("min");
    public static final MetricName DEFAULT_MAX_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX = MetricName.of("max");
    public static final MetricName DEFAULT_MEAN_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX = MetricName.of("mean");

    private final boolean separateHistogramAndSummary;

    private final MetricName histogramChildInstanceSampleNameSuffix;
    private final MetricName summaryChildInstanceSampleNameSuffix;
    private final MetricName minChildInstanceSampleNameSuffix;
    private final MetricName maxChildInstanceSampleNameSuffix;
    private final MetricName meanChildInstanceSampleNameSuffix;

    public PrometheusSampleMaker() {
        this(
            DEFAULT_SEPARATE_HISTOGRAM_AND_SUMMARY,
            DEFAULT_HISTOGRAM_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX,
            DEFAULT_SUMMARY_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX,
            DEFAULT_MIN_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX,
            DEFAULT_MAX_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX,
            DEFAULT_MEAN_CHILD_INSTANCE_SAMPLE_NAME_SUFFIX);
    }

    public PrometheusSampleMaker(
        boolean separateHistogramAndSummary,
        MetricName histogramChildInstanceSampleNameSuffix,
        MetricName summaryChildInstanceSampleNameSuffix,
        MetricName minChildInstanceSampleNameSuffix,
        MetricName maxChildInstanceSampleNameSuffix,
        MetricName meanChildInstanceSampleNameSuffix) {

        this.separateHistogramAndSummary = separateHistogramAndSummary;

        this.histogramChildInstanceSampleNameSuffix = histogramChildInstanceSampleNameSuffix;
        this.summaryChildInstanceSampleNameSuffix = summaryChildInstanceSampleNameSuffix;
        this.minChildInstanceSampleNameSuffix = minChildInstanceSampleNameSuffix;
        this.maxChildInstanceSampleNameSuffix = maxChildInstanceSampleNameSuffix;
        this.meanChildInstanceSampleNameSuffix = meanChildInstanceSampleNameSuffix;
    }

    @Override
    public PrometheusSample makeSample(
        PrometheusSampleSpec spec,
        PrometheusInstanceSampleSpec instanceSampleSpec,
        PrometheusInstanceSample instanceSample) {

        if (!spec.isEnabled() || !spec.hasMeasurable() || !spec.hasValue()) {
            return null;
        }

        MetricInstance instance = instanceSampleSpec.instance();
        MetricName childInstanceSampleNameSuffix = null;
        Collector.Type childInstanceSampleType = null;

        // TODO: support exporting Histogram.Min/Max/Mean as a regular sample with a special name suffix.
        Measurable m = spec.measurable();

        if (m instanceof Min) {
            childInstanceSampleNameSuffix = minChildInstanceSampleNameSuffix;
            childInstanceSampleType = GAUGE;
        } else if (m instanceof Max) {
            childInstanceSampleNameSuffix = maxChildInstanceSampleNameSuffix;
            childInstanceSampleType = GAUGE;
        } else if (m instanceof Mean) {
            childInstanceSampleNameSuffix = meanChildInstanceSampleNameSuffix;
            childInstanceSampleType = GAUGE;
        }

        String nameSuffix = null;

        if (instance instanceof TimerInstance || instance instanceof HistogramInstance) {
            if (m instanceof Count) {
                nameSuffix = "_count";
            } else if (m instanceof TotalSum) {
                nameSuffix = "_sum";
            } else if (m instanceof Bucket) {
                nameSuffix = "_bucket";
            }
        }

        List<String> labelNames;
        List<String> labelValues;

        if (instanceSampleSpec.hasDimensionValues()) {
            labelNames = instanceSampleSpec.dimensionValues().stream().map(dv -> dv.dimension().name()).collect(toList());
            labelValues = instanceSampleSpec.dimensionValues().stream().map(MetricDimensionValue::value).collect(toList());

            if (m instanceof Percentile) {
                labelNames.add("quantile");
                Percentile p = (Percentile)m;
                labelValues.add(p.quantileAsString());
            } else if (m instanceof Bucket) {
                labelNames.add("le");
                labelValues.add(leValue(instance, (Bucket)m));
            }
        } else if (m instanceof Percentile) {
            labelNames = singletonList("quantile");
            Percentile p = (Percentile)m;
            labelValues = singletonList(p.quantileAsString());
        } else if (m instanceof Bucket) {
            labelNames = singletonList("le");
            labelValues = singletonList(leValue(instance, (Bucket)m));
        } else {
            labelNames = emptyList();
            labelValues = emptyList();
        }

        Collector.Type type = instanceSample.type();

        if (!separateHistogramAndSummary
            || !((type == HISTOGRAM && instance.isWithPercentiles()) || (type == SUMMARY && instance.isWithBuckets()))) {

            return new PrometheusSample(
                m,
                childInstanceSampleNameSuffix,
                childInstanceSampleType,
                null,
                nameSuffix,
                labelNames,
                labelValues,
                spec.value());
        }

        MetricName childSampleChildInstanceSampleNameSuffix =
            type == HISTOGRAM ?
            summaryChildInstanceSampleNameSuffix :
            histogramChildInstanceSampleNameSuffix;

        Collector.Type childSampleChildInstanceSampleType = type == HISTOGRAM ? SUMMARY : HISTOGRAM;

        if (m instanceof Count || m instanceof TotalSum) {
            PrometheusSample childSample = new PrometheusSample(
                m,
                childSampleChildInstanceSampleNameSuffix,
                childSampleChildInstanceSampleType,
                null,
                nameSuffix,
                labelNames,
                labelValues,
                spec.value());

            return new PrometheusSample(
                m,
                childInstanceSampleNameSuffix,
                childInstanceSampleType,
                null,
                nameSuffix,
                labelNames,
                labelValues,
                spec.value(),
                List.of(childSample));
        } else if (type == HISTOGRAM && m instanceof Percentile || type == SUMMARY && m instanceof Bucket) {
            return new PrometheusSample(
                m,
                childSampleChildInstanceSampleNameSuffix,
                childSampleChildInstanceSampleType,
                null,
                nameSuffix,
                labelNames,
                labelValues,
                spec.value());
        } else {
            return new PrometheusSample(
                m,
                childInstanceSampleNameSuffix,
                childInstanceSampleType,
                null,
                nameSuffix,
                labelNames,
                labelValues,
                spec.value());
        }
    }

    private String leValue(MetricInstance instance, Bucket bucket) {
        if (bucket.isInf()) {
            return "+Inf";
        } else if (bucket.isNegativeInf()) {
            return "-Inf";
        } else {
            return
                instance instanceof TimerInstance ?
                bucket.upperBoundSecAsNumberString() :
                bucket.upperBoundAsNumberString();
        }
    }
}