package com.ringcentral.platform.metrics.spring.jmx;

import com.ringcentral.platform.metrics.measurables.DefaultMeasurableNameProvider;
import com.ringcentral.platform.metrics.reporters.jmx.*;
import com.ringcentral.platform.metrics.spring.MfMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import static java.lang.management.ManagementFactory.getPlatformMBeanServer;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(MfMetricsExportAutoConfiguration.class)
@ConditionalOnProperty(prefix = MfJmxProperties.PREFIX, name = "enabled")
@EnableConfigurationProperties(MfJmxProperties.class)
public class MfJmxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MfJmxConfigBuilder mfJmxConfigBuilder() {
        return new MfJmxConfigBuilder();
    }

    @Bean
    @ConditionalOnMissingBean
    public JmxMetricsReporterCustomizer jmxMetricsReporterCustomizer() {
        return r -> {};
    }

    @Bean
    @ConditionalOnMissingBean
    public JmxMetricsReporter jmxMetricsReporter(
        MfJmxProperties properties,
        MfJmxConfigBuilder configBuilder,
        JmxMetricsReporterCustomizer customizer) {

        configBuilder = new MfJmxConfigBuilder()
            .domainName(properties.getDomainName())
            .rebase(configBuilder);

        MfJmxConfig config = configBuilder.build();

        JmxMetricsReporter reporter = new JmxMetricsReporter(
            config.mBeanSpecModProviders(),
            getPlatformMBeanServer(),
            config.hasObjectNameProvider() ? config.objectNameProvider() : DefaultObjectNameProvider.INSTANCE,
            config.hasMeasurableNameProvider() ? config.measurableNameProvider() : DefaultMeasurableNameProvider.INSTANCE,
            config.hasDomainName() ? config.domainName() : JmxMetricsReporter.DEFAULT_DOMAIN_NAME);

        customizer.customizeJmxMetricsReporter(reporter);
        return reporter;
    }
}
