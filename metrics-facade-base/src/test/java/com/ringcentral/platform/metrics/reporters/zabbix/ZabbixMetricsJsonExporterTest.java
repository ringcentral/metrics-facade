package com.ringcentral.platform.metrics.reporters.zabbix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringcentral.platform.metrics.reporters.MetricsJson;
import com.ringcentral.platform.metrics.samples.*;
import org.junit.Test;

import java.util.*;

import static com.ringcentral.platform.metrics.samples.SampleTypes.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class ZabbixMetricsJsonExporterTest {

    ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void export_noInstanceSamples() throws Exception {
        InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider =
            mock(InstanceSamplesProvider.class);

        when(instanceSamplesProvider.instanceSamples()).thenReturn(Set.of());

        ZabbixMetricsJsonExporter exporter = new ZabbixMetricsJsonExporter(instanceSamplesProvider);
        MetricsJson metricsJson = exporter.exportMetrics();

        assertTrue(jsonEqual(jsonMapper.writeValueAsString(metricsJson), "{}"));
    }

    @Test
    public void export() throws Exception {
        InstanceSamplesProvider<DefaultSample, DefaultInstanceSample> instanceSamplesProvider =
            mock(InstanceSamplesProvider.class);

        DefaultInstanceSample instanceSample_1 = new DefaultInstanceSample();
        instanceSample_1.add(new DefaultSample("a", 1, INSTANT));
        instanceSample_1.add(new DefaultSample("a.b", 2.0, INSTANT));
        instanceSample_1.add(new DefaultSample("a.b.c", "3", INSTANT));

        DefaultInstanceSample instanceSample_2 = new DefaultInstanceSample();

        DefaultInstanceSample instanceSample_3 = new DefaultInstanceSample();
        instanceSample_3.add(new DefaultSample("d", 4, INSTANT));
        instanceSample_3.add(new DefaultSample("d.e", 5.0, INSTANT));
        instanceSample_3.add(new DefaultSample("d.e.f", "6", INSTANT));
        instanceSample_3.add(new DefaultSample("d.e.f.g", 7, DELTA));
        instanceSample_3.add(new DefaultSample("d.e.f.g.h", 8, DELTA));

        Set<DefaultInstanceSample> es = Set.of(instanceSample_1, instanceSample_2, instanceSample_3);
        when(instanceSamplesProvider.instanceSamples()).thenReturn(es);

        ZabbixMetricsJsonExporter exporter = new ZabbixMetricsJsonExporter(instanceSamplesProvider);
        MetricsJson metricsJson = exporter.exportMetrics();

        assertTrue(jsonEqual(jsonMapper.writeValueAsString(metricsJson),
        "{\n" +
            "  \"instant\": [\n" +
            "    {\n" +
            "      \"a\": 1\n" +
            "    },\n" +
            "    {\n" +
            "      \"a.b\": 2.0\n" +
            "    },\n" +
            "    {\n" +
            "      \"a.b.c\": \"3\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"d\": 4\n" +
            "    },\n" +
            "    {\n" +
            "      \"d.e\": 5.0\n" +
            "    },\n" +
            "    {\n" +
            "      \"d.e.f\": \"6\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"delta\": [\n" +
            "    {\n" +
            "      \"d.e.f.g\": 7\n" +
            "    },\n" +
            "    {\n" +
            "      \"d.e.f.g.h\": 8\n" +
            "    }\n" +
            "  ]\n" +
            "}"));
    }

    public boolean jsonEqual(String a, String b) {
        try {
            return Objects.equals(jsonMapper.readTree(a), jsonMapper.readTree(b));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}