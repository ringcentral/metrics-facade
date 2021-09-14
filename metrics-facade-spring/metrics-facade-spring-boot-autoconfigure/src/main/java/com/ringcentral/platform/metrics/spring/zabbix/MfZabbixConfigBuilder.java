package com.ringcentral.platform.metrics.spring.zabbix;

import com.ringcentral.platform.metrics.samples.*;

public class MfZabbixConfigBuilder {

    private InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider;

    public MfZabbixConfigBuilder() {}

    public MfZabbixConfigBuilder rebase(MfZabbixConfigBuilder base) {
        if (base.instanceSamplesProvider != null && instanceSamplesProvider == null) {
            instanceSamplesProvider = base.instanceSamplesProvider;
        }

        return this;
    }

    public MfZabbixConfigBuilder instanceSamplesProvider(InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider) {
        this.instanceSamplesProvider = instanceSamplesProvider;
        return this;
    }

    public MfZabbixConfig build() {
        return new MfZabbixConfig(instanceSamplesProvider);
    }
}
