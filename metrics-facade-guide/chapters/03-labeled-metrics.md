## Labeled metrics

_Labeled metric_ is a metric with which a set of attributes (labels) is associated, and which generates a separate "child" metric for each involved combination of values of these attributes (labels).

Assume you are developing a HTTP service that, in order to do its job, calls several other HTTP services.
Each of the external services is represented by several instances running on specific servers and ports.
The service being developed can call any of these instances
(for example, by using round-robin balancing).

Let us also assume that we would like to have a separate request execution `Counter`
for each instance of an external service (that is, for each combination of service, server, and port
for which at least one request has been made).

In other words, we would like to define a labeled `Counter` with the
labels service, server, port (taking into account the order of the labels).


[com.ringcentral.platform.metrics.guide.chapter03.Listing01](../examples/chapter-03/src/main/java/com/ringcentral/platform/metrics/guide/chapter03/Listing01.java)
```java
package com.ringcentral.platform.metrics.guide.chapter03;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;

public class Listing01 {

    public static void main(String[] args) throws InterruptedException {
        System.out.println(run());
    }

    public static String run() throws InterruptedException {
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

        // 4) Some action happens
        // auth-server-1:8080 is called once
        counter.inc(
                LabelValues.forLabelValues(
                        SERVICE.value("auth"),
                        SERVER.value("auth-server-1"),
                        PORT.value("8080")
                ));

        // auth-server-2:8080 is called 10 times
        for (int i = 0; i < 10; i++) {
            counter.inc(
                    LabelValues.forLabelValues(
                            SERVICE.value("auth"),
                            SERVER.value("auth-server-2"),
                            PORT.value("8080")
                    ));
        }

        // contact-server-1:8081 is called 3 times
        for (int i = 0; i < 3; i++) {
            counter.inc(
                    LabelValues.forLabelValues(
                            SERVICE.value("contacts"),
                            SERVER.value("contact-server-1"),
                            PORT.value("8081")
                    ));
        }

        // 5) Labeled metric instances are added asynchronously
        Thread.sleep(100);

        // 6) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 7) Export metrics
        var result = new StringBuilder();
        result.append(exporter.exportMetrics());
        return result.toString();
    }
}
```
Output:

```text
# HELP request_total Generated from metric instances with name request.total
# TYPE request_total gauge
request_total{service="auth",server="auth-server-1",port="8080",} 1.0
request_total{service="auth",server="auth-server-2",port="8080",} 10.0
request_total{service="contacts",server="contact-server-1",port="8081",} 3.0
```
As expected there is the `Metric` _request_total_ with different combinations of labels. Each combination, which has taken place, has its own counter. 

**TODO** there should be a link, where  `Labeled metric instances are added asynchronously` will be explained in details

### Labels' values order

If labels' values are passed in a wrong order, then there will be an exception.

[com.ringcentral.platform.metrics.guide.chapter03.Listing02](../examples/chapter-03/src/main/java/com/ringcentral/platform/metrics/guide/chapter03/Listing02.java)
```java
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
```
Output:

```text
Exception in thread "main" java.lang.IllegalArgumentException: labelValues = [LabelValue{label=Label{name='server'}, value='auth-server-1'}, LabelValue{label=Label{name='port'}, value='8080'}, LabelValue{label=Label{name='service'}, value='auth'}] do not match labels = [Label{name='service'}, Label{name='server'}, Label{name='port'}]
	at com.ringcentral.platform.metrics.AbstractMeter.unexpected(AbstractMeter.java:419)
	at com.ringcentral.platform.metrics.AbstractMeter.checkLabelValues(AbstractMeter.java:408)
	at com.ringcentral.platform.metrics.AbstractMeter.update(AbstractMeter.java:379)
	at com.ringcentral.platform.metrics.counter.AbstractCounter.inc(AbstractCounter.java:42)
	at com.ringcentral.platform.metrics.counter.Counter.inc(Counter.java:38)
	at com.ringcentral.platform.metrics.guide.Lesson_3_WrongOrderOfLabels.main(Lesson_3_WrongOrderOfLabels.java:31)
```

Exception's message tells us that order is wrong and provides information about the correct one.

### Metrics eviction

#### Maximum number of combinations

The maximum number of combinations of label values - when this threshold is exceeded, the combination that has not been updated for the longest time,
will be automatically removed (will not waste system resources)

