package com.ringcentral.platform.metrics.spring.telegraf;

import com.ringcentral.platform.metrics.reporters.telegraf.TelegrafMetricsJsonExporter;

public interface TelegrafMetricsJsonExporterCustomizer {
    void customizeTelegrafMetricsJsonExporter(TelegrafMetricsJsonExporter exporter);
}
