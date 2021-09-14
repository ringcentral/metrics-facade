package com.ringcentral.platform.metrics.spring.zabbix.lld;

import com.ringcentral.platform.metrics.reporters.zabbix.ZGroupMBeansExporter;

public interface ZGroupMBeansExporterCustomizer {
    void customizeZGroupMBeansExporter(ZGroupMBeansExporter exporter);
}
