package com.ringcentral.platform.metrics.spring.prometheus;

import com.ringcentral.platform.metrics.samples.InstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.*;

import java.util.Locale;

import static com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter.*;

public class MfPrometheusConfigBuilder {

    private InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider;
    private Boolean convertNameToLowercase;
    private Locale locale;

    public MfPrometheusConfigBuilder() {}

    public MfPrometheusConfigBuilder rebase(MfPrometheusConfigBuilder base) {
        if (base.instanceSamplesProvider != null && instanceSamplesProvider == null) {
            instanceSamplesProvider = base.instanceSamplesProvider;
        }

        if (base.convertNameToLowercase != null && convertNameToLowercase == null) {
            convertNameToLowercase = base.convertNameToLowercase;
        }

        if (base.locale != null && locale == null) {
            locale = base.locale;
        }

        return this;
    }

    public MfPrometheusConfigBuilder instanceSamplesProvider(InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider) {
        this.instanceSamplesProvider = instanceSamplesProvider;
        return this;
    }

    public MfPrometheusConfigBuilder convertNameToLowercase(Boolean convertNameToLowercase) {
        this.convertNameToLowercase = convertNameToLowercase;
        return this;
    }

    public MfPrometheusConfigBuilder locale(Locale locale) {
        this.locale = locale;
        return this;
    }

    public MfPrometheusConfig build() {
        boolean convertNameToLowercase = DEFAULT_CONVERT_NAME_TO_LOWER_CASE;

        if (this.convertNameToLowercase != null) {
            convertNameToLowercase = this.convertNameToLowercase;
        }

        return new MfPrometheusConfig(
            instanceSamplesProvider,
            convertNameToLowercase,
            locale != null ? locale : DEFAULT_LOCALE);
    }
}
