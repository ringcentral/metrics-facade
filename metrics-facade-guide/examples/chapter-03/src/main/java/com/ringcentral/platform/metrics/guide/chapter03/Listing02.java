package com.ringcentral.platform.metrics.guide.chapter03;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;

import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;

public class Listing02 {

    public static void main(String[] args) {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Create labels
        var SERVICE = new Label("service");
        var SERVER = new Label("server");
        var PORT = new Label("port");

        // 3) Register metric
        MetricName name = MetricName.of("request", "total");
        Counter counter = registry.counter(name,
                () -> withCounter()
                        .labels(SERVICE, SERVER, PORT));

        // 4) Wrong order of labels' values.
        // actual: SERVER, PORT, SERVICE
        // expected: SERVICE, SERVER, PORT
        counter.inc(
                LabelValues.forLabelValues(
                        SERVER.value("auth-server-1"),
                        PORT.value("8080"),
                        SERVICE.value("auth")
                ));
    }
}
