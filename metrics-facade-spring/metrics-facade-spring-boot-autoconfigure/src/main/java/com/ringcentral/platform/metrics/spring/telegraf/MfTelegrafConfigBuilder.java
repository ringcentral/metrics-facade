package com.ringcentral.platform.metrics.spring.telegraf;

import com.ringcentral.platform.metrics.samples.*;

import static com.ringcentral.platform.metrics.reporters.telegraf.TelegrafMetricsJsonExporter.DEFAULT_GROUP_BY_TYPE;

public class MfTelegrafConfigBuilder {

    private Boolean groupByType;
    private InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider;

    public MfTelegrafConfigBuilder() {}

    public MfTelegrafConfigBuilder rebase(MfTelegrafConfigBuilder base) {
        if (base.groupByType != null && groupByType == null) {
            groupByType = base.groupByType;
        }

        if (base.instanceSamplesProvider != null && instanceSamplesProvider == null) {
            instanceSamplesProvider = base.instanceSamplesProvider;
        }

        return this;
    }

    public MfTelegrafConfigBuilder groupByType(Boolean groupByType) {
        this.groupByType = groupByType;
        return this;
    }

    public MfTelegrafConfigBuilder instanceSamplesProvider(InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider) {
        this.instanceSamplesProvider = instanceSamplesProvider;
        return this;
    }

    public MfTelegrafConfig build() {
        boolean groupByType = DEFAULT_GROUP_BY_TYPE;

        if (this.groupByType != null) {
            groupByType = this.groupByType;
        }

        return new MfTelegrafConfig(groupByType, instanceSamplesProvider);
    }
}
