package com.ringcentral.platform.metrics.reporters.telegraf;

import com.fasterxml.jackson.annotation.*;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.reporters.*;
import com.ringcentral.platform.metrics.samples.*;

import java.util.*;

public class TelegrafMetricsJsonExporter implements MetricsJsonExporter {

    public static final boolean DEFAULT_GROUP_BY_TYPE = false;

    private final boolean groupByType;
    private final InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider;

    public TelegrafMetricsJsonExporter(
        boolean groupByType,
        MetricRegistry registry) {

        this(groupByType, new DefaultInstanceSamplesProvider(registry));
    }

    public TelegrafMetricsJsonExporter(
        boolean groupByType,
        InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider) {

        this.groupByType = groupByType;
        this.instanceSamplesProvider = instanceSamplesProvider;
    }

    @SafeVarargs
    public TelegrafMetricsJsonExporter(
        boolean groupByType,
        InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>>... instanceSamplesProviders) {

        this.groupByType = groupByType;
        this.instanceSamplesProvider = new CompositeInstanceSamplesProvider<>(List.of(instanceSamplesProviders));
    }

    @Override
    public MetricsJson exportMetrics() {
        ModifiableTelegrafMetricsJson metricsJson =
            groupByType ?
            new GroupedTelegrafMetricsJson() :
            new UngroupedTelegrafMetricsJson();

        instanceSamplesProvider.instanceSamples().forEach(is ->
            is.samples().forEach(s -> metricsJson.putMetricJson(s.type(), new TelegrafMetricJson(s.name(), s.value()))));

        return metricsJson;
    }

    public interface ModifiableTelegrafMetricsJson extends MetricsJson {
        void putMetricJson(String type, TelegrafMetricJson metricJson);
    }

    public static class GroupedTelegrafMetricsJson implements ModifiableTelegrafMetricsJson {

        private final Map<String, Map<String, Object>> metricJsons = new LinkedHashMap<>();

        @JsonAnySetter
        public void putMetricJson(String type, TelegrafMetricJson metricJson) {
            metricJsons.computeIfAbsent(type, t -> new LinkedHashMap<>()).put(metricJson.name, metricJson.value);
        }

        @JsonAnyGetter
        public Map<String, Map<String, Object>> metricJsons() {
            return metricJsons;
        }
    }

    public static class UngroupedTelegrafMetricsJson implements ModifiableTelegrafMetricsJson {

        private final Map<String, Object> metricJsons = new LinkedHashMap<>();

        @JsonAnySetter
        public void putMetricJson(String type, TelegrafMetricJson metricJson) {
            metricJsons.put(metricJson.name, metricJson.value);
        }

        @JsonAnyGetter
        public Map<String, Object> metricJsons() {
            return metricJsons;
        }
    }

    public static class TelegrafMetricJson {

        private final String name;
        private final Object value;

        public TelegrafMetricJson(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }
}