package com.ringcentral.platform.metrics.samples.prometheus.collectorRegistry;


import com.codahale.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.samples.InstanceSamplesProvider;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusInstanceSample;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusSample;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;

import java.util.Set;

public class DropwizardMetricRegistryPrometheusInstanceSamplesProvider implements InstanceSamplesProvider<
        PrometheusSample,
        PrometheusInstanceSample> {

    private final InstanceSamplesProvider<PrometheusSample, PrometheusInstanceSample> provider;

    public DropwizardMetricRegistryPrometheusInstanceSamplesProvider(MetricRegistry registry) {
        this(null, registry);
    }

    public DropwizardMetricRegistryPrometheusInstanceSamplesProvider(MetricName namePrefix, MetricRegistry registry) {
        final var collectorRegistry = new CollectorRegistry();
        collectorRegistry.register(new DropwizardExports(registry));
        provider = new SimpleCollectorRegistryPrometheusInstanceSamplesProvider(namePrefix, collectorRegistry);
    }

    @Override
    public Set<PrometheusInstanceSample> instanceSamples() {
        return provider.instanceSamples();
    }
}
