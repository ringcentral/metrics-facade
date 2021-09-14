package com.ringcentral.platform.metrics.spring;

import com.ringcentral.platform.metrics.micrometer.MfMeterRegistryConfig;

class MfPropertiesConfigAdapter extends PropertiesConfigAdapter<MfProperties> implements MfMeterRegistryConfig {

    MfPropertiesConfigAdapter(MfProperties properties) {
        super(properties);
    }

    @Override
    public String prefix() {
        return MfProperties.PREFIX;
    }

    @Override
    public String get(String key) {
        return null;
    }
}
