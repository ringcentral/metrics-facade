package com.ringcentral.platform.metrics.spring;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.micrometer.*;
import io.micrometer.core.instrument.Clock;
import org.springframework.boot.actuate.autoconfigure.metrics.*;
import org.springframework.boot.actuate.autoconfigure.metrics.export.ConditionalOnEnabledMetricsExport;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({ CompositeMeterRegistryAutoConfiguration.class, SimpleMetricsExportAutoConfiguration.class })
@AutoConfigureAfter(MetricsAutoConfiguration.class)
@ConditionalOnBean(Clock.class)
@ConditionalOnClass(MfMeterRegistry.class)
@ConditionalOnEnabledMetricsExport("mf")
@EnableConfigurationProperties(MfProperties.class)
public class MfMetricsExportAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MfMeterRegistryConfig mfMeterRegistryConfig(MfProperties properties) {
        return new MfPropertiesConfigAdapter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricRegistryMaker metricRegistryMaker() {
        return DefaultMetricRegistry::new;
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricRegistry mfMetricRegistry(
        MetricRegistryMaker maker,
        List<MetricRegistryCustomizer> customizers,
        List<MetricRegistryListener> listeners) {

        MetricRegistry registry = maker.makeMetricRegistry();

        if (customizers != null) {
            customizers.forEach(customizer -> customizer.customizeMetricRegistry(registry));
        }

        if (listeners != null) {
            listeners.forEach(registry::addListener);
        }

        return registry;
    }

    @Bean
    @ConditionalOnMissingBean
    public MfMeterRegistry mfMeterRegistry(
        MfMeterRegistryConfig config,
        MetricRegistry metricRegistry,
        Clock clock) {

        return new MfMeterRegistry(metricRegistry, clock);
    }
}
