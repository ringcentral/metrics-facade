package com.ringcentral.platform.metrics.spring.zabbix.lld;

import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter;

public interface ZabbixLldMetricsReporterCustomizer {
    void customizeZabbixLldMetricsReporter(ZabbixLldMetricsReporter reporter);
}
