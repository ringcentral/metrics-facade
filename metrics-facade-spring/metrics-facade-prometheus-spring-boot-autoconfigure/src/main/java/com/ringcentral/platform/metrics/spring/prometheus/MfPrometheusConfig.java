package com.ringcentral.platform.metrics.spring.prometheus;

import com.ringcentral.platform.metrics.samples.*;
import com.ringcentral.platform.metrics.samples.prometheus.*;

import java.util.Locale;

public class MfPrometheusConfig {

    private final InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider;
    private final boolean convertNameToLowercase;
    private final Locale locale;

    public MfPrometheusConfig(
        InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider,
        boolean convertNameToLowercase,
        Locale locale) {

        this.instanceSamplesProvider = instanceSamplesProvider;
        this.convertNameToLowercase = convertNameToLowercase;
        this.locale = locale;
    }

    public boolean hasInstanceSamplesProvider() {
        return instanceSamplesProvider != null;
    }

    public InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider() {
        return instanceSamplesProvider;
    }

    public boolean convertNameToLowercase() {
        return convertNameToLowercase;
    }

    public boolean hasLocale() {
        return locale != null;
    }

    public Locale locale() {
        return locale;
    }
}
