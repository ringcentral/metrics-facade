package com.ringcentral.platform.metrics.spring.telegraf;

import com.ringcentral.platform.metrics.spring.MfProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = MfTelegrafProperties.PREFIX)
public class MfTelegrafProperties {

    public static final String PREFIX = MfProperties.PREFIX + ".telegraf";

    private Boolean groupByType;

    public Boolean getGroupByType() {
        return groupByType;
    }

    public void setGroupByType(Boolean groupByType) {
        this.groupByType = groupByType;
    }
}
