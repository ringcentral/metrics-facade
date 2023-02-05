package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelUtils;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.List;
import java.util.Map;

import static com.ringcentral.platform.metrics.utils.CollectionUtils.isNonEmpty;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;

public class DefaultInstanceSampleSpec implements InstanceSampleSpec {

    private Boolean enabled;
    private MetricName name;
    private List<LabelValue> labelValues;
    private Boolean withMeasurableName;

    public static DefaultInstanceSampleSpec instanceSampleSpec() {
        return new DefaultInstanceSampleSpec();
    }

    public DefaultInstanceSampleSpec() {}

    public DefaultInstanceSampleSpec(
        Boolean enabled,
        MetricName name,
        List<LabelValue> labelValues,
        Boolean withMeasurableName) {

        this.enabled = enabled;
        this.name = name;
        this.labelValues = labelValues;
        this.withMeasurableName = withMeasurableName;
    }

    public boolean hasEnabled() {
        return enabled != null;
    }

    public DefaultInstanceSampleSpec enable() {
        return enabled(TRUE);
    }

    public DefaultInstanceSampleSpec disable() {
        return enabled(FALSE);
    }

    public DefaultInstanceSampleSpec enabled(Boolean enabled) {
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

    public DefaultInstanceSampleSpec name(MetricName name) {
        this.name = name;
        return this;
    }

    public MetricName name() {
        return name;
    }

    public boolean hasLabelValues() {
        return isNonEmpty(labelValues);
    }

    public DefaultInstanceSampleSpec noLabelValues() {
        return labelValues(emptyList());
    }

    public DefaultInstanceSampleSpec labelValues(List<LabelValue> labelValues) {
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

    public boolean hasWithMeasurableName() {
        return withMeasurableName != null;
    }

    public DefaultInstanceSampleSpec withMeasurableName() {
        return withMeasurableName(TRUE);
    }

    public DefaultInstanceSampleSpec withoutMeasurableName() {
        return noMeasurableName();
    }

    public DefaultInstanceSampleSpec noMeasurableName() {
        return withMeasurableName(FALSE);
    }

    public DefaultInstanceSampleSpec withMeasurableName(Boolean withMeasurableName) {
        this.withMeasurableName = withMeasurableName;
        return this;
    }

    public Boolean getWithMeasurableName() {
        return withMeasurableName;
    }

    public boolean isWithMeasurableName() {
        return !hasWithMeasurableName() || getWithMeasurableName();
    }

    @Override
    public DefaultInstanceSampleSpec modify(InstanceSampleSpec mod) {
        if (!(mod instanceof DefaultInstanceSampleSpec)) {
            return this;
        }

        DefaultInstanceSampleSpec defaultMod = (DefaultInstanceSampleSpec)mod;

        if (defaultMod.enabled != null) {
            enabled = defaultMod.enabled;
        }

        if (defaultMod.name != null) {
            name = defaultMod.name;
        }

        if (defaultMod.labelValues != null) {
            labelValues = defaultMod.labelValues;
        }

        if (defaultMod.withMeasurableName != null) {
            withMeasurableName = defaultMod.withMeasurableName;
        }

        return this;
    }
}
