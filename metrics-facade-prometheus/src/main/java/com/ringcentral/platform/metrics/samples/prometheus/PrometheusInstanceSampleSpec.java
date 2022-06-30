package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionUtils;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
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
    private List<MetricDimensionValue> dimensionValues;

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
        List<MetricDimensionValue> dimensionValues) {

        this.enabled = enabled;
        this.instance = instance;
        this.name = name;
        this.description = description;
        this.dimensionValues = dimensionValues;
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

    public boolean hasDimensionValues() {
        return dimensionValues != null && !dimensionValues.isEmpty();
    }

    public PrometheusInstanceSampleSpec noDimensionValues() {
        return dimensionValues(emptyList());
    }

    public PrometheusInstanceSampleSpec dimensionValues(List<MetricDimensionValue> dimensionValues) {
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

        if (prometheusMod.dimensionValues != null) {
            dimensionValues = prometheusMod.dimensionValues;
        }

        return this;
    }
}
