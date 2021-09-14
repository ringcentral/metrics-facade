package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.reporters.jmx.JmxMetricsReporter;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusHttpServer;

import java.io.IOException;

public abstract class AbstractSample {

    public static final MetricDimension SAMPLE = new MetricDimension("sample");
    public static final MetricDimension SERVICE = new MetricDimension("service");
    public static final MetricDimension SERVER = new MetricDimension("server");
    public static final MetricDimension PORT = new MetricDimension("port");

    public static final int PROMETHEUS_PORT = 9095;

    protected static void export(MetricRegistry registry) throws IOException {
        PrometheusMetricsExporter prometheusExporter = new PrometheusMetricsExporter(registry);
        new PrometheusHttpServer(PROMETHEUS_PORT, prometheusExporter);
        registry.addListener(new JmxMetricsReporter());
    }

    protected static void hang() throws InterruptedException {
        synchronized(AbstractSample.class) { AbstractSample.class.wait(); }
    }
}
