package com.ringcentral.platform.metrics.reporters.telegraf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ringcentral.platform.metrics.reporters.MetricsJson;
import com.ringcentral.platform.metrics.samples.*;
import org.junit.Test;

import java.util.*;

import static com.ringcentral.platform.metrics.samples.SampleTypes.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class TelegrafMetricsJsonExporterTest {

    ObjectMapper jsonMapper = new ObjectMapper();

    @Test
    public void export_groupByType_noInstanceSamples() throws Exception {
        InstanceSamplesProvider<? extends DefaultSample, ? extends InstanceSample<DefaultSample>> instanceSamplesProvider =
            mock(InstanceSamplesProvider.class);

        when(instanceSamplesProvider.instanceSamples()).thenReturn(Set.of());

        TelegrafMetricsJsonExporter exporter = new TelegrafMetricsJsonExporter(true, instanceSamplesProvider);
        MetricsJson metricsJson = exporter.exportMetrics();

        assertTrue(jsonEqual(jsonMapper.writeValueAsString(metricsJson), "{}"));
    }

    @Test
    public void export_groupByType() throws Exception {
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

        TelegrafMetricsJsonExporter exporter = new TelegrafMetricsJsonExporter(true, instanceSamplesProvider);
        MetricsJson metricsJson = exporter.exportMetrics();

        assertTrue(jsonEqual(jsonMapper.writeValueAsString(metricsJson),
            "{\n" +
            "  \"instant\": {\n" +
            "    \"d\": 4,\n" +
            "    \"d.e\": 5.0,\n" +
            "    \"d.e.f\": \"6\",\n" +
            "    \"a\": 1,\n" +
            "    \"a.b\": 2.0,\n" +
            "    \"a.b.c\": \"3\"\n" +
            "  },\n" +
            "  \"delta\": {\n" +
            "    \"d.e.f.g\": 7,\n" +
            "    \"d.e.f.g.h\": 8\n" +
            "  }\n" +
            "}"));
    }

    @Test
    public void export_notGroupByType() throws Exception {
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

        TelegrafMetricsJsonExporter exporter = new TelegrafMetricsJsonExporter(false, instanceSamplesProvider);
        MetricsJson metricsJson = exporter.exportMetrics();

        assertTrue(jsonEqual(jsonMapper.writeValueAsString(metricsJson),
         "{\n" +
            "  \"d\": 4,\n" +
            "  \"d.e\": 5.0,\n" +
            "  \"d.e.f\": \"6\",\n" +
            "  \"d.e.f.g\": 7,\n" +
            "  \"d.e.f.g.h\": 8,\n" +
            "  \"a\": 1,\n" +
            "  \"a.b\": 2.0,\n" +
            "  \"a.b.c\": \"3\"\n" +
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