package com.ringcentral.platform.metrics.spring.zabbix;

import com.ringcentral.platform.metrics.spring.MfProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = MfZabbixProperties.PREFIX)
public class MfZabbixProperties {

    public static final String PREFIX = MfProperties.PREFIX + ".zabbix";
}
