package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionUtils;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.List;
import java.util.Map;

import static java.lang.Boolean.*;
import static java.util.Collections.*;
import static org.apache.commons.lang3.StringUtils.*;

public class DefaultSampleSpec implements SampleSpec {

    private Boolean enabled;
    private MetricName name;
    private String measurableName;
    private List<MetricDimensionValue> dimensionValues;
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
        List<MetricDimensionValue> dimensionValues,
        Object value,
        String type) {

        this.enabled = enabled;
        this.name = name;
        this.measurableName = measurableName;
        this.dimensionValues = dimensionValues;
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
        return measurableName(EMPTY);
    }

    public DefaultSampleSpec measurableName(String measurableName) {
        this.measurableName = measurableName;
        return this;
    }

    public String measurableName() {
        return measurableName;
    }

    public boolean hasDimensionValues() {
        return dimensionValues != null && !dimensionValues.isEmpty();
    }

    public DefaultSampleSpec noDimensionValues() {
        return dimensionValues(emptyList());
    }

    public DefaultSampleSpec dimensionValues(List<MetricDimensionValue> dimensionValues) {
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

        if (defaultMod.dimensionValues != null) {
            dimensionValues = defaultMod.dimensionValues;
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
