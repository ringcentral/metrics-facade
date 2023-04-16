package com.ringcentral.platform.metrics.guide.chapter03;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;

import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;

public class Listing05 {

    public static void main(String[] args) {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Create labels
        var serviceLabel = new Label("service");
        var serverLabel = new Label("server");
        var portLabel = new Label("port");

        // 3) Register metric
        MetricName name = MetricName.of("request", "total");
        Counter counter = registry.counter(name,
                () -> withCounter()
                        .labels(serviceLabel, serverLabel, portLabel));

        // 4) Wrong order of labels' values.
        // actual: SERVER, PORT, SERVICE
        // expected: SERVICE, SERVER, PORT
        counter.inc(
                LabelValues.forLabelValues(
                        serverLabel.value("auth-server-1"),
                        portLabel.value("8080"),
                        serviceLabel.value("auth")
                ));
    }
}
