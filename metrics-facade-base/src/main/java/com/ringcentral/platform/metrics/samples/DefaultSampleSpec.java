package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.List;
import java.util.Map;

import static com.ringcentral.platform.metrics.utils.StringUtils.*;
import static java.lang.Boolean.*;
import static java.util.Collections.emptyList;

public class DefaultSampleSpec implements SampleSpec {

    private Boolean enabled;
    private MetricName name;
    private String measurableName;
    private List<LabelValue> labelValues;
    private Object value;
    private String type;

    public static DefaultSampleSpec sampleSpec() {
        return new DefaultSampleSpec();
    }

    public DefaultSampleSpec() {}

    public DefaultSampleSpec(
        Boolean enabled,
        MetricName name,
        String measurableName,
        List<LabelValue> labelValues,
        Object value,
        String type) {

        this.enabled = enabled;
        this.name = name;
        this.measurableName = measurableName;
        this.labelValues = labelValues;
        this.value = value;
        this.type = type;
    }

    public boolean hasEnabled() {
        return enabled != null;
    }

    public DefaultSampleSpec enable() {
        return enabled(TRUE);
    }

    public DefaultSampleSpec disable() {
        return enabled(FALSE);
    }

    public DefaultSampleSpec enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public boolean isEnabled() {
        return !hasEnabled() || getEnabled();
    }

    public boolean hasName() {
        return name != null;
    }

    public DefaultSampleSpec name(MetricName name) {
        this.name = name;
        return this;
    }

    public MetricName name() {
        return name;
    }

    public boolean hasMeasurableName() {
        return isNotBlank(measurableName);
    }

    public DefaultSampleSpec noMeasurableName() {
        return measurableName(EMPTY_STRING);
    }

    public DefaultSampleSpec measurableName(String measurableName) {
        this.measurableName = measurableName;
        return this;
    }

    public String measurableName() {
        return measurableName;
    }

    public boolean hasLabelValues() {
        return labelValues != null && !labelValues.isEmpty();
    }

    public DefaultSampleSpec noLabelValues() {
        return labelValues(emptyList());
    }

    public DefaultSampleSpec labelValues(List<LabelValue> labelValues) {
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

    public boolean hasValue() {
        return value != null;
    }

    public DefaultSampleSpec value(Object value) {
        this.value = value;
        return this;
    }

    public Object value() {
        return value;
    }

    public boolean hasType() {
        return type != null;
    }

    public DefaultSampleSpec type(String type) {
        this.type = type;
        return this;
    }

    public String type() {
        return type;
    }

    @Override
    public DefaultSampleSpec modify(SampleSpec mod) {
        if (!(mod instanceof DefaultSampleSpec)) {
            return this;
        }

        DefaultSampleSpec defaultMod = (DefaultSampleSpec)mod;

        if (defaultMod.enabled != null) {
            enabled = defaultMod.enabled;
        }

        if (defaultMod.name != null) {
            name = defaultMod.name;
        }

        if (defaultMod.measurableName != null) {
            measurableName = defaultMod.measurableName;
        }

        if (defaultMod.labelValues != null) {
            labelValues = defaultMod.labelValues;
        }

        if (defaultMod.value != null) {
            value = defaultMod.value;
        }

        if (defaultMod.type != null) {
            type = defaultMod.type;
        }

        return this;
    }
}
