package com.ringcentral.platform.metrics.spring;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.infoProviders.*;
import com.ringcentral.platform.metrics.micrometer.MfMeterRegistry;
import com.ringcentral.platform.metrics.reporters.zabbix.ZabbixLldMetricsReporter.*;
import com.ringcentral.platform.metrics.samples.DefaultInstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSamplesProvider;
import com.ringcentral.platform.metrics.spring.jmx.*;
import com.ringcentral.platform.metrics.spring.prometheus.*;
import com.ringcentral.platform.metrics.spring.telegraf.*;
import com.ringcentral.platform.metrics.spring.zabbix.*;
import com.ringcentral.platform.metrics.spring.zabbix.lld.*;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.*;

import java.util.List;

import static com.ringcentral.platform.metrics.names.MetricNameMask.nameMask;
import static com.ringcentral.platform.metrics.predicates.DefaultMetricInstancePredicate.forMetricInstancesMatching;

@Configuration
public class MetricsFacadeConfig {

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
    public MfPrometheusConfigBuilder mfPrometheusConfigBuilder(MetricRegistry registry) {
        // See PrometheusMetricsExporterSample for more details about building PrometheusInstanceSamplesProvider.
        PrometheusInstanceSamplesProvider instanceSamplesProvider = new PrometheusInstanceSamplesProvider(registry);
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
