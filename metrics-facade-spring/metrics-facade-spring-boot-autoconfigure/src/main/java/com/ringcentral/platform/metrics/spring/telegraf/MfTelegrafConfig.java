package com.ringcentral.platform.metrics.spring.telegraf;

import com.ringcentral.platform.metrics.samples.*;

public class MfTelegrafConfig {

    private final boolean groupByType;
    private final InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider;

    public MfTelegrafConfig(
        boolean groupByType,
        InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider) {

        this.groupByType = groupByType;
        this.instanceSamplesProvider = instanceSamplesProvider;
    }

    public boolean groupByType() {
        return groupByType;
    }

    public boolean hasInstanceSamplesProvider() {
        return instanceSamplesProvider != null;
    }

    public InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider() {
        return instanceSamplesProvider;
    }
}
