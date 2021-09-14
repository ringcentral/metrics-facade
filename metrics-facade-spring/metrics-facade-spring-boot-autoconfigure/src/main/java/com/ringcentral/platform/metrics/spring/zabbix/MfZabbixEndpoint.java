package com.ringcentral.platform.metrics.spring.zabbix;

import com.ringcentral.platform.metrics.reporters.MetricsJson;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixMetricsJsonExporter;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@WebEndpoint(id = "mfzabbix")
public class MfZabbixEndpoint {

    private final ZabbixMetricsJsonExporter exporter;

    public MfZabbixEndpoint(ZabbixMetricsJsonExporter exporter) {
        this.exporter = exporter;
    }

    @ReadOperation
    public WebEndpointResponse<MetricsJson> export() {
        try {
            return new WebEndpointResponse<>(exporter.exportMetrics(), APPLICATION_JSON);
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to export metrics", e);
        }
    }
}
