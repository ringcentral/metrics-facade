package com.ringcentral.platform.metrics.spring.prometheus;

import com.ringcentral.platform.metrics.spring.MfProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = MfPrometheusProperties.PREFIX)
public class MfPrometheusProperties {

    public static final String PREFIX = MfProperties.PREFIX + ".prometheus";

    private Boolean convertNameToLowercase;

    public Boolean getConvertNameToLowercase() {
        return convertNameToLowercase;
    }

    public void setConvertNameToLowercase(Boolean convertNameToLowercase) {
        this.convertNameToLowercase = convertNameToLowercase;
    }
}
