package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.samples.InstanceSampleSpec;

import java.util.List;

import static java.lang.Boolean.*;
import static java.util.Collections.*;

public class PrometheusInstanceSampleSpec implements InstanceSampleSpec {

    private Boolean enabled;
    private final MetricInstance instance;
    private MetricName name;
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
        List<MetricDimensionValue> dimensionValues) {

        this.enabled = enabled;
        this.instance = instance;
        this.name = name;
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

        if (prometheusMod.dimensionValues != null) {
            dimensionValues = prometheusMod.dimensionValues;
        }

        return this;
    }
}
