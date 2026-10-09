package com.ringcentral.platform.metrics.spring;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.micrometer.MfMeterRegistry;
import com.ringcentral.platform.metrics.reporters.jmx.JmxMetricsReporter;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.reporters.telegraf.TelegrafMetricsJsonExporter;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixMetricsJsonExporter;
import com.ringcentral.platform.metrics.spring.prometheus.MfPrometheusEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
    classes = MetricsFacadeSpringBootSampleApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class MetricsFacadeSpringBootSampleAppTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void startsWithMetricsFacadeRegistryAndReporters() {
        assertNotNull(context.getBean(MetricRegistry.class));
        assertNotNull(context.getBean(MfMeterRegistry.class));
        assertNotNull(context.getBean(JmxMetricsReporter.class));
        assertNotNull(context.getBean(PrometheusMetricsExporter.class));
        assertNotNull(context.getBean(TelegrafMetricsJsonExporter.class));
        assertNotNull(context.getBean(ZabbixLldMetricsReporter.class));
        assertNotNull(context.getBean(ZabbixMetricsJsonExporter.class));
        assertNotNull(context.getBean(MfPrometheusEndpoint.class));
    }
}
