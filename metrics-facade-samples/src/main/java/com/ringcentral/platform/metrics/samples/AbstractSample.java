package com.ringcentral.platform.metrics.samples;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.reporters.jmx.JmxMetricsReporter;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.ringcentral.platform.metrics.samples.prometheus.PrometheusHttpServer;

import java.io.IOException;

public abstract class AbstractSample {

    public static final Label SAMPLE = new Label("sample");
    public static final Label SERVICE = new Label("service");
    public static final Label SERVER = new Label("server");
    public static final Label PORT = new Label("port");

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
