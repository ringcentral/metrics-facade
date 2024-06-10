package com.ringcentral.platform.metrics.reporters.telegraf;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.reporters.MetricsJson;
import com.ringcentral.platform.metrics.reporters.MetricsJsonExporter;
import com.ringcentral.platform.metrics.samples.*;

import java.util.*;

public class TelegrafMetricsJsonExporter implements MetricsJsonExporter {

    public static final boolean DEFAULT_GROUP_BY_TYPE = false;

    private final boolean groupByType;
    private final InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider;

    public TelegrafMetricsJsonExporter(boolean groupByType, MetricRegistry registry) {
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

        instanceSamplesProvider.instanceSamples().forEach(is -> is.samples().forEach(s -> metricsJson.putSample(s.type(), s.name(), s.value())));
        return metricsJson;
    }

    public interface ModifiableTelegrafMetricsJson extends MetricsJson {
        void putSample(String type, String name, Object value);
    }

    public static class GroupedTelegrafMetricsJson extends LinkedHashMap<String, Map<String, Object>> implements ModifiableTelegrafMetricsJson {

        public void putSample(String type, String name, Object value) {
            computeIfAbsent(type, t -> new LinkedHashMap<>()).put(name, value);
        }
    }

    public static class UngroupedTelegrafMetricsJson extends LinkedHashMap<String, Object> implements ModifiableTelegrafMetricsJson {

        public void putSample(String type, String name, Object value) {
            put(name, value);
        }
    }
}