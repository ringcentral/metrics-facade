package com.ringcentral.platform.metrics.spring.prometheus;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.infoProviders.ConcurrentMaskTreeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.samples.InstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.*;
import com.ringcentral.platform.metrics.spring.MfMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporterBuilder.prometheusMetricsExporterBuilder;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSamplesProviderBuilder.prometheusInstanceSamplesProvider;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(MfMetricsExportAutoConfiguration.class)
@ConditionalOnProperty(prefix = MfPrometheusProperties.PREFIX, name = "enabled")
@EnableConfigurationProperties(MfPrometheusProperties.class)
public class MfPrometheusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PrometheusInstanceSampleSpecModsProvider prometheusInstanceSampleSpecModsProvider() {
        return new PrometheusInstanceSampleSpecModsProvider(new ConcurrentMaskTreeMetricNamedInfoProvider<>());
    }

    @Bean
    @ConditionalOnMissingBean
    public PrometheusSampleSpecModsProvider prometheusSampleSpecModsProvider() {
        return new PrometheusSampleSpecModsProvider(new ConcurrentMaskTreeMetricNamedInfoProvider<>());
    }

    @Bean
    @ConditionalOnMissingBean
    public MfPrometheusConfigBuilder mfPrometheusConfigBuilder(
        MetricRegistry registry,
        PrometheusInstanceSampleSpecModsProvider instanceSampleSpecModsProvider,
        PrometheusSampleSpecModsProvider sampleSpecModsProvider) {

        PrometheusInstanceSamplesProvider instanceSamplesProvider = prometheusInstanceSamplesProvider(registry)
            .instanceSampleSpecModsProvider(instanceSampleSpecModsProvider)
            .sampleSpecModsProvider(sampleSpecModsProvider)
            .build();

        return new MfPrometheusConfigBuilder().instanceSamplesProvider(instanceSamplesProvider);
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

        PrometheusMetricsExporter exporter = prometheusMetricsExporterBuilder()
            .convertNameToLowercase(config.convertNameToLowercase())
            .locale(config.locale())
            .addInstanceSamplesProvider(instanceSamplesProvider)
            .build();

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
