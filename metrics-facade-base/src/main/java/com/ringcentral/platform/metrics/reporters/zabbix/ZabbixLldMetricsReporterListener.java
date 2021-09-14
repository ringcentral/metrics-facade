package com.ringcentral.platform.metrics.reporters.zabbix;

import java.io.Closeable;

public interface ZabbixLldMetricsReporterListener extends Closeable {
    void entityAdded(ZEntity entity);
    void entityRemoved(ZEntity entity);
}
