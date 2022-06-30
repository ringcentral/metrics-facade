package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionUtils;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.List;
import java.util.Map;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;

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
        return MetricDimensionUtils.hasDimensionValues(dimensionValues);
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

    public boolean hasDimension(MetricDimension dimension) {
        return MetricDimensionUtils.hasDimension(dimensionValues, dimension);
    }

    public String valueOf(MetricDimension dimension) {
        return MetricDimensionUtils.valueOf(dimensionValues, dimension);
    }

    public MetricDimensionValue dimensionValueOf(MetricDimension dimension) {
        return MetricDimensionUtils.dimensionValueOf(dimensionValues, dimension);
    }

    public Map<MetricDimension, MetricDimensionValue> dimensionToValue() {
        return MetricDimensionUtils.dimensionToValue(dimensionValues);
    }

    public List<MetricDimensionValue> dimensionValuesWithout(MetricDimension dimension, MetricDimension... dimensions) {
        return MetricDimensionUtils.dimensionValuesWithout(dimensionValues, dimension, dimensions);
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
