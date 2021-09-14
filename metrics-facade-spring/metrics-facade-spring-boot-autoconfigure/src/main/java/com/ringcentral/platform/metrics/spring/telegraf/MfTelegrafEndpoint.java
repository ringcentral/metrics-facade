package com.ringcentral.platform.metrics.spring.telegraf;

import com.ringcentral.platform.metrics.reporters.MetricsJson;
import com.ringcentral.platform.metrics.reporters.telegraf.TelegrafMetricsJsonExporter;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@WebEndpoint(id = "mftelegraf")
public class MfTelegrafEndpoint {

    private final TelegrafMetricsJsonExporter exporter;

    public MfTelegrafEndpoint(TelegrafMetricsJsonExporter exporter) {
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
