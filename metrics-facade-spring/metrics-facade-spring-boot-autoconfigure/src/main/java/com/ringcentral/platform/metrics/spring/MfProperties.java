package com.ringcentral.platform.metrics.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = MfProperties.PREFIX)
public class MfProperties {

    public static final String PREFIX = "management.metrics.export.mf";
}
