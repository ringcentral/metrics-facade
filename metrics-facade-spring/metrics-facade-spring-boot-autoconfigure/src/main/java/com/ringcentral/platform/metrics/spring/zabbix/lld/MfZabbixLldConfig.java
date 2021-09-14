package com.ringcentral.platform.metrics.spring.zabbix.lld;

import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.reporters.zabbix.ZGroupJsonMapper;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter.Rule;

public class MfZabbixLldConfig {

    private final PredicativeMetricNamedInfoProvider<Rule> rulesProvider;
    private final String objectNamePrefix;
    private final ZGroupJsonMapper groupJsonMapper;
    private final String groupJsonAttrName;

    public MfZabbixLldConfig(
        PredicativeMetricNamedInfoProvider<Rule> rulesProvider,
        String objectNamePrefix,
        ZGroupJsonMapper groupJsonMapper,
        String groupJsonAttrName) {

        this.rulesProvider = rulesProvider;
        this.objectNamePrefix = objectNamePrefix;
        this.groupJsonMapper = groupJsonMapper;
        this.groupJsonAttrName = groupJsonAttrName;
    }

    public boolean hasRulesProvider() {
        return rulesProvider != null;
    }

    public PredicativeMetricNamedInfoProvider<Rule> rulesProvider() {
        return rulesProvider;
    }

    public String objectNamePrefix() {
        return objectNamePrefix;
    }

    public boolean hasGroupJsonMapper() {
        return groupJsonMapper != null;
    }

    public ZGroupJsonMapper groupJsonMapper() {
        return groupJsonMapper;
    }

    public String groupJsonAttrName() {
        return groupJsonAttrName;
    }
}
