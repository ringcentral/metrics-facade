package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.samples.Sample;
import io.prometheus.client.Collector;

import java.util.List;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.Collections.emptyList;

public class PrometheusSample implements Sample {

    private final MetricName childInstanceSampleNameSuffix;
    private final Collector.Type childInstanceSampleType;

    private final String nameSuffix;
    private final List<String> labelNames;
    private final List<String> labelValues;
    private final double value;

    public PrometheusSample(
        MetricName childInstanceSampleNameSuffix,
        Collector.Type childInstanceSampleType,
        String nameSuffix,
        List<String> labelNames,
        List<String> labelValues,
        double value) {

        if (childInstanceSampleNameSuffix != null) {
            checkArgument(!childInstanceSampleNameSuffix.isEmpty(), "childInstanceSampleNameSuffix is empty");
            checkArgument(childInstanceSampleType != null, "childInstanceSampleType is null");
        }

        this.childInstanceSampleNameSuffix = childInstanceSampleNameSuffix;
        this.childInstanceSampleType = childInstanceSampleType;

        this.nameSuffix = nameSuffix;
        this.labelNames = labelNames != null ? labelNames : emptyList();
        this.labelValues = labelValues != null ? labelValues : emptyList();
        this.value = value;
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
            null,
            null,
            nameSuffix,
            labelNames,
            labelValues,
            value);
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
}
