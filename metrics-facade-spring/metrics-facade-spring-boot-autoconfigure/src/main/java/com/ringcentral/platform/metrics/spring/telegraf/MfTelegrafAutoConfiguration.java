package com.ringcentral.platform.metrics.spring.telegraf;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.reporters.telegraf.TelegrafMetricsJsonExporter;
import com.ringcentral.platform.metrics.samples.*;
import com.ringcentral.platform.metrics.spring.MfMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(MfMetricsExportAutoConfiguration.class)
@ConditionalOnProperty(prefix = MfTelegrafProperties.PREFIX, name = "enabled")
@EnableConfigurationProperties(MfTelegrafProperties.class)
public class MfTelegrafAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MfTelegrafConfigBuilder mfTelegrafConfigBuilder(MetricRegistry registry) {
        return new MfTelegrafConfigBuilder().instanceSamplesProvider(new DefaultInstanceSamplesProvider(registry));
    }

    @Bean
    @ConditionalOnMissingBean
    public TelegrafMetricsJsonExporterCustomizer telegrafMetricsJsonExporterCustomizer(MetricRegistry registry) {
        return e -> {};
    }

    @Bean
    @ConditionalOnMissingBean
    public TelegrafMetricsJsonExporter telegrafMetricsJsonExporter(
        MfTelegrafProperties properties,
        MfTelegrafConfigBuilder configBuilder,
        TelegrafMetricsJsonExporterCustomizer customizer,
        MetricRegistry registry) {

        configBuilder = new MfTelegrafConfigBuilder()
            .groupByType(properties.getGroupByType())
            .rebase(configBuilder);

        MfTelegrafConfig config = configBuilder.build();

        InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider =
            config.hasInstanceSamplesProvider() ?
            config.instanceSamplesProvider() :
            new DefaultInstanceSamplesProvider(registry);

        TelegrafMetricsJsonExporter exporter = new TelegrafMetricsJsonExporter(config.groupByType(), instanceSamplesProvider);
        customizer.customizeTelegrafMetricsJsonExporter(exporter);
        return exporter;
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAvailableEndpoint(endpoint = MfTelegrafEndpoint.class)
    public static class MfTelegrafEndpointConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public MfTelegrafEndpoint mfTelegrafEndpoint(TelegrafMetricsJsonExporter exporter) {
            return new MfTelegrafEndpoint(exporter);
        }
    }
}
