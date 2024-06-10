package com.ringcentral.platform.metrics.reporters.zabbix;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.reporters.MetricsJson;
import com.ringcentral.platform.metrics.reporters.MetricsJsonExporter;
import com.ringcentral.platform.metrics.samples.*;

import java.util.*;

public class ZabbixMetricsJsonExporter implements MetricsJsonExporter {

    private final InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider;

    public ZabbixMetricsJsonExporter(MetricRegistry registry) {
        this(new DefaultInstanceSamplesProvider(registry));
    }

    public ZabbixMetricsJsonExporter(InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider) {
        this.instanceSamplesProvider = instanceSamplesProvider;
    }

    @SafeVarargs
    public ZabbixMetricsJsonExporter(InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>>... instanceSamplesProviders) {
        this.instanceSamplesProvider = new CompositeInstanceSamplesProvider<>(List.of(instanceSamplesProviders));
    }

    @Override
    public MetricsJson exportMetrics() {
        ZabbixMetricsJson metricsJson = new ZabbixMetricsJson();
        instanceSamplesProvider.instanceSamples().forEach(is -> is.samples().forEach(s -> metricsJson.putSample(s.type(), s.name(), s.value())));
        metricsJson.forEach((key, val) -> val.sort(Map.Entry.comparingByKey()));
        return metricsJson;
    }

    public static class ZabbixMetricsJson extends LinkedHashMap<String, ArrayList<Map.Entry<String, Object>>> implements MetricsJson {

        public void putSample(String type, String name, Object value) {
            computeIfAbsent(type, t -> new ArrayList<>()).add(Map.entry(name, value));
        }
    }
}