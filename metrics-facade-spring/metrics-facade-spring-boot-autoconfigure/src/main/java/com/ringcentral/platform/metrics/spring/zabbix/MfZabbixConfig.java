package com.ringcentral.platform.metrics.spring.zabbix;

import com.ringcentral.platform.metrics.samples.*;

public class MfZabbixConfig {

    private final InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider;

    public MfZabbixConfig(InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider) {
        this.instanceSamplesProvider = instanceSamplesProvider;
    }

    public boolean hasInstanceSamplesProvider() {
        return instanceSamplesProvider != null;
    }

    public InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider() {
        return instanceSamplesProvider;
    }
}
