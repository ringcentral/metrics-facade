package com.ringcentral.platform.metrics.spring.zabbix.lld;

import com.ringcentral.platform.metrics.spring.MfMetricsExportAutoConfiguration;
import com.ringcentral.platform.metrics.reporters.zabbix.*;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(MfMetricsExportAutoConfiguration.class)
@ConditionalOnProperty(prefix = MfZabbixLldProperties.PREFIX, name = "enabled")
@EnableConfigurationProperties(MfZabbixLldProperties.class)
public class MfZabbixLldAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MfZabbixLldConfigBuilder mfZabbixLldConfigBuilder() {
        return new MfZabbixLldConfigBuilder();
    }

    @Bean
    @ConditionalOnMissingBean
    public ZGroupMBeansExporterCustomizer zGroupMBeansExporterCustomizer() {
        return e -> {};
    }

    @Bean
    @ConditionalOnMissingBean
    public ZabbixLldMetricsReporterCustomizer zabbixLldMetricsReporterCustomizer() {
        return r -> {};
    }

    @Bean
    @ConditionalOnMissingBean
    public ZabbixLldMetricsReporter zabbixLldMetricsReporter(
        MfZabbixLldProperties properties,
        MfZabbixLldConfigBuilder configBuilder,
        ZGroupMBeansExporterCustomizer zGroupMBeansExporterCustomizer,
        ZabbixLldMetricsReporterCustomizer customizer) {

        configBuilder = new MfZabbixLldConfigBuilder()
            .objectNamePrefix(properties.getObjectNamePrefix())
            .groupJsonAttrName(properties.getGroupJsonAttrName())
            .rebase(configBuilder);

        MfZabbixLldConfig config = configBuilder.build();

        ZGroupMBeansExporter zGroupMBeansExporter = new ZGroupMBeansExporter(
            config.objectNamePrefix(),
            config.hasGroupJsonMapper() ? config.groupJsonMapper() : DefaultZGroupJsonMapper.INSTANCE,
            config.groupJsonAttrName());

        zGroupMBeansExporterCustomizer.customizeZGroupMBeansExporter(zGroupMBeansExporter);

        ZabbixLldMetricsReporter reporter =
            config.hasRulesProvider() ?
            new ZabbixLldMetricsReporter(config.rulesProvider(), zGroupMBeansExporter) :
            new ZabbixLldMetricsReporter(zGroupMBeansExporter);

        customizer.customizeZabbixLldMetricsReporter(reporter);
        return reporter;
    }
}
