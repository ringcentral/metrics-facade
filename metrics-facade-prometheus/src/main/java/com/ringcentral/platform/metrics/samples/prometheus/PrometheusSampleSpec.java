package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.samples.SampleSpec;

import static java.lang.Boolean.*;

public class PrometheusSampleSpec implements SampleSpec {

    private Boolean enabled;
    private final Measurable measurable;
    private Double value;

    public static PrometheusSampleSpec prometheusSampleSpec() {
        return sampleSpec();
    }

    public static PrometheusSampleSpec sampleSpec() {
        return new PrometheusSampleSpec();
    }

    public PrometheusSampleSpec() {
        this.measurable = null;
    }

    public PrometheusSampleSpec(
        Boolean enabled,
        Measurable measurable,
        Double value) {

        this.enabled = enabled;
        this.measurable = measurable;
        this.value = value;
    }

    public boolean hasEnabled() {
        return enabled != null;
    }

    public PrometheusSampleSpec enable() {
        return enabled(TRUE);
    }

    public PrometheusSampleSpec disable() {
        return enabled(FALSE);
    }

    public PrometheusSampleSpec enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public boolean isEnabled() {
        return !hasEnabled() || getEnabled();
    }

    public boolean hasMeasurable() {
        return measurable != null;
    }

    public Measurable measurable() {
        return measurable;
    }

    public boolean hasValue() {
        return value != null;
    }

    public PrometheusSampleSpec value(Double value) {
        this.value = value;
        return this;
    }

    public Double value() {
        return value;
    }

    @Override
    public PrometheusSampleSpec modify(SampleSpec mod) {
        if (!(mod instanceof PrometheusSampleSpec)) {
            return this;
        }

        PrometheusSampleSpec prometheusMod = (PrometheusSampleSpec)mod;

        if (prometheusMod.enabled != null) {
            enabled = prometheusMod.enabled;
        }

        if (prometheusMod.value != null) {
            value = prometheusMod.value;
        }

        return this;
    }
}
