package com.ringcentral.platform.metrics.reporters.jmx;

import java.util.*;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import static java.lang.Boolean.*;
import static java.util.Collections.*;

public final class MBeanSpec {

    private Boolean enabled;
    private MetricName name;
    private Set<Measurable> measurables;
    private List<MetricDimensionValue> dimensionValues;

    public static MBeanSpec mBeanSpec() {
        return new MBeanSpec();
    }

    public MBeanSpec() {}

    public MBeanSpec(
        Boolean enabled,
        MetricName name,
        Set<Measurable> measurables,
        List<MetricDimensionValue> dimensionValues) {

        this.enabled = enabled;
        this.name = name;
        this.measurables = measurables;
        this.dimensionValues = dimensionValues;
    }

    public boolean hasEnabled() {
        return enabled != null;
    }

    public MBeanSpec enable() {
        return enabled(TRUE);
    }

    public MBeanSpec disable() {
        return enabled(FALSE);
    }

    public MBeanSpec enabled(Boolean enabled) {
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

    public MBeanSpec name(MetricName name) {
        this.name = name;
        return this;
    }

    public MetricName name() {
        return name;
    }

    public boolean hasMeasurables() {
        return measurables != null && !measurables.isEmpty();
    }

    public MBeanSpec noMeasurables() {
        return measurables(emptySet());
    }

    public MBeanSpec measurables(Set<Measurable> measurables) {
        this.measurables = measurables;
        return this;
    }

    public Set<Measurable> measurables() {
        return measurables;
    }

    public boolean hasDimensionValues() {
        return dimensionValues != null && !dimensionValues.isEmpty();
    }

    public MBeanSpec noDimensionValues() {
        return dimensionValues(emptyList());
    }

    public MBeanSpec dimensionValues(List<MetricDimensionValue> dimensionValues) {
        this.dimensionValues = dimensionValues;
        return this;
    }

    public List<MetricDimensionValue> dimensionValues() {
        return dimensionValues;
    }

    public MBeanSpec modify(MBeanSpec mod) {
        if (mod.enabled != null) {
            enabled = mod.enabled;
        }

        if (mod.name != null) {
            name = mod.name;
        }

        if (mod.hasMeasurables()) {
            measurables = mod.measurables();
        }

        if (mod.dimensionValues != null) {
            dimensionValues = mod.dimensionValues;
        }

        return this;
    }
}
