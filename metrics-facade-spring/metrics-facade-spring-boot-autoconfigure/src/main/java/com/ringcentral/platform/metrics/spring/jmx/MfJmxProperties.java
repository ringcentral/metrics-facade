package com.ringcentral.platform.metrics.spring.jmx;

import com.ringcentral.platform.metrics.spring.MfProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = MfJmxProperties.PREFIX)
public class MfJmxProperties {

    public static final String PREFIX = MfProperties.PREFIX + ".jmx";

    private String domainName;

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
}
