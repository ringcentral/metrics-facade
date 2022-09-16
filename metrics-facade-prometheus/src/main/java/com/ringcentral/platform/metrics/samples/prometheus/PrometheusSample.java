package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.samples.Sample;
import io.prometheus.client.Collector;

import java.util.List;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.Collections.emptyList;

public class PrometheusSample implements Sample<PrometheusSample> {

    private final Measurable measurable;
    private final MetricName childInstanceSampleNameSuffix;
    private final Collector.Type childInstanceSampleType;
    private final MetricName name;
    private final String nameSuffix;
    private final List<String> labelNames;
    private final List<String> labelValues;
    private final double value;
    private final List<PrometheusSample> children;

    public PrometheusSample(
        Measurable measurable,
        MetricName childInstanceSampleNameSuffix,
        Collector.Type childInstanceSampleType,
        MetricName name,
        String nameSuffix,
        List<String> labelNames,
        List<String> labelValues,
        double value) {

        this(
            measurable,
            childInstanceSampleNameSuffix,
            childInstanceSampleType,
            name,
            nameSuffix,
            labelNames,
            labelValues,
            value,
            emptyList());
    }

    public PrometheusSample(
        Measurable measurable,
        MetricName childInstanceSampleNameSuffix,
        Collector.Type childInstanceSampleType,
        MetricName name,
        String nameSuffix,
        List<String> labelNames,
        List<String> labelValues,
        double value,
        List<PrometheusSample> children) {

        this.measurable = measurable;

        if (childInstanceSampleNameSuffix != null) {
            checkArgument(!childInstanceSampleNameSuffix.isEmpty(), "childInstanceSampleNameSuffix is empty");
            checkArgument(childInstanceSampleType != null, "childInstanceSampleType is null");
        }

        this.childInstanceSampleNameSuffix = childInstanceSampleNameSuffix;
        this.childInstanceSampleType = childInstanceSampleType;

        this.name = name;
        this.nameSuffix = nameSuffix;
        this.labelNames = labelNames != null ? labelNames : emptyList();
        this.labelValues = labelValues != null ? labelValues : emptyList();
        this.value = value;

        this.children = children != null ? children : emptyList();
    }

    public boolean hasMeasurable() {
        return measurable != null;
    }

    public Measurable measurable() {
        return measurable;
    }

    public boolean belongsToChildInstanceSample() {
        return childInstanceSampleNameSuffix != null;
    }

    public MetricName childInstanceSampleNameSuffix() {
        return childInstanceSampleNameSuffix;
    }

    public Collector.Type childInstanceSampleType() {
        return childInstanceSampleType;
    }

    public PrometheusSample notBelongingToChildInstanceSample() {
        return new PrometheusSample(
            measurable,
            null,
            null,
            name,
            nameSuffix,
            labelNames,
            labelValues,
            value,
            children);
    }

    public boolean hasName() {
        return name != null;
    }

    public MetricName name() {
        return name;
    }

    public boolean hasNameSuffix() {
        return nameSuffix != null;
    }

    public String nameSuffix() {
        return nameSuffix;
    }

    public List<String> labelNames() {
        return labelNames;
    }

    public List<String> labelValues() {
        return labelValues;
    }

    public double value() {
        return value;
    }

    @Override
    public List<PrometheusSample> children() {
        return children;
    }
}
