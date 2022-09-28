package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.samples.Sample;
import io.prometheus.client.Collector;

import java.util.List;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.Collections.emptyList;

public class PrometheusSample implements Sample {

    private final Measurable measurable;
    private final MetricName childInstanceSampleNameSuffix;
    private final Collector.Type childInstanceSampleType;
    private final MetricName name;
    private final String nameSuffix;
    private final List<String> labelNames;
    private final List<String> labelValues;
    private final double value;

    public PrometheusSample(
        Measurable measurable,
        MetricName childInstanceSampleNameSuffix,
        Collector.Type childInstanceSampleType,
        MetricName name,
        String nameSuffix,
        List<String> labelNames,
        List<String> labelValues,
        double value) {

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
            value);
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
    public String toString() {
        return "PrometheusSample{" +
            "measurable=" + measurable +
            ", childInstanceSampleNameSuffix=" + childInstanceSampleNameSuffix +
            ", childInstanceSampleType=" + childInstanceSampleType +
            ", name=" + name +
            ", nameSuffix='" + nameSuffix + '\'' +
            ", labelNames=" + labelNames +
            ", labelValues=" + labelValues +
            ", value=" + value +
            '}';
    }
}
