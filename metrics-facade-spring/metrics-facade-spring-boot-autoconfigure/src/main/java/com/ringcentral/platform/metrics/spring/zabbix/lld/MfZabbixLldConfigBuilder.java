package com.ringcentral.platform.metrics.spring.zabbix.lld;

import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.reporters.zabbix.ZGroupJsonMapper;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter.Rule;

public class MfZabbixLldConfigBuilder {

    private PredicativeMetricNamedInfoProvider<Rule> rulesProvider;
    private String objectNamePrefix;
    private ZGroupJsonMapper groupJsonMapper;
    private String groupJsonAttrName;

    public MfZabbixLldConfigBuilder() {}

    public MfZabbixLldConfigBuilder rebase(MfZabbixLldConfigBuilder base) {
        if (base.rulesProvider != null && rulesProvider == null) {
            rulesProvider = base.rulesProvider;
        }

        if (base.objectNamePrefix != null && objectNamePrefix == null) {
            objectNamePrefix = base.objectNamePrefix;
        }

        if (base.groupJsonMapper != null && groupJsonMapper == null) {
            groupJsonMapper = base.groupJsonMapper;
        }

        if (base.groupJsonAttrName != null && groupJsonAttrName == null) {
            groupJsonAttrName = base.groupJsonAttrName;
        }

        return this;
    }

    public MfZabbixLldConfigBuilder rulesProvider(PredicativeMetricNamedInfoProvider<Rule> rulesProvider) {
        this.rulesProvider = rulesProvider;
        return this;
    }

    public MfZabbixLldConfigBuilder objectNamePrefix(String objectNamePrefix) {
        this.objectNamePrefix = objectNamePrefix;
        return this;
    }

    public MfZabbixLldConfigBuilder groupJsonMapper(ZGroupJsonMapper groupJsonMapper) {
        this.groupJsonMapper = groupJsonMapper;
        return this;
    }

    public MfZabbixLldConfigBuilder groupJsonAttrName(String groupJsonAttrName) {
        this.groupJsonAttrName = groupJsonAttrName;
        return this;
    }

    public MfZabbixLldConfig build() {
        return new MfZabbixLldConfig(
            rulesProvider,
            objectNamePrefix,
            groupJsonMapper,
            groupJsonAttrName);
    }
}
