package com.ringcentral.platform.metrics.spring.zabbix;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixMetricsJsonExporter;
import com.ringcentral.platform.metrics.samples.DefaultInstanceSamplesProvider;
import com.ringcentral.platform.metrics.spring.MfMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(MfMetricsExportAutoConfiguration.class)
@ConditionalOnProperty(prefix = MfZabbixProperties.PREFIX, name = "enabled")
@EnableConfigurationProperties(MfZabbixProperties.class)
public class MfZabbixAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MfZabbixConfigBuilder mfZabbixConfigBuilder(MetricRegistry registry) {
        return new MfZabbixConfigBuilder().instanceSamplesProvider(new DefaultInstanceSamplesProvider(registry));
    }

    @Bean
    @ConditionalOnMissingBean
    public ZabbixMetricsJsonExporterCustomizer zabbixMetricsJsonExporterCustomizer(MetricRegistry registry) {
        return e -> {};
    }

    @Bean
    @ConditionalOnMissingBean
    public ZabbixMetricsJsonExporter zabbixMetricsJsonExporter(
        MfZabbixProperties properties,
        MfZabbixConfigBuilder configBuilder,
        ZabbixMetricsJsonExporterCustomizer customizer,
        MetricRegistry registry) {

        MfZabbixConfig config = configBuilder.build();

        ZabbixMetricsJsonExporter exporter =
            config.hasInstanceSamplesProvider() ?
            new ZabbixMetricsJsonExporter(config.instanceSamplesProvider()) :
            new ZabbixMetricsJsonExporter(registry);

        customizer.customizeZabbixMetricsJsonExporter(exporter);
        return exporter;
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAvailableEndpoint(endpoint = MfZabbixEndpoint.class)
    @ConditionalOnProperty(prefix = MfZabbixProperties.PREFIX, name = "enabled")
    public static class ZabbixMetricsJsonEndpointConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public MfZabbixEndpoint mfZabbixEndpoint(ZabbixMetricsJsonExporter exporter) {
            return new MfZabbixEndpoint(exporter);
        }
    }
}
