package com.ringcentral.platform.metrics.spring.prometheus;

import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.samples.prometheus.collectorRegistry.SimpleCollectorRegistryPrometheusInstanceSamplesProvider;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import org.junit.Test;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusOutputFormat;

import static com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporterBuilder.prometheusMetricsExporter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class MfPrometheusEndpointTest {

    @Test
    public void shouldReturnMatchingTextContentTypeWhenProtobufIsSelected() {
        assertTextFallback(PrometheusOutputFormat.CONTENT_TYPE_PROTOBUF);
    }

    @Test
    public void shouldReturnMatchingTextContentTypeWhenFormatIsNull() {
        assertTextFallback(null);
    }

    @Test
    public void shouldKeepExplicitTextAndOpenMetricsFormats() {
        MfPrometheusEndpoint endpoint = new MfPrometheusEndpoint(exporterWithCounter());

        WebEndpointResponse<String> text = endpoint.export(PrometheusOutputFormat.CONTENT_TYPE_004);
        WebEndpointResponse<String> openMetrics = endpoint.export(PrometheusOutputFormat.CONTENT_TYPE_OPENMETRICS_100);

        assertEquals(PrometheusOutputFormat.CONTENT_TYPE_004.getProducedMimeType(), text.getContentType());
        assertTrue(text.getBody().contains("scrape_test_total 3.0"));
        assertFalse(text.getBody().contains("# EOF"));
        assertEquals(PrometheusOutputFormat.CONTENT_TYPE_OPENMETRICS_100.getProducedMimeType(), openMetrics.getContentType());
        assertTrue(openMetrics.getBody().contains("scrape_test_total 3.0"));
        assertTrue(openMetrics.getBody().endsWith("# EOF\n"));
    }

    @Test
    public void shouldPreserveExporterFailureAsCause() {
        PrometheusMetricsExporter exporter = mock(PrometheusMetricsExporter.class);
        IllegalStateException cause = new IllegalStateException("test failure");
        doThrow(cause).when(exporter).exportMetrics(any(PrometheusMetricsExporter.Format.class));

        try {
            new MfPrometheusEndpoint(exporter).export(PrometheusOutputFormat.CONTENT_TYPE_004);
            fail("Expected exporter failure");
        } catch (RuntimeException exception) {
            assertEquals("Failed to export metrics", exception.getMessage());
            assertEquals(cause, exception.getCause());
        }
    }

    private void assertTextFallback(PrometheusOutputFormat requestedFormat) {
        WebEndpointResponse<String> response = new MfPrometheusEndpoint(exporterWithCounter()).export(requestedFormat);

        assertEquals(PrometheusOutputFormat.CONTENT_TYPE_004.getProducedMimeType(), response.getContentType());
        assertTrue(response.getBody().contains("scrape_test_total 3.0"));
        assertFalse(response.getBody().contains("# EOF"));
    }

    private PrometheusMetricsExporter exporterWithCounter() {
        CollectorRegistry registry = new CollectorRegistry(true);
        Counter counter = Counter.build()
            .name("scrape_test")
            .help("Deterministic scrape test counter")
            .register(registry);
        counter.inc(3);

        return prometheusMetricsExporter()
            .addInstanceSamplesProvider(new SimpleCollectorRegistryPrometheusInstanceSamplesProvider(registry))
            .build();
    }
}
