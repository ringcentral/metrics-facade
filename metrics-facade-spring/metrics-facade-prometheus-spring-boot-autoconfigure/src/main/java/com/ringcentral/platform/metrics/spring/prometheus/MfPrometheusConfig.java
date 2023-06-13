package com.ringcentral.platform.metrics.spring.prometheus;

import com.ringcentral.platform.metrics.samples.InstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSample;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusSample;

public class MfPrometheusConfig {

    private final InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider;
    private final boolean convertNameToLowercase;

    public MfPrometheusConfig(
        InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider,
        boolean convertNameToLowercase) {

        this.instanceSamplesProvider = instanceSamplesProvider;
        this.convertNameToLowercase = convertNameToLowercase;
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
}
