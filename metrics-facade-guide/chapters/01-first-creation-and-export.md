## A first metric creation and export

There is the basic example of metric creation and its exporting.

[com.ringcentral.platform.metrics.guide.chapter01.Listing01](../examples/chapter-01/src/main/java/com/ringcentral/platform/metrics/guide/chapter01/Listing01.java)
```java 
package com.ringcentral.platform.metrics.guide;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

public class Example {
    
    public static void main(String[] args) throws Exception {
        System.out.println(run());
    }

    public static String run() {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Register metric
        MetricName name = MetricName.of("request", "total");
        Counter counter = registry.counter(name);

        // 3) Increase counter
        counter.inc();

        // 4) Create exporter
        PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);

        // 5) Export metrics
        StringBuilder result = new StringBuilder();
        result.append(exporter.exportMetrics());
        return result.toString();
    }
}
```

Output:
```text
# HELP request_total Generated from metric instances with name request.total
# TYPE request_total gauge
request_total 1.0
```

As you can see creation of the metric and its update is pretty simple.

In the next part we will learn about existing metric types and measurables - [Metric types and Measurables](02-metric-types.md).

Return to [Main page](../README.md).
