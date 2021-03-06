package com.ringcentral.platform.metrics.spring.prometheus;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.samples.InstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.*;
import com.ringcentral.platform.metrics.spring.MfMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import static com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter.DEFAULT_LOCALE;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(MfMetricsExportAutoConfiguration.class)
@ConditionalOnProperty(prefix = MfPrometheusProperties.PREFIX, name = "enabled")
@EnableConfigurationProperties(MfPrometheusProperties.class)
public class MfPrometheusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MfPrometheusConfigBuilder mfPrometheusConfigBuilder(MetricRegistry registry) {
        return new MfPrometheusConfigBuilder().instanceSamplesProvider(new PrometheusInstanceSamplesProvider(registry));
    }

    @Bean
    @ConditionalOnMissingBean
    public PrometheusMetricsExporterCustomizer prometheusMetricsExporterCustomizer(MetricRegistry registry) {
        return e -> {};
    }

    @Bean
    @ConditionalOnMissingBean
    public PrometheusMetricsExporter prometheusMetricsExporter(
        MfPrometheusProperties properties,
        MfPrometheusConfigBuilder configBuilder,
        PrometheusMetricsExporterCustomizer customizer,
        MetricRegistry registry) {

        configBuilder = new MfPrometheusConfigBuilder()
            .convertNameToLowercase(properties.getConvertNameToLowercase())
            .locale(properties.getLocale())
            .rebase(configBuilder);

        MfPrometheusConfig config = configBuilder.build();

        InstanceSamplesProvider<? extends PrometheusSample, ? extends PrometheusInstanceSample> instanceSamplesProvider =
            config.hasInstanceSamplesProvider() ?
            config.instanceSamplesProvider() :
            new PrometheusInstanceSamplesProvider(registry);

        PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(
            config.convertNameToLowercase(),
            config.hasLocale() ? config.locale() : DEFAULT_LOCALE,
            instanceSamplesProvider);

        customizer.customizePrometheusMetricsExporter(exporter);
        return exporter;
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnAvailableEndpoint(endpoint = MfPrometheusEndpoint.class)
    public static class MfPrometheusEndpointConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public MfPrometheusEndpoint mfPrometheusEndpoint(PrometheusMetricsExporter exporter) {
            return new MfPrometheusEndpoint(exporter);
        }
    }
}