[com.ringcentral.platform.metrics.guide.chapter03.Listing03](../examples/chapter-03/src/main/java/com/ringcentral/platform/metrics/guide/chapter03/Listing03.java)
```java
package com.ringcentral.platform.metrics.guide.chapter03;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;

public class Listing03 {

    public static void main(String[] args) throws InterruptedException {
        System.out.println(run());
    }

    public static String run() throws InterruptedException {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Create labels
        Label SERVICE = new Label("service");
        Label SERVER = new Label("server");
        Label PORT = new Label("port");

        // 3) Register metric
        MetricName name = MetricName.of("request", "total");
        Counter counter = registry.counter(name,
                () -> withCounter()
                        .labels(SERVICE, SERVER, PORT)
                        .maxLabeledInstancesPerSlice(3)
        );

        // 4) Some action happens
        for (int i = 1; i < 6; i++) {
            counter.inc(
                    LabelValues.forLabelValues(
                            SERVICE.value("auth"),
                            SERVER.value("auth-server-" + i),
                            PORT.value("8080")
                    ));
        }

        // 5) Labeled metric instances are added asynchronously
        Thread.sleep(100);

        // 6) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 7) Export metrics
        var result = new StringBuilder();
        result.append(exporter.exportMetrics());
        return result.toString();
    }
}
```

```text
# HELP request_total Generated from metric instances with name request.total
# TYPE request_total gauge
request_total{service="auth",server="auth-server-5",port="8080",} 1.0
request_total{service="auth",server="auth-server-4",port="8080",} 1.0
request_total{service="auth",server="auth-server-3",port="8080",} 1.0
```

As expected there are only 3 instances of `Metric` _request_total_.


#### Expiration time
The expiration time for a combination of label values - 
if a combination has not been updated during this time, it will be automatically removed.

[com.ringcentral.platform.metrics.guide.chapter03.Listing04](../examples/chapter-03/src/main/java/com/ringcentral/platform/metrics/guide/chapter03/Listing04.java)

```java
package com.ringcentral.platform.metrics.guide.chapter03;

import com.ringcentral.platform.metrics.counter.Counter;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.reporters.prometheus.PrometheusMetricsExporter;

import java.time.Duration;

import static com.ringcentral.platform.metrics.counter.configs.builders.CounterConfigBuilder.withCounter;

public class Listing04 {

    public static void main(String[] args) throws InterruptedException {
        System.out.println(run());
    }

    public static String run() throws InterruptedException {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Create labels
        var SERVICE = new Label("service");
        var SERVER = new Label("server");
        var PORT = new Label("port");

        // 3) Register metric
        var expirationTime = Duration.ofMillis(500);
        MetricName name = MetricName.of("request", "total");
        Counter counter = registry.counter(name,
                () -> withCounter()
                        .labels(SERVICE, SERVER, PORT)
                        .expireLabeledInstanceAfter(expirationTime)
        );

        // 4) Some action happens
        for (int i = 1; i < 6; i++) {
            counter.inc(
                    LabelValues.forLabelValues(
                            SERVICE.value("auth"),
                            SERVER.value("auth-server-" + i),
                            PORT.value("8080")
                    ));
        }

        // 5) Labeled metric instances are added asynchronously
        Thread.sleep(expirationTime.dividedBy(2).toMillis());

        // 6) Create exporter
        var exporter = new PrometheusMetricsExporter(registry);

        // 7) Export metrics
        var result = new StringBuilder();
        result.append("Before expiration time\n")
                .append(exporter.exportMetrics());

        // 8) Export metrics after expiration time passed
        Thread.sleep(expirationTime.multipliedBy(2).toMillis());
        result.append("After expiration time\n")
                .append(exporter.exportMetrics());

        return result.toString();
    }
}
```

Output:

```text
Before expiration time
HELP request_total Generated from metric instances with name request.total
TYPE request_total gauge
request_total{service="auth",server="auth-server-5",port="8080",} 1.0
request_total{service="auth",server="auth-server-1",port="8080",} 1.0
request_total{service="auth",server="auth-server-4",port="8080",} 1.0
request_total{service="auth",server="auth-server-2",port="8080",} 1.0
request_total{service="auth",server="auth-server-3",port="8080",} 1.0
After expiration time
```

As expected there is no the `Metric` _request_total_ after expiration time.