package com.ringcentral.platform.metrics.reporters.zabbix;

import com.fasterxml.jackson.annotation.*;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.reporters.*;
import com.ringcentral.platform.metrics.samples.*;

import java.util.*;

import static java.util.Comparator.comparing;

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
        ZMetricsJson metricsJson = new ZMetricsJson();

        instanceSamplesProvider.instanceSamples().forEach(is ->
            is.samples().forEach(s -> metricsJson.putMetricJson(s.type(), new ZMetricJson(s.name(), s.value()))));

        metricsJson.metricJsons.forEach((key, val) -> val.sort(comparing(ZMetricJson::name)));

        return metricsJson;
    }

    public static class ZMetricsJson implements MetricsJson {

        private final Map<String, ArrayList<ZMetricJson>> metricJsons = new LinkedHashMap<>();

        @JsonAnySetter
        public void putMetricJson(String type, ZMetricJson metricJson) {
            metricJsons.computeIfAbsent(type, t -> new ArrayList<>()).add(metricJson);
        }

        @JsonAnyGetter
        public Map<String, ArrayList<ZMetricJson>> metricJsons() {
            return metricJsons;
        }
    }

    public static class ZMetricJson {

        private final Map<String, Object> props = new LinkedHashMap<>();

        public ZMetricJson(String name, Object value) {
            props.put(name, value);
        }

        public String name() {
            return props.keySet().iterator().next();
        }

        @JsonAnySetter
        public void putProperty(String name, Object property) {
            props.put(name, property);
        }

        @JsonAnyGetter
        public Map<String, Object> properties() {
            return props;
        }
    }
}