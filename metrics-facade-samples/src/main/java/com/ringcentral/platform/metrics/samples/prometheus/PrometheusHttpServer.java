package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ringcentral.platform.metrics.samples.prometheus.PrometheusHttpServer.NamedDaemonThreadFactory.defaultThreadFactory;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;

public class PrometheusHttpServer {

    private static class LocalByteArrayOutputStream extends ThreadLocal<ByteArrayOutputStream> {

        @Override
        protected ByteArrayOutputStream initialValue() {
            return new ByteArrayOutputStream(1 << 20);
        }
    }

    static class MetricsHandler implements HttpHandler {

        static final String CONTENT_LENGTH = "Content-Length";

        private final PrometheusMetricsExporter exporter;
        private final LocalByteArrayOutputStream outputStream = new LocalByteArrayOutputStream();

        MetricsHandler(PrometheusMetricsExporter exporter) {
            this.exporter = exporter;
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            ByteArrayOutputStream os = outputStream.get();
            os.reset();
            OutputStreamWriter writer = new OutputStreamWriter(os, UTF_8);
            exporter.exportMetrics(writer);
            writer.close();
            httpExchange.getResponseHeaders().set(CONTENT_LENGTH, String.valueOf(os.size()));
            httpExchange.sendResponseHeaders(HTTP_OK, os.size());
            os.writeTo(httpExchange.getResponseBody());
            httpExchange.close();
        }
    }

    static class NamedDaemonThreadFactory implements ThreadFactory {

        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);

        private final int poolNumber = POOL_NUMBER.getAndIncrement();
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadFactory delegate;
        private final boolean daemon;

        NamedDaemonThreadFactory(ThreadFactory delegate, boolean daemon) {
            this.delegate = delegate;
            this.daemon = daemon;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = delegate.newThread(r);
            t.setName(String.format("prometheus-http-%d-%d", poolNumber, threadNumber.getAndIncrement()));
            t.setDaemon(daemon);
            return t;
        }

        static ThreadFactory defaultThreadFactory(boolean daemon) {
            return new NamedDaemonThreadFactory(Executors.defaultThreadFactory(), daemon);
        }
    }

    protected final HttpServer server;
    protected final ExecutorService executor;

    public PrometheusHttpServer(HttpServer httpServer, boolean daemon, PrometheusMetricsExporter exporter) {
        if (httpServer.getAddress() == null) {
            throw new IllegalArgumentException("HttpServer hasn't been bound to an address");
        }

        server = httpServer;
        HttpHandler metricsHandler = new MetricsHandler(exporter);
        server.createContext("/metrics", metricsHandler);
        executor = Executors.newFixedThreadPool(5, defaultThreadFactory(daemon));
        server.setExecutor(executor);
        start(daemon);
    }

    public PrometheusHttpServer(InetSocketAddress addr, boolean daemon, PrometheusMetricsExporter exporter) throws IOException {
        this(HttpServer.create(addr, 3), daemon, exporter);
    }

    public PrometheusHttpServer(InetSocketAddress addr, PrometheusMetricsExporter exporter) throws IOException {
        this(addr, false, exporter);
    }

    public PrometheusHttpServer(int port, boolean daemon, PrometheusMetricsExporter exporter) throws IOException {
        this(new InetSocketAddress(port), daemon, exporter);
    }

    public PrometheusHttpServer(int port, PrometheusMetricsExporter exporter) throws IOException {
        this(port, false, exporter);
    }

    public PrometheusHttpServer(String host, int port, boolean daemon, PrometheusMetricsExporter exporter) throws IOException {
        this(new InetSocketAddress(host, port), daemon, exporter);
    }

    public PrometheusHttpServer(String host, int port, PrometheusMetricsExporter exporter) throws IOException {
        this(new InetSocketAddress(host, port), false, exporter);
    }

    public int port() {
        return server.getAddress().getPort();
    }

    private void start(boolean daemon) {
        if (daemon == Thread.currentThread().isDaemon()) {
            server.start();
        } else {
            FutureTask<Void> startTask = new FutureTask<>(server::start, null);
            defaultThreadFactory(daemon).newThread(startTask).start();

            try {
                startTask.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                // This is possible only if the current thread has been interrupted,
                // but in real use cases this should not happen.
                // In any case, there is nothing to do, except to propagate interrupted flag.
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop() {
        server.stop(0);
        // Free any (parked/idle) threads in pool.
        executor.shutdown();
    }
}