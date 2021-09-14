package com.ringcentral.platform.metrics.spring.zabbix;

import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixMetricsJsonExporter;

public interface ZabbixMetricsJsonExporterCustomizer {
    void customizeZabbixMetricsJsonExporter(ZabbixMetricsJsonExporter exporter);
}
