package com.ringcentral.platform.metrics.spring;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.infoProviders.ConcurrentMaskTreeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.infoProviders.PredicativeMetricNamedInfoProvider;
import com.ringcentral.platform.metrics.micrometer.MfMeterRegistry;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter.Rule;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter.RuleItem;
import com.ringcentral.platform.metrics.samples.DefaultInstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSampleSpecModsProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusSampleSpecModsProvider;
import com.ringcentral.platform.metrics.spring.jmx.JmxMetricsReporterCustomizer;
import com.ringcentral.platform.metrics.spring.jmx.MfJmxConfigBuilder;
import com.ringcentral.platform.metrics.spring.prometheus.MfPrometheusConfigBuilder;
import com.ringcentral.platform.metrics.spring.prometheus.PrometheusMetricsExporterCustomizer;
import com.ringcentral.platform.metrics.spring.telegraf.MfTelegrafConfigBuilder;
import com.ringcentral.platform.metrics.spring.telegraf.TelegrafMetricsJsonExporterCustomizer;
import com.ringcentral.platform.metrics.spring.zabbix.MfZabbixConfigBuilder;
import com.ringcentral.platform.metrics.spring.zabbix.ZabbixMetricsJsonExporterCustomizer;
import com.ringcentral.platform.metrics.spring.zabbix.lld.MfZabbixLldConfigBuilder;
import com.ringcentral.platform.metrics.spring.zabbix.lld.ZGroupMBeansExporterCustomizer;
import com.ringcentral.platform.metrics.spring.zabbix.lld.ZabbixLldMetricsReporterCustomizer;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.ringcentral.platform.metrics.MetricModBuilder.modifying;
import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;
import static com.ringcentral.platform.metrics.names.MetricNameMask.metricWithName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.nameMask;
import static com.ringcentral.platform.metrics.predicates.DefaultMetricInstancePredicate.forMetricInstancesMatching;
import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSamplesProviderBuilder.prometheusInstanceSamplesProvider;

@Configuration
public class MetricsFacadeConfig {

    // Defaults and overrides

    @Bean
    public MetricRegistryCustomizer mfDefaultsAndOverridesCustomizer() {
        return registry -> registry.postConfigure(
            metricWithName("counter"),
            modifying().counter(withCounter().description("Customized counter description")));
    }

    // JMX

    @Bean
    public MfJmxConfigBuilder mfJmxConfigBuilder() {
        return new MfJmxConfigBuilder().domainName("metrics");
    }

    @Bean
    public JmxMetricsReporterCustomizer jmxMetricsReporterCustomizer() {
        return reporter -> {
            // ...
        };
    }

    // Prometheus

    @Bean
    @ConditionalOnMissingBean
    public MfPrometheusConfigBuilder mfPrometheusConfigBuilder(
        MetricRegistry registry,
        PrometheusInstanceSampleSpecModsProvider instanceSampleSpecModsProvider,
        PrometheusSampleSpecModsProvider sampleSpecModsProvider) {

        // See PrometheusMetricsExporterSample for more details about building PrometheusInstanceSamplesProvider.
        PrometheusInstanceSamplesProvider instanceSamplesProvider = prometheusInstanceSamplesProvider(registry)
            .instanceSampleSpecModsProvider(instanceSampleSpecModsProvider)
            .sampleSpecModsProvider(sampleSpecModsProvider)
            .build();

        return new MfPrometheusConfigBuilder().instanceSamplesProvider(instanceSamplesProvider);
    }

    @Bean
    public PrometheusMetricsExporterCustomizer prometheusMetricsExporterCustomizer(MetricRegistry registry) {
        return exporter -> {
            // ...
        };
    }

    // Zabbix LLD

    @Bean
    public MfZabbixLldConfigBuilder mfZabbixLldConfigBuilder() {
        // See ZabbixReportersSample for more details about building PredicativeMetricNamedInfoProvider<Rule>.
        PredicativeMetricNamedInfoProvider<Rule> rulesProvider = new ConcurrentMaskTreeMetricNamedInfoProvider<>();
        return new MfZabbixLldConfigBuilder().rulesProvider(rulesProvider);
    }

    @Bean
    public ZGroupMBeansExporterCustomizer zGroupMBeansExporterCustomizer() {
        return exporter -> {
            exporter.ensureGroup("ensuredGroup_1");
            exporter.ensureGroup("ensuredGroup_2");
        };
    }

    public static final MetricDimension METHOD = new MetricDimension("method");
    public static final MetricDimension URI = new MetricDimension("uri");
    public static final MetricDimension STATUS = new MetricDimension("status");

    @Bean
    public ZabbixLldMetricsReporterCustomizer zabbixLldMetricsReporterCustomizer() {
        return reporter -> reporter.addRules(
            forMetricInstancesMatching(nameMask("http.server.requests")),
            new Rule(
                "httpRequest",
                List.of(
                    new RuleItem(METHOD, "method"),
                    new RuleItem(URI, "uri"),
                    new RuleItem(STATUS, "status"))));
    }

    // Zabbix

    @Bean
    public MfZabbixConfigBuilder mfZabbixConfigBuilder(MetricRegistry registry) {
        // See ZabbixReportersSample for more details about building DefaultInstanceSamplesProvider.
        DefaultInstanceSamplesProvider instanceSamplesProvider = new DefaultInstanceSamplesProvider(registry);
        return new MfZabbixConfigBuilder().instanceSamplesProvider(instanceSamplesProvider);
    }

    @Bean
    public ZabbixMetricsJsonExporterCustomizer zabbixMetricsJsonExporterCustomizer(MetricRegistry registry) {
        return exporter -> {
            // ...
        };
    }

    // Telegraf

    @Bean
    public MfTelegrafConfigBuilder mfTelegrafConfigBuilder(MetricRegistry registry) {
        // See TelegrafMetricsJsonExporterSample for more details about building DefaultInstanceSamplesProvider.
        DefaultInstanceSamplesProvider instanceSamplesProvider = new DefaultInstanceSamplesProvider(registry);
        return new MfTelegrafConfigBuilder().instanceSamplesProvider(instanceSamplesProvider);
    }

    @Bean
    public TelegrafMetricsJsonExporterCustomizer telegrafMetricsJsonExporterCustomizer(MetricRegistry registry) {
        return exporter -> {
            // ...
        };
    }

    // MfMeterRegistry

    @Bean
    MeterRegistryCustomizer<MfMeterRegistry> mfMeterRegistryCustomizer() {
        return registry -> {};
    }
}
