package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelUtils;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.samples.InstanceSampleSpec;

import java.util.List;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;

public class PrometheusInstanceSampleSpec implements InstanceSampleSpec {

    private Boolean enabled;
    private final MetricInstance instance;
    private MetricName name;
    private String description;
    private List<LabelValue> labelValues;

    public static PrometheusInstanceSampleSpec prometheusInstanceSampleSpec() {
        return instanceSampleSpec();
    }

    public static PrometheusInstanceSampleSpec instanceSampleSpec() {
        return new PrometheusInstanceSampleSpec();
    }

    public PrometheusInstanceSampleSpec() {
        this.instance = null;
    }

    public PrometheusInstanceSampleSpec(
        Boolean enabled,
        MetricInstance instance,
        MetricName name,
        String description,
        List<LabelValue> labelValues) {

        this.enabled = enabled;
        this.instance = instance;
        this.name = name;
        this.description = description;
        this.labelValues = labelValues;
    }

    public boolean hasEnabled() {
        return enabled != null;
    }

    public PrometheusInstanceSampleSpec enable() {
        return enabled(TRUE);
    }

    public PrometheusInstanceSampleSpec disable() {
        return enabled(FALSE);
    }

    public PrometheusInstanceSampleSpec enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public boolean isEnabled() {
        return !hasEnabled() || getEnabled();
    }

    public boolean hasInstance() {
        return instance != null;
    }

    public MetricInstance instance() {
        return instance;
    }

    public boolean hasName() {
        return name != null;
    }

    public PrometheusInstanceSampleSpec name(MetricName name) {
        this.name = name;
        return this;
    }

    public MetricName name() {
        return name;
    }

    public PrometheusInstanceSampleSpec description(String description) {
        this.description = description;
        return this;
    }

    public String description() {
        return description;
    }

    public boolean hasLabelValues() {
        return labelValues != null && !labelValues.isEmpty();
    }

    public PrometheusInstanceSampleSpec noLabelValues() {
        return labelValues(emptyList());
    }

    public PrometheusInstanceSampleSpec labelValues(List<LabelValue> labelValues) {
        this.labelValues = labelValues;
        return this;
    }

    public List<LabelValue> labelValues() {
        return labelValues;
    }

    public boolean hasLabel(Label label) {
        return LabelUtils.hasLabel(labelValues, label);
    }

    public String valueOf(Label label) {
        return LabelUtils.valueOf(labelValues, label);
    }

    public LabelValue labelValueOf(Label label) {
        return LabelUtils.labelValueOf(labelValues, label);
    }

    public Map<Label, LabelValue> labelToValue() {
        return LabelUtils.labelToValue(labelValues);
    }

    public List<LabelValue> labelValuesWithout(Label label, Label... labels) {
        return LabelUtils.labelValuesWithout(labelValues, label, labels);
    }

    @Override
    public PrometheusInstanceSampleSpec modify(InstanceSampleSpec mod) {
        if (!(mod instanceof PrometheusInstanceSampleSpec)) {
            return this;
        }

        PrometheusInstanceSampleSpec prometheusMod = (PrometheusInstanceSampleSpec)mod;

        if (prometheusMod.enabled != null) {
            enabled = prometheusMod.enabled;
        }

        if (prometheusMod.name != null) {
            name = prometheusMod.name;
        }

        if (prometheusMod.description != null) {
            description = prometheusMod.description;
        }

        if (prometheusMod.labelValues != null) {
            labelValues = prometheusMod.labelValues;
        }

        return this;
    }
}
