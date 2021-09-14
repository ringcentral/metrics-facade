package com.ringcentral.platform.metrics.spring.prometheus;

import com.ringcentral.platform.metrics.spring.MfProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;

@ConfigurationProperties(prefix = MfPrometheusProperties.PREFIX)
public class MfPrometheusProperties {

    public static final String PREFIX = MfProperties.PREFIX + ".prometheus";

    private Boolean convertNameToLowercase;
    private Locale locale;

    public Boolean getConvertNameToLowercase() {
        return convertNameToLowercase;
    }

    public void setConvertNameToLowercase(Boolean convertNameToLowercase) {
        this.convertNameToLowercase = convertNameToLowercase;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
