package com.ringcentral.platform.metrics.samples;

import java.util.List;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.names.MetricName;
import static java.lang.Boolean.*;
import static java.util.Collections.*;

public class DefaultInstanceSampleSpec implements InstanceSampleSpec {

    private Boolean enabled;
    private MetricName name;
    private List<MetricDimensionValue> dimensionValues;
    private Boolean withMeasurableName;

    public static DefaultInstanceSampleSpec instanceSampleSpec() {
        return new DefaultInstanceSampleSpec();
    }

    public DefaultInstanceSampleSpec() {}

    public DefaultInstanceSampleSpec(
        Boolean enabled,
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        Boolean withMeasurableName) {

        this.enabled = enabled;
        this.name = name;
        this.dimensionValues = dimensionValues;
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

    public boolean hasDimensionValues() {
        return dimensionValues != null && !dimensionValues.isEmpty();
    }

    public DefaultInstanceSampleSpec noDimensionValues() {
        return dimensionValues(emptyList());
    }

    public DefaultInstanceSampleSpec dimensionValues(List<MetricDimensionValue> dimensionValues) {
        this.dimensionValues = dimensionValues;
        return this;
    }

    public List<MetricDimensionValue> dimensionValues() {
        return dimensionValues;
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

        if (defaultMod.dimensionValues != null) {
            dimensionValues = defaultMod.dimensionValues;
        }

        if (defaultMod.withMeasurableName != null) {
            withMeasurableName = defaultMod.withMeasurableName;
        }

        return this;
    }
}
