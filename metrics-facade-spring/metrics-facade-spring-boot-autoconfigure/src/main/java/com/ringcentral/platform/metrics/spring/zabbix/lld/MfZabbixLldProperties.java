package com.ringcentral.platform.metrics.spring.zabbix.lld;

import com.ringcentral.platform.metrics.spring.MfProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = MfZabbixLldProperties.PREFIX)
public class MfZabbixLldProperties {

    public static final String PREFIX = MfProperties.PREFIX + ".zabbix-lld";

    @NotBlank
    private String objectNamePrefix;

    @NotBlank
    private String groupJsonAttrName;

    public String getObjectNamePrefix() {
        return objectNamePrefix;
    }

    public void setObjectNamePrefix(String objectNamePrefix) {
        this.objectNamePrefix = objectNamePrefix;
    }

    public String getGroupJsonAttrName() {
        return groupJsonAttrName;
    }

    public void setGroupJsonAttrName(String groupJsonAttrName) {
        this.groupJsonAttrName = groupJsonAttrName;
    }
}
