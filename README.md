# RingCentral Metrics Facade

***RingCentral Metrics Facade*** *is a Java library for working with metrics,        
allowing extremely flexible configuration of metrics and their export,         
designed to be generic and not tied to a specific implementation.*      

Table of Contents
=================
* [Main Features Overview](#main-features-overview)
* [Getting Started](#getting-started)
* [Features](#features)
  * [Flexible Configuration](#flexible-configuration)
  * [Labeled metrics](#labeled-metrics)
  * [Prefix Label Values](#prefix-label-values)
  * [Exclusions](#exclusions)
  * [Slices and Levels](#slices-and-levels)
* [Metrics](#metrics)
  * [Counter](#counter)
    * [Counter Config](#counter-config)
  * [Rate](#rate)
    * [Rate Config](#rate-config)
  * [Histogram](#histogram)
    * [Histogram Config](#histogram-config)
  * [Timer](#timer)
    * [Timer Config](#timer-config)
  * [Var and CachingVar](#var-and-cachingvar)
* [Metrics Reporters](#metrics-reporters)
  * [PrometheusMetricsExporter](#prometheusmetricsexporter)
  * [ZabbixMetricsJsonExporter and ZabbixLldMetricsReporter](#zabbixmetricsjsonexporter-and-zabbixlldmetricsreporter)
  * [TelegrafMetricsJsonExporter](#telegrafmetricsjsonexporter)
  * [JmxMetricsReporter](#jmxmetricsreporter)
* [Metrics Producers](#metrics-producers)
  * [SystemMetricsProducer](#systemmetricsproducer)
  * [OperatingSystemMetricsProducer](#operatingsystemmetricsproducer)
  * [GarbageCollectorsMetricsProducer](#garbagecollectorsmetricsproducer)
  * [MemoryMetricsProducer](#memorymetricsproducer)
  * [ThreadsMetricsProducer](#threadsmetricsproducer)
  * [BufferPoolsMetricsProducer](#bufferpoolsmetricsproducer)
* [Integration](#integration)
  * [Dropwizard MetricRegistry](#dropwizard-MetricRegistry-integration)
* [License](#license)  

## Main Features Overview

### Extremely flexible configuration

You can set up defaults and overrides for ***any*** subset of metrics  
(by using a predicate) and ***almost any*** metric parameters.                  
In particular, you can specify a set of values to be ***calculated*** and ***exported***.            
For example, the implementation of a metric is not required to perform an expensive operation of        
calculating percentiles, if percentiles are not included in the set of values defined for this metric.           
See [Getting Started](#getting-started) for details and usage examples.        

### Advanced support for labeled metrics

In particular, Metrics Facade supports eviction and expiration for labeled metrics,        
that is, for each labeled metric, it allows you to specify:        
- the maximum number of combinations of label values:        
  when this threshold is exceeded, the combination that has not been updated for the longest time,        
  will be automatically removed (will not waste system resources)          
- the expiration time for a combination of label values:  
  if a combination has not been updated during this time, it will be automatically removed    

See [Getting Started](#getting-started) for details and usage examples.      

## Getting Started

Let us first introduce some important concepts that    
we will use in this section and throughout the document.    

***Measurable value*** *is a typed value that can be measured.*  

Examples: the number of requests processed, the number of requests    
processed with errors, the average request processing time,    
the amount of free memory, the number of items in the queue.    

Each measurable value has a type.      
Measurable value types are represented by subclasses of ```Measurable```.       
Examples:         
- ```Counter.COUNT``` (the number of requests processed)    
- ```Histogram.MEAN``` (the average request processing time)  
- ```Var.Value``` (the amount of free memory)  

***Metric*** *is a typed named entity measuring a certain set of ```Measurable```s,    
and providing the results of the measurements.*      

Each metric has a type that supports a certain set of ```Measurable```s and  
a certain way of measuring them.

Metric types are represented by subclasses of ```Metric```.     
Examples:  
- [Counter](#counter) is a ```java.lang.Long``` based counter,    
  measuring exactly one value - the current value of the counter (```Counter.COUNT```).      
  The measurement is performed by explicitly increasing or decreasing the counter:      
  ```java 
  Counter requestErrorCounter = ... // COUNT = 0
  requestErrorCounter.inc();        // COUNT = 1
  requestErrorCounter.inc(2);       // COUNT = 3
  requestErrorCounter.dec(2);       // COUNT = 1
  ```

- [Timer](#timer) measures the number of times something happens,      
  the speed at which it happens (rate), and the statistical distribution of its duration:  
    - ```Counter.COUNT``` (the number of times something happens)  
    - ```Rate.MEAN_RATE``` (the mean rate)  
    - ```Rate.ONE_MINUTE_RATE```
    - ```Rate.FIVE_MINUTES_RATE```
    - ```Rate.FIFTEEN_MINUTES_RATE```
    - ```Rate.RATE_UNIT```
    - ```Histogram.MIN``` (the min duration)  
    - ```Histogram.MAX``` (the max duration)  
    - ```Histogram.MEAN``` (the mean duration)  
    - ```Histogram.STANDARD_DEVIATION```
    - ```Histogram.Percentile``` (including the predefined ```PERCENTILE_5```, ```PERCENTILE_10```, ...)
    - ```Timer.DURATION_UNIT```  

  The measurement is performed by explicitly taking into account the durations:    
  ```java 
  // Histogram.MAX = 0.0
  // Histogram.MEAN = 0.0
  Timer requestTimer = ...
  
  // Histogram.MAX = 10.0
  // Histogram.MEAN = 10.0
  requestTimer.update(10, NANOSECONDS);
  
  // Histogram.MAX = 20.0
  // Histogram.MEAN = 15.0
  requestTimer.update(20, NANOSECONDS); 
  
  // Histogram.MAX = 30.0
  // Histogram.MEAN = 20.0
  requestTimer.update(30, NANOSECONDS); 
  ``` 

Each metric has a name represented by the ```MetricName``` class.  

```Metric``` does not provide measurement results directly.    
Instead, ```Metric``` provides the set of its instances (```MetricInstance```),    
which, in turn, provide measurement results:    

```java
interface Metric extends Iterable<MetricInstance> {
    ...
}

interface MetricInstance {
    ...
    Set<Measurable> measurables();
    <V> V valueOf(Measurable measurable) throws NotMeasuredException;
}

```

This design has been driven by the need to support labeled metrics.  

***Labeled metric*** *is a metric with which a set of attributes (***labels***) is associated,      
and which generates a separate "child" metric (represented by a ```MetricInstance```)    
for each involved (for which there was at least one update) combination of values of these attributes.*      

Let us explain this definition with the following example 1,      
that we will use and develop throughout this section.  

Assume you are developing an HTTP service that, in order to do its job, calls several other HTTP services.  
Each of the external services is represented by several instances running on specific servers and ports.        
The service being developed can call any of these instances.      
(for example, by using round-robin balancing).    

Let us also assume that we would like to have a ***separate*** request execution [Timer](#timer)      
for each instance of an external service (that is, for each combination of service, server, and port         
*for which at least one request has been made*).      

In other words, we would like to define a ***labeled [Timer](#timer)*** with the    
***labels*** service, server, port (taking into account the order of the labels).  

Let us now return to the concept of a metric instance (```MetricInstance```), and consider it in more detail.

Instances are identified by a name (```MetricName```) which always starts with the metric's name (```MetricName```)          
**and** a list of label values (```LabelValue```).          
There is also the set of ```Measurable```s associated with an instance.      
The instance allows you to get the value of any ```Measurable``` from this set (see ```instance.valueOf(m)``` below):  

```java  
Counter counter = registry.counter(withName("counter"));

counter.inc();
counter.inc(2);
counter.dec();

counter.addListener(new MetricListener() {

    @Override
    public void metricInstanceAdded(MetricInstance instance) {
        List<String> labelValuesString = instance.labelValues().stream()
            .map(lv -> lv.label().name() + "=" + lv.value())
            .collect(toList());

        String valuesString = "{" + instance.measurables().stream()
            .map(m -> m.getClass().getSimpleName() + "=" + instance.valueOf(m))
            .collect(joining(", ")) + "}";
            
        // You can also use the following "snapshot-based" approach for getting values:
        // 
        // MeasurableValues values = instance.measurableValues();
        // values.valueOf(m);    
      
        System.out.println(
            "Metric instance added:\n"
            + "  name = '" + instance.name() + "',\n"
            + "  label values = " + instance.labelValues() + ",\n"
            + "  total instance = " + instance.isTotalInstance() + ",\n"
            + "  labeled metric total instance = " + instance.isLabeledMetricTotalInstance() + ",\n"
            + "  level instance = " + instance.isLevelInstance() + ",\n"
            + "  measurable values = " + valuesString);
    }

    @Override
    public void metricInstanceRemoved(MetricInstance instance) {
        System.out.println("Metric instance removed: name = '" + instance.name() + "'");
    }
});
``` 

Output:
```
Metric instance added:
  name = 'counter',
  label values = [],
  total instance = true,
  labeled metric total instance = false,
  level instance = false,
  measurable values = {Count=2}
```

The lifetime of some instances coincides with the lifetime of the metric,            
while others can be added or removed when certain conditions are met.            
Being ```Iterable<MetricInstance>```, the metric provides the current set of instances (thread-safely).            
This feature is used by most exporters (see below).          
It's also possible to subscribe to the events ```metricInstanceAdded``` and ```metricInstanceRemoved```.        
This feature is used, for example, by ```JmxMetricsReporter```.  

An update of a metric is taken into account by the corresponding subset of instances.        
By default, only ***total instance*** is created along with the metric    
(it can be disabled through metric configuration).        
The total instance takes all updates into account.        
The lifetime of the total instance is the same as the lifetime of the metric.    
This is the only instance created for non-labeled metrics.  

If the metric is labeled, an instance is created for each combination of ```LabelValue```s.        
Such an instance takes into account only those updates that are made for the corresponding combination:    

```java  
Histogram histogram = registry.histogram(
    withName("histogram"),
    () -> withHistogram()
        .labels(SERVICE, SERVER, PORT)
        .allSlice().noLevels());

// updates the total instance and the instance service_1/server_1_1/111
histogram.update(10, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111"))); 

// updates the total instance and the instance service_1/server_1_12/121
histogram.update(20, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));

// updates the total instance and the instance service_2/server_2_1/211
histogram.update(30, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));

// updates the total instance and the instance service_2/server_2_1/212
histogram.update(40, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("212")));

// instances are added asynchronously
Thread.sleep(25);

histogram.forEach(instance -> {
    List<String> labelValuesString = instance.labelValues().stream()
        .map(lv -> lv.label().name() + "=" + lv.value())
        .collect(toList());

    // "snapshot-based" approach for getting values
    MeasurableValues values = instance.measurableValues();

    String valuesString = "{" + instance.measurables().stream()
        // or instance.valueOf(m) but this approach is not "snapshot-based"
        // and should not be used unnecessarily
        .map(m -> {
            String name =
                m instanceof Histogram.Percentile ?
                "Percentile_" + ((Histogram.Percentile)m).quantileAsString() :
                m.getClass().getSimpleName();

            return name + "=" + values.valueOf(m);
        })
        .collect(joining(", ")) + "}";

    System.out.println(
        "Metric instance:\n"
        + "  name = '" + instance.name() + "',\n"
        + "  label values = " + labelValuesString + ",\n"
        + "  total instance = " + instance.isTotalInstance() + ",\n"
        + "  labeled metric total instance = " + instance.isLabeledMetricTotalInstance() + ",\n"
        + "  level instance = " + instance.isLevelInstance() + ",\n"
        + "  measurable values = " + valuesString);
});
```

Output:
```
Metric instance:
  name = 'histogram',
  label values = [],
  total instance = true,
  labeled metric total instance = true,
  level instance = false,
  measurable values = {Count=4, Max=40, Percentile_0.9=40.0, Percentile_0.99=40.0, Percentile_0.5=30.0, Mean=25.0, Min=10}
  
Metric instance:
  name = 'histogram',
  label values = [service=service_1, server=server_1_1, port=111],
  total instance = false,
  labeled metric total instance = false,
  level instance = false,
  measurable values = {Count=1, Max=10, Percentile_0.9=10.0, Percentile_0.99=10.0, Percentile_0.5=10.0, Mean=10.0, Min=10}
  
Metric instance:
  name = 'histogram',
  label values = [service=service_1, server=server_1_2, port=121],
  total instance = false,
  labeled metric total instance = false,
  level instance = false,
  measurable values = {Count=1, Max=20, Percentile_0.9=20.0, Percentile_0.99=20.0, Percentile_0.5=20.0, Mean=20.0, Min=20}  
  
Metric instance:
  name = 'histogram',
  label values = [service=service_2, server=server_2_1, port=211],
  total instance = false,
  labeled metric total instance = false,
  level instance = false,
  measurable values = {Count=1, Max=30, Percentile_0.9=30.0, Percentile_0.99=30.0, Percentile_0.5=30.0, Mean=30.0, Min=30}  
  
Metric instance:
  name = 'histogram',
  label values = [service=service_2, server=server_2_1, port=212],
  total instance = false,
  labeled metric total instance = false,
  level instance = false,
  measurable values = {Count=1, Max=40, Percentile_0.9=40.0, Percentile_0.99=40.0, Percentile_0.5=40.0, Mean=40.0, Min=4}
```

The life cycle of such an instance may end earlier the life cycle of the metric      
in the case of an eviction or expiration (see [Labeled metrics](#labeled-metrics)),          
as well as in the case of deregistering a list of label values for a variable (see [Var and Caching Var](#var-and-cachingvar)).    

To manage metrics, a special entity is used - metric registry.  
 
***Metric registry*** *allows you to add and remove metrics, get the current set of metrics, and        
subscribe to an event of adding/removing a metric.*      

A metric registry is represented by the ```MetricRegistry``` class.  

```MetricRegistry``` implementations provide metric implementations.    
Currently, the following implementations are supported:
- ```DefaultMetricRegistry```
- ```DropwizardMetricRegistry``` based on https://metrics.dropwizard.io  
In the next versions, we plan to abandon this implementation in favor of ```DefaultMetricRegistry```

Let us move on to practice and show how to implement the requirements from example 1      
using Metrics Facade. First, you need to add a number of dependencies:        

Base (Core):
```xml
<dependency>
    <groupId>com.ringcentral.platform.metrics</groupId>
    <artifactId>metrics-facade-base</artifactId>
    <version>3.1.0-RELEASE</version>
</dependency>
```

```MetricRegistry``` implementation (for example, ```DefaultMetricRegistry```):
```xml
<dependency>
    <groupId>com.ringcentral.platform.metrics</groupId>
    <artifactId>metrics-facade-default-impl</artifactId>
    <version>3.1.0-RELEASE</version>
</dependency>
```

Metrics reporter(s) (for example, ```PrometheusMetricsExporter```):
```xml
<dependency>
    <groupId>com.ringcentral.platform.metrics</groupId>
    <artifactId>metrics-facade-prometheus</artifactId>
    <version>3.1.0-RELEASE</version>
</dependency>
```

Let's add metrics.    
At the moment, we have only one metric - a labeled [Timer](#timer) for requests to    
external services with the labels service, server, and port.  

A label is represented by the ```Label``` class.  
Let's define the labels:    
```java
Label SERVICE = new Label("service");
Label SERVER = new Label("server");
Label PORT = new Label("port");
```

and add the metric using```MetricRegistry```:      

```java
MetricRegistry registry = new DefaultMetricRegistry();  
...

Timer httpClientRequestTimer = registry.timer(
    withName("http", "client", "request", "duration"),
    () -> withTimer().labels(SERVICE, SERVER, PORT));
```

The next step is to update the metric.      
To do this, after executing a request to an external service,      
it is necessary to record the time of its execution:     

```java
// in nanos
long requestDuration = ...

httpClientRequestTimer.update(
    requestDuration, 
    forLabelValues(SERVICE.value("authorizationService"), SERVER.value("127.0.0.1"), PORT.value("7001"))))
```

You can also use the following recording scheme:    

```java
// start a stopwatch before executing the request    
Stopwatch stopwatch = fullConfigTimer.stopwatch(forLabelValues(
    SERVICE.value("authorizationService"), 
    SERVER.value("127.0.0.1"), 
    PORT.value("7001"))));
...
        
// stop the stopwatch after executing the request    
stopwatch.stop()
```

Here the question may arise, what is measured?  
By default, [Timer](#timer) measures the following values:  
- ```Counter.COUNT```
- ```Rate.MEAN_RATE```
- ```Rate.ONE_MINUTE_RATE```
- ```Rate.FIVE_MINUTES_RATE```
- ```Rate.FIFTEEN_MINUTES_RATE```
- ```Rate.RATE_UNIT```
- ```Histogram.MIN```
- ```Histogram.MAX```
- ```Histogram.MEAN```
- ```Histogram.PERCENTILE_50```
- ```Histogram.PERCENTILE_90```
- ```Histogram.PERCENTILE_99```
- ```Timer.DURATION_UNIT```

However, Metrics Facade allows for *extremely flexible configuration of metrics*.      
In particular, for each metric you can specify a set of values to be ***calculated*** and ***exported***.  

Suppose, for example, that you have multiple timers in your project, and for most of them    
(and the number of timers in an average project can reach several dozen)      
you want to measure ```Counter.COUNT```, ```Histogram.MAX```, ```Histogram.MEAN```, ```Histogram.PERCENTILE_90```,    
but for the timer for processing incoming HTTP requests you want to also measure    
```Histogram.MIN```, ```Histogram.PERCENTILE_75```, ```Histogram.PERCENTILE_99```.    

This can be achieved as follows:    
1) Set the required ***default*** values for timers:
   ```java
   registry.preConfigure(allMetrics(), modifying().timer(withTimer().measurables(
       COUNT,
       MAX, 
       MEAN, 
       PERCENTILE_90)));
   ```

2) Set the required set of values when defining the timer for processing    
   incoming HTTP requests (the defaults will be overridden):  
   ```java
   Timer httpRequestTimer = registry.timer(
      withName("http", "request", "duration"),
      () -> withTimer().measurables(
          COUNT,
          MIN,
          MAX,
          MEAN,
          PERCENTILE_75,
          PERCENTILE_90,
          PERCENTILE_99)
   ```
   ***or*** set the required ***default*** values for ```Timer``` with the appropriate name:  
   ```java
   registry.preConfigure(
       // there may also be other predicates (for example, metricsMatchingNameMask ("a.**.b")),
       // with which you can effectively configure any subset of metrics.
       metricWithName("http", "request", "duration"), 
       modifying().timer(withTimer().measurables(
           COUNT,
           MIN,
           MAX,
           MEAN,
           PERCENTILE_75,
           PERCENTILE_90,
           PERCENTILE_99)));
   ```

This configuration scheme allows:  

- Set the configuration in one place without having to change the code of defining each metric involved.      
- Optimize calculations and export.        
  For example, the implementation of a metric is not required to perform an expensive operation of        
  calculating percentiles, if percentiles are not included in the set of values defined for this metric.     
  We plan to provide such implementations in one of the next major versions.        
- Add (in future versions) support for new measurable values ***without breaking backward compatibility***.  
  Unlike most other libraries for working with metrics, the Metrics Facade interfaces do not provide    
  special methods for getting values of certain measured values (e.g. ```double Histogram.mean()```).      
  Instead, methods are used that take ```Measurable``` as a parameter:        
  ```MetricInstance.valueOf(Measurable)``` or        
  ```MetricInstance.measurableValues().valueOf(Measurable)``` (a snapshot-based approach).    
  This design allows the library to remain extremely flexible in terms of supporting new features.    
  
Let's add a counter of active client connections (sessions) to our project:    
```java
Counter activeClientConnectionCounter = registry.counter(withName("active", "client", "connections"));
```

Now we have two metrics.       
These metrics live in memory, measuring certain values.      
However, metrics are usually used to provide runtime information about an application instance.  
In order to do this, it is necessary not only to collect metrics but also to export them to  
external monitoring systems that provide various means of visualizing and analyzing the collected metrics.  
For example, Prometheus (https://prometheus.io).   

To export metrics, metrics reporters are used ([Metrics Reporters](#metrics-reporters)).

***Metrics reporter*** *allows you to present metrics in the format of a specific monitoring system;  
some reporters additionally send metrics to an external monitoring system in the appropriate format.*

Let's add reporters for Prometheus and JMX:
```java
PrometheusMetricsExporter prometheusExporter = new PrometheusMetricsExporter(registry);
new PrometheusHttpServer(9095, prometheusExporter); // This server is for tests only

registry.addListener(new JmxMetricsReporter());
```

Let's assume that the following metric updates have been made:    
```java
httpClientRequestTimer.update(
    100L, MILLISECONDS,
    forLabelValues(SERVICE.value("authorizationService"), SERVER.value("127.0.0.1"), PORT.value("7001")));

httpClientRequestTimer.update(
    200L, MILLISECONDS,
    forLabelValues(SERVICE.value("authorizationService"), SERVER.value("127.0.0.2"), PORT.value("7002")));

// start a stopwatch before executing the request
Stopwatch stopwatch = httpClientRequestTimer.stopwatch(forLabelValues(
    SERVICE.value("throttlingService"),
    SERVER.value("127.0.0.3"),
    PORT.value("7003")));

sleep(300L);

// stop the stopwatch after executing the request
stopwatch.stop();

activeClientConnectionCounter.inc();
activeClientConnectionCounter.inc();
activeClientConnectionCounter.inc();
activeClientConnectionCounter.dec();
```

Let's take a look at (slightly modified and formatted for better readability)    
the response of the test Prometheus server (http://localhost:9095/metrics):

```java
# TYPE http_client_request_duration summary
http_client_request_duration_count{service="authorizationService",server="127.0.0.1",port="7001",} 1.0
http_client_request_duration{service="authorizationService",server="127.0.0.1",port="7001",quantile="0.9",} 100.0
http_client_request_duration{service="authorizationService",server="127.0.0.1",port="7001",quantile="0.99",} 100.0
http_client_request_duration{service="authorizationService",server="127.0.0.1",port="7001",quantile="0.5",} 100.0
        
http_client_request_duration_count{service="authorizationService",server="127.0.0.2",port="7002",} 1.0
http_client_request_duration{service="authorizationService",server="127.0.0.2",port="7002",quantile="0.9",} 200.0
http_client_request_duration{service="authorizationService",server="127.0.0.2",port="7002",quantile="0.99",} 200.0
http_client_request_duration{service="authorizationService",server="127.0.0.2",port="7002",quantile="0.5",} 200.0
        
http_client_request_duration_count{service="throttlingService",server="127.0.0.3",port="7003",} 1.0
http_client_request_duration{service="throttlingService",server="127.0.0.3",port="7003",quantile="0.9",} 300.692665
http_client_request_duration{service="throttlingService",server="127.0.0.3",port="7003",quantile="0.99",} 300.692665
http_client_request_duration{service="throttlingService",server="127.0.0.3",port="7003",quantile="0.5",} 300.692665
        
# TYPE http_client_request_duration_max gauge
http_client_request_duration_max{service="authorizationService",server="127.0.0.1",port="7001",} 100.0
http_client_request_duration_max{service="authorizationService",server="127.0.0.2",port="7002",} 200.0
http_client_request_duration_max{service="throttlingService",server="127.0.0.3",port="7003",} 300.692665        
        
# TYPE http_client_request_duration_mean gauge
http_client_request_duration_mean{service="authorizationService",server="127.0.0.1",port="7001",} 100.0
http_client_request_duration_mean{service="authorizationService",server="127.0.0.2",port="7002",} 200.0
http_client_request_duration_mean{service="throttlingService",server="127.0.0.3",port="7003",} 300.692665
        
# TYPE active_client_connections gauge
active_client_connections 2.0
```

Now let's assume that the servers and ports for external services are not statically configured,      
and are periodically requested from a special external service named "discoveryService".      
Let's also assume that we would not want the duration of requests to this "auxiliary" service      
to affect the statistics for other services ("business" services).        

This can be achieved through exclusion of label values matching the corresponding predicate:        
```java
Timer httpClientRequestTimer = registry.timer(
    withName("http", "client", "request", "duration"),
    () -> withTimer()
        .labels(SERVICE, SERVER, PORT)
        .exclude(labelValuesMatchingAll(SERVICE.mask("discoveryService"))));
```

The following metric update will be ignored:    
```java
httpClientRequestTimer.update(
    100L, MILLISECONDS,
    forLabelValues(SERVICE.value("discoveryService"), SERVER.value("127.0.0.4"), PORT.value("7004")));
```

Further, since services are discovered dynamically,          
the set of their server-port combinations can change over time.          
The set of ```MetricInstance```s of the timer will, accordingly, grow indefinitely.        
Some of the ```MetricInstance```s will represent non-existent servers, and because    
they have already been exported to external monitoring systems, they are no longer needed,    
but at the same time they continue to consume resources (for example, memory).          

To solve this problem, Metrics Facade allows you to set for each metric:  
- the maximum number of combinations of label values:          
  when this threshold is exceeded, the combination that has not been updated for the longest time,          
  will be automatically removed  
- the expiration time for a combination of label values:    
  if a combination has not been updated during this time, it will be automatically removed  
  
For example:  

```java
Timer httpClientRequestTimer = registry.timer(
    withName("http", "client", "request", "duration"),
    () -> withTimer()
        .labels(SERVICE, SERVER, PORT)
        .exclude(labelValuesMatchingAll(SERVICE.mask("discoveryService")))
        .maxLabeledInstancesPerSlice(100) // eviction
        .expireLabeledInstanceAfter(30, SECONDS)); // expiration
```

Further, it would often be helpful to have:  
1) a total ```MetricInstance``` for each service    
   (taking all updates for this service into account)    
2) a total ```MetricInstance``` for each service-server combination  
3) a total ```MetricInstance``` for each server.    
   Let's also assume that for such an instance, it is sufficient to measure only      
   ```MAX```, ```MEAN```, and ```PERCENTILE_99```,        
   and it is necessary to consider only updates for services whose name either starts with "auth" or        
   contains the "throttling" substring, but at the same time exclude updates for port 7004      

Metrics Facade offers the ability to ***automatically*** add this kind of        
```MetricInstance```s through the functionality of slices and levels.         

***Slice*** *is a child metric that takes into account only those updates of the parent metric     
which satisfy the given ```LabelValuesPredicate```.      
A slice can have its own configuration: name suffix, labels      
(MUST be sublist if the parent metric's labels), measurable values, etc.*        

By default, a metric has only one slice - ```AllSlice``` taking all updates into account.    

***Slice level*** *is a set of ```MetricInstance```s of the slice  
for the first ```k``` labels, ```k = 1..<label_count> - 1```.*   

For example, if for a slice:    
- three labels are defined: ```SERVICE```, ```SERVER```, ```PORT```  
- levels are enabled  

then when updating this slice for the values             
```SERVICE.value("service_1")```, ```SERVER.value("server_1_1")```, ```PORT.value("111")```,      
the following ```MetricInstance```s will be created/updated:
- ```SERVICE.value("service_1")```, ```SERVER.value("server_1_1")```, ```PORT.value("111")```
- ```SERVICE.value("service_1")```, ```SERVER.value("server_1_1")``` ***level instance***
- ```SERVICE.value("service_1")``` ***level instance***

By default, levels are enabled for ```AllSlice``` and disabled for other slices.  
Here is how you can implement the above requirements using the functionality of slices and levels:  
 
```java
Timer httpClientRequestTimer = registry.timer(
    withName("http", "client", "request", "duration"),
    () -> withTimer()
        .labels(SERVICE, SERVER, PORT)
        ...
        .allSlice()
            .enableLevels() // enabled by default for AllSlice; implements 1) and 2)
        .slice("by", "server") // implements 3) 
            .predicate(labelValuesMatchingAll(
                SERVICE.mask("auth*|*throttling*"),
                PORT.predicate(p -> !p.equals("7004"))))
            .labels(SERVER)
            .measurables(MAX, MEAN, PERCENTILE_99));
```

You can find the complete sample ```GettingStartedSample.java``` in the following Maven module:    
```xml
<dependency>
    <groupId>com.ringcentral.platform.metrics</groupId>
    <artifactId>metrics-facade-samples</artifactId>
    <version>3.1.0-RELEASE</version>
</dependency>
```

## Features

### Flexible Configuration

You can set up defaults/overrides for ***any*** subset of metrics      
(by using a predicate) and ***almost any*** metric parameters.                    
In particular, you can specify a set of values to be ***calculated*** and ***exported***.                
For example, the implementation of a metric is not required to perform an expensive operation of            
calculating percentiles, if percentiles are not included in the set of values defined for this metric.                

Example of setting up defaults (```DefaultsSample.java```):  
```java
registry.preConfigure(allMetrics(), modifying()
    .metric(withMetric().prefix(labelValues(SAMPLE.value("defaults"))))
    .meter(withMeter()
        .expireLabeledInstanceAfter(30, MINUTES)
        .allSlice().noLevels())
    .rate(withRate().measurables(COUNT, ONE_MINUTE_RATE))
    .histogram(withHistogram().measurables(COUNT, MAX, MEAN, PERCENTILE_95))
    .timer(withTimer().measurables(COUNT, ONE_MINUTE_RATE, MAX, MEAN, PERCENTILE_95)));
```

Example of setting up overrides (```OverridesSample.java```):  
```java
registry.postConfigure(allMetrics(), modifying()
    .metric(withMetric().prefix(labelValues(SAMPLE.value("overrides"))))
    .meter(withMeter()
        .expireLabeledInstanceAfter(30, MINUTES)
        .allSlice().noLevels())
    .rate(withRate().measurables(COUNT, ONE_MINUTE_RATE))
    .histogram(withHistogram().measurables(COUNT, MAX, MEAN, PERCENTILE_95))
    .timer(withTimer().measurables(COUNT, ONE_MINUTE_RATE, MAX, MEAN, PERCENTILE_95)));
```

...for any subset of metrics and almost any parameters:    

```java
registry.postConfigure(metricWithName("a.b.c"), modifying().metric(withMetric().disable()));
registry.postConfigure(metricsWithNamePrefix("a.b"), modifying().histogram(withHistogram().enable()));
registry.postConfigure(metricsMatchingNameMask("a.**.b"), modifying().histogram(withHistogram().enable()));

registry.postConfigure(
    metrics()
        .including(metricsMatchingNameMask("a.b.**.d.**")).excluding(metricWithName("a.b.c.d"))
        .including(metricsWithNamePrefix("d.e.f")).excluding(metricsWithNamePrefix("d.e.f.g")),
    modifying().meter(withMeter().disable()));

assert registry.timer(withName("a", "b", "c", "d")).isEnabled();
assert !registry.timer(withName("a", "b", "c", "d", "e")).isEnabled(); // disabled
assert !registry.timer(withName("a", "b", "x", "d")).isEnabled(); // disabled
assert !registry.timer(withName("d", "e", "f")).isEnabled(); // disabled
assert registry.timer(withName("d", "e", "f", "g")).isEnabled();
assert registry.timer(withName("d", "e", "f", "g", "h")).isEnabled();
```

See [Getting Started](#getting-started), ```DefaultsSample.java```,    
and ```OverridesSample.java``` for more details and usage examples.    

### Labeled metrics

In particular, Metrics Facade supports eviction and expiration for labeled metrics,        
that is, for each labeled metric, it allows you to specify:
- the maximum number of combinations of label values:        
  when this threshold is exceeded, the combination that has not been updated for the longest time,        
  will be automatically removed (will not waste system resources)
- the expiration time for a combination of label values:  
  if a combination has not been updated during this time, it will be automatically removed
  
```LabeledMetricsEvictionAndExpirationSample.java```
```java
registry.histogram(
    withName("ActiveHealthChecker", "healthCheck", "attemptCount", "histogram"),
    () -> withHistogram()
        .labels(SERVICE, SERVER, PORT)
        .maxLabeledInstancesPerSlice(5) // eviction
        .expireLabeledInstanceAfter(1, MINUTES)); // expiration
```

See [Getting Started](#getting-started) and ```LabeledMetricsEvictionAndExpirationSample.java```   
for more details and usage examples.    

### Prefix Label Values

```java
registry.postConfigure(
    metricsMatchingNameMask("ActiveHealthChecker.**"),
    modifying().metric(withMetric().prefix(labelValues(SAMPLE.value("prefixLabelValues")))));
    
registry.histogram(
    withName("ActiveHealthChecker", "healthCheck", "attemptCount", "histogram"),
    () -> withHistogram().labels(SERVICE, SERVER, PORT));

h.update(25, forLabelValues(
    SERVICE.value("service_1"), 
    SERVER.value("server_1_1"), 
    PORT.value("7001")));    
```

is the same as

```java
registry.histogram(
    withName("ActiveHealthChecker", "healthCheck", "attemptCount", "histogram"),
    () -> withHistogram().labels(SAMPLE, SERVICE, SERVER, PORT));

h.update(25, forLabelValues(
    SAMPLE.value("prefixLabelValues"),
    SERVICE.value("service_1"), 
    SERVER.value("server_1_1"), 
    PORT.value("7001")));    
```

See ```PrefixLabelValuesSample.java``` for more details and usage examples.

### Exclusions

You can drop the metric updates for label values matching a predicate:

```ExclusionsSample.java```
```java
registry.timer(
    withName("ActiveHealthChecker", "healthCheck"),
    () -> withTimer()
        .labels(SERVICE, SERVER, PORT)
        .exclude(labelValuesMatchingAny(
            SERVER.mask("server_1_*|*2_1*"),
            PORT.predicate(p -> p.equals("9001")))));
```

See [Getting Started](#getting-started) and ```ExclusionsSample.java``` for more details and usage examples.

### Slices and Levels

***Slice*** *is a child metric that takes into account only those updates of the parent metric       
which satisfy the given ```LabelValuesPredicate```.      
A slice can have its own configuration: name suffix, labels      
(MUST be sublist if the parent metric's labels), measurable values, etc.*

By default, a metric has only one slice - ```AllSlice``` taking all updates into account.  

***Slice level*** *is a set of ```MetricInstance```s of the slice    
for the first ```k``` labels, ```k = 1..<label_count> - 1```.*  

For example, if for a slice:    
- three labels are defined: ```SERVICE```, ```SERVER```, ```PORT```    
- levels are enabled    

then when updating this slice for the values                 
```SERVICE.value("service_1")```, ```SERVER.value("server_1_1")```, ```PORT.value("111")```,        
the following ```MetricInstance```s will be created/updated:  
- ```SERVICE.value("service_1")```, ```SERVER.value("server_1_1")```, ```PORT.value("111")```  
- ```SERVICE.value("service_1")```, ```SERVER.value("server_1_1")``` ***level instance***  
- ```SERVICE.value("service_1")``` ***level instance***  

By default, levels are enabled for ```AllSlice``` and disabled for other slices.    
See [Getting Started](#getting-started) and ```SlicesAndLevelsSample.java``` for more details and usage examples.    

## Metrics

### Counter

```c.r.p.metrics.counter.Counter``` is a ```java.lang.Long``` based counter that can be increased or decreased:    

```CounterSample.java```
```java
// Default config:
//   no labels
//   measurables: { COUNT }
Counter defaultConfigCounter = registry.counter(withName("counter", "defaultConfig"));

defaultConfigCounter.inc();
defaultConfigCounter.inc(2);
defaultConfigCounter.dec();
```

Supported measurables:  
- ```Counter.COUNT```

#### Counter Config

```CounterSample.java```  
```java
Counter fullConfigCounter = registry.counter(
    withName("counter", "fullConfig"),
    () -> withCounter()
        // options: disable(), enabled(boolean)
        // default: enabled
        .enable()

        // default: no prefix label values
        .prefix(labelValues(SAMPLE.value("counter")))

        // default: no labels
        .labels(SERVICE, SERVER, PORT)

        // options: noExclusions()
        // default: no exclusions
        .exclude(labelValuesMatchingAny(
            SERVICE.mask("serv*2|serv*4*"),
            SERVER.mask("server_5")))

        // default: unlimited
        .maxLabeledInstancesPerSlice(5)

        // options: notExpireLabeledInstances()
        // default: no expiration
        .expireLabeledInstanceAfter(25, SECONDS)

        // options: noMeasurables()
        // default: { COUNT }
        .measurables(COUNT)

        // the properties specific to the metrics implementation
        // default: no properties
        .put("key_1", "value_1_1")

        .allSlice()
            // options: disable(), enabled(boolean)
            // default: enabled
            .enable()

            // default: the metric's labels [ SERVICE, SERVER, PORT ]
            .labels(SERVICE, SERVER)

            // options: noMaxLabeledInstances()
            // default: the metric's maxLabeledInstancesPerSlice = 5
            .maxLabeledInstances(10)

            // options: notExpireLabeledInstances()
            // default: the metric's expireLabeledInstanceAfter = 25 SECONDS
            .expireLabeledInstanceAfter(42, SECONDS)

            // options: noMeasurables() 
            // default: the metric's measurables { COUNT }
            .measurables(COUNT)

            // options: disableTotal(), noTotal(), totalEnabled(boolean)
            // default: enabled
            .enableTotal()

            // options: disableLevels(), noLevels(), levelsEnabled(boolean)
            // default: enabled
            .enableLevels()

            // the properties specific to the metrics implementation
            // default: no properties (no overrides)
            .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_1"
            .put("key_2", "value_2_1")

            .total(counterInstance()
                // default: empty name suffix
                .name("total")

                // options: noMeasurables()
                // default: the slice's measurables { COUNT }
                .measurables(COUNT)

                // the properties specific to the metrics implementation
                // default: no properties (no overrides)
                .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_2"
                .put("key_2", "value_2_2")) // overrides "key_2" -> "value_2_1"
        .slice("byService")
            // options: disable(), enabled(boolean)
            // default: enabled
            .enable()

            // default: no predicate
            .predicate(labelValuesMatchingAll(
                SERVICE.mask("serv*_1*"),
                SERVER.predicate(s -> s.equals("server_1_1"))))

            // default: no labels
            .labels(SERVICE)

            // options: noMaxLabeledInstances()
            // default: the metric's maxLabeledInstancesPerSlice = 5
            .maxLabeledInstances(2)

            // options: notExpireLabeledInstances()
            // default: the metric's expireLabeledInstanceAfter = 25 SECONDS
            .expireLabeledInstanceAfter(42, SECONDS)

            // options: noMeasurables()
            // default: the metric's measurables { COUNT }
            .measurables(COUNT)

            // options: disableTotal(), noTotal(), totalEnabled(boolean)
            // default: enabled
            .enableTotal()

            // options: disableLevels(), noLevels(), levelsEnabled(boolean)
            // default: disabled
            .enableLevels()

            // the properties specific to the metrics implementation
            // default: no properties (no overrides)
            .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_1"
            .put("key_2", "value_2_1")

            .total(counterInstance()
                // default: empty name suffix
                .name("total")

                // options: noMeasurables()
                // default: the slice's measurables { COUNT }
                .measurables(COUNT)

                // the properties specific to the metrics implementation
                // default: no properties (no overrides)
                .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_2"
                .put("key_2", "value_2_2")) // overrides "key_2" -> "value_2_1"
);
```

### Rate

```c.r.p.metrics.rate.Rate``` measures the number of times something happens (```Counter.COUNT```)  
and the speed at which it happens
(for example, the number of requests per ```Rate.RATE_UNIT```).

Supported measurables:  
- ```Counter.COUNT```
- ```Rate.MEAN_RATE```
- ```Rate.ONE_MINUTE_RATE```
- ```Rate.FIVE_MINUTES_RATE```
- ```Rate.FIFTEEN_MINUTES_RATE```
- ```Rate.RATE_UNIT```

```RateSample.java```
```java
// Default config:
//   no labels
//   measurables: {
//     COUNT,
//     MEAN_RATE,
//     ONE_MINUTE_RATE,
//     FIVE_MINUTES_RATE,
//     FIFTEEN_MINUTES_RATE,
//     RATE_UNIT
//   }
Rate defaultConfigRate = registry.rate(withName("rate", "defaultConfig"));

defaultConfigRate.mark();
defaultConfigRate.mark(2);
```

#### Rate Config

```RateSample.java```
```java
Rate fullConfigRate = registry.rate(
    withName("rate", "fullConfig"),
    () -> withRate()
        // options: disable(), enabled(boolean)
        // default: enabled
        .enable()

        // default: no prefix label values
        .prefix(labelValues(SAMPLE.value("rate")))

        // default: no labels
        .labels(SERVICE, SERVER, PORT)

        // options: noExclusions()
        // default: no exclusions
        .exclude(labelValuesMatchingAny(
            SERVICE.mask("serv*2|serv*4*"),
            SERVER.mask("server_5")))

        // default: unlimited
        .maxLabeledInstancesPerSlice(5)

        // options: notExpireLabeledInstances()
        // default: no expiration
        .expireLabeledInstanceAfter(25, SECONDS)

        // options: noMeasurables()
        // default: {
        //   COUNT,
        //   MEAN_RATE,
        //   ONE_MINUTE_RATE,
        //   FIVE_MINUTES_RATE,
        //   FIFTEEN_MINUTES_RATE,
        //   RATE_UNIT
        // }
        .measurables(COUNT)
        
        /**
         * options:
         *   - expMovingAverage() == {@link ExpMovingAverageRateImplConfigBuilder#expMovingAverage()},
         *   - custom impl, e.g. countAndMean() == {@link CountScalingRateConfigBuilder#countScaling()}.
         *     Custom impls must be registered: registry.extendWith(new CountScalingRateConfigBuilder()).
         * default: expMovingAverage()
         */
        .impl(expMovingAverage())
        // .impl(countScaling().factor(2)) // custom impl

        // the properties specific to the metrics implementation
        // default: no properties
        .put("key_1", "value_1_1")

        .allSlice()
            // options: disable(), enabled(boolean)
            // default: enabled
            .enable()

            // default: the metric's labels [ SERVICE, SERVER, PORT ]
            .labels(SERVICE, SERVER)

            // options: noMaxLabeledInstances()
            // default: the metric's maxLabeledInstancesPerSlice = 5
            .maxLabeledInstances(10)

            // options: notExpireLabeledInstances()
            // default: the metric's expireLabeledInstanceAfter = 25 SECONDS
            .expireLabeledInstanceAfter(42, SECONDS)

            // options: noMeasurables()
            // default: the metric's measurables { COUNT }
            .measurables(COUNT, MEAN_RATE)

            // options: disableTotal(), noTotal(), totalEnabled(boolean)
            // default: enabled
            .enableTotal()

            // options: disableLevels(), noLevels(), levelsEnabled(boolean)
            // default: enabled
            .enableLevels()

            // the properties specific to the metrics implementation
            // default: no properties (no overrides)
            .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_1"
            .put("key_2", "value_2_1")

            .total(rateInstance()
                // default: empty name suffix
                .name("total")

                // options: noMeasurables()
                // default: the slice's measurables { COUNT, MEAN_RATE }
                .measurables(
                    COUNT,
                    MEAN_RATE, ONE_MINUTE_RATE, FIVE_MINUTES_RATE,
                    RATE_UNIT)

                // the properties specific to the metrics implementation
                // default: no properties (no overrides)
                .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_2"
                .put("key_2", "value_2_2")) // overrides "key_2" -> "value_2_1"
        .slice("byService")
            // options: disable(), enabled(boolean)
            // default: enabled
            .enable()

            // default: no predicate
            .predicate(labelValuesMatchingAll(
                SERVICE.mask("serv*_1*"),
                SERVER.predicate(s -> s.equals("server_1_1"))))

            // default: no labels
            .labels(SERVICE)

            // options: noMaxLabeledInstances()
            // default: the metric's maxLabeledInstancesPerSlice = 5
            .maxLabeledInstances(2)

            // options: notExpireLabeledInstances()
            // default: the metric's expireLabeledInstanceAfter = 25 SECONDS
            .expireLabeledInstanceAfter(42, SECONDS)

            // options: noMeasurables()
            // default: the metric's measurables { COUNT }
            .measurables(COUNT, ONE_MINUTE_RATE)

            // options: disableTotal(), noTotal(), totalEnabled(boolean)
            // default: enabled
            .enableTotal()

            // options: disableLevels(), noLevels(), levelsEnabled(boolean)
            // default: disabled
            .enableLevels()

            // the properties specific to the metrics implementation
            // default: no properties (no overrides)
            .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_1"
            .put("key_2", "value_2_1")

            .total(rateInstance()
                // default: empty name suffix
                .name("total")

                // options: noMeasurables()
                // default: the slice's measurables { COUNT, ONE_MINUTE_RATE }
                .measurables(
                    COUNT,
                    MEAN_RATE, ONE_MINUTE_RATE, FIVE_MINUTES_RATE,
                    RATE_UNIT)

                // the properties specific to the metrics implementation
                // default: no properties (no overrides)
                .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_2"
                .put("key_2", "value_2_2")) // overrides "key_2" -> "value_2_1"
);
```

### Histogram

```c.r.p.metrics.histogram.Histogram``` measures the statistical distribution of ```java.lang.Long``` values.    

```HistogramSample.java```
```java
// Default config:
//   no labels
//   measurables: {
//     COUNT,
//     MIN,
//     MAX,
//     MEAN,
//     PERCENTILE_50,
//     PERCENTILE_90,
//     PERCENTILE_99
//   }
Histogram defaultConfigHistogram = registry.histogram(withName("histogram", "defaultConfig"));

defaultConfigHistogram.update(1L);
defaultConfigHistogram.update(2L);
```

Supported measurables:  
- ```Counter.COUNT```
- ```Histogram.MIN```
- ```Histogram.MAX```
- ```Histogram.MEAN```
- ```Histogram.STANDARD_DEVIATION```
- ```Histogram.Percentile``` (including the predefined ```Histogram.PERCENTILE_5```, ```Histogram.PERCENTILE_10```, ...)
- ```Histogram.Bucket``` (including the predefined ```Histogram.MS_10_BUCKET```, ```Histogram.SEC_1_BUCKET```, ...)  

#### Histogram Config

```HistogramSample.java```
```java
Histogram fullConfigHistogram = registry.histogram(
    withName("histogram", "fullConfig"),
    () -> withHistogram()
        // options: disable(), enabled(boolean)
        // default: enabled
        .enable()

        // default: no prefix label values
        .prefix(labelValues(SAMPLE.value("histogram")))

        // default: no labels
        .labels(SERVICE, SERVER, PORT)

        // options: noExclusions()
        // default: no exclusions
        .exclude(labelValuesMatchingAny(
            SERVICE.mask("serv*2|serv*4*"),
            SERVER.mask("server_5")))

        // default: unlimited
        .maxLabeledInstancesPerSlice(5)

        // options: notExpireLabeledInstances()
        // default: no expiration
        .expireLabeledInstanceAfter(25, SECONDS)

        // options: noMeasurables()
        // default: {
        //   COUNT,
        //   MIN,
        //   MAX,
        //   MEAN,
        //   PERCENTILE_50,
        //   PERCENTILE_90,
        //   PERCENTILE_99
        // }
        .measurables(COUNT, MEAN)

        // the properties specific to the metrics implementation
        // default: no properties
        .put("key_1", "value_1_1")

        /**
         * options:
         *   - hdr() == {@link HdrHistogramImplConfigBuilder#hdr()},
         *   - scale() == {@link com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfigBuilder#scale()},
         *   - custom impl, e.g. countAndTotalSum() == {@link CountAndTotalSumScalingHistogramConfigBuilder#countAndTotalSumScaling()}.
         *     Custom impls must be registered: registry.extendWith(new CountAndTotalSumScalingHistogramConfigBuilder()).
         * default: hdr()
         */
        .impl(hdr()
            .resetByChunks(6, Duration.ofMinutes(2))
            .highestTrackableValue(1000, REDUCE_TO_HIGHEST_TRACKABLE)
            .significantDigits(3)
            .snapshotTtl(30, SECONDS))
        // .impl(countAndTotalSumScaling().factor(2)) // custom impl

        .allSlice()
            // options: disable(), enabled(boolean)
            // default: enabled
            .enable()

            // default: the metric's labels [ SERVICE, SERVER, PORT ]
            .labels(SERVICE, SERVER)

            // options: noMaxLabeledInstances()
            // default: the metric's maxLabeledInstancesPerSlice = 5
            .maxLabeledInstances(10)

            // options: notExpireLabeledInstances()
            // default: the metric's expireLabeledInstanceAfter = 25 SECONDS
            .expireLabeledInstanceAfter(42, SECONDS)

            // options: noMeasurables()
            // default: the metric's measurables { COUNT, MEAN }
            .measurables(COUNT, MEAN, MAX)

            // options: disableTotal(), noTotal(), totalEnabled(boolean)
            // default: enabled
            .enableTotal()

            // options: disableLevels(), noLevels(), levelsEnabled(boolean)
            // default: enabled
            .enableLevels()

            // the properties specific to the metrics implementation
            // default: no properties (no overrides)
            .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_1"
            .put("key_2", "value_2_1")

            .total(histogramInstance()
                // default: empty name suffix
                .name("total")

                // options: noMeasurables()
                // default: the slice's measurables { COUNT, MEAN, MAX }
                .measurables(COUNT, MEAN, PERCENTILE_95, MAX, Bucket.of(1), Bucket.of(2))

                // the properties specific to the metrics implementation
                // default: no properties (no overrides)
                .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_2"
                .put("key_2", "value_2_2")) // overrides "key_2" -> "value_2_1"
        .slice("byService")
            // options: disable(), enabled(boolean)
            // default: enabled
            .enable()

            // default: no predicate
            .predicate(labelValuesMatchingAll(
                SERVICE.mask("serv*_1*"),
                SERVER.predicate(s -> s.equals("server_1_1"))))

            // default: no labels
            .labels(SERVICE)

            // options: noMaxLabeledInstances()
            // default: the metric's maxLabeledInstancesPerSlice = 5
            .maxLabeledInstances(2)

            // options: notExpireLabeledInstances()
            // default: the metric's expireLabeledInstanceAfter = 25 SECONDS
            .expireLabeledInstanceAfter(42, SECONDS)

            // options: noMeasurables()
            // default: the metric's measurables { COUNT, MEAN }
            .measurables(
                COUNT,
                TOTAL_SUM,
                MEAN,
                PERCENTILE_50,
                PERCENTILE_95,
                MAX,
                Buckets.of(points(0, 1, 24, 25, 30, 49, 50, 55)))

            // options: disableTotal(), noTotal(), totalEnabled(boolean)
            // default: enabled
            .enableTotal()

            // options: disableLevels(), noLevels(), levelsEnabled(boolean)
            // default: disabled
            .enableLevels()

            // the properties specific to the metrics implementation
            // default: no properties (no overrides)
            .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_1"
            .put("key_2", "value_2_1")

            .total(histogramInstance()
                // default: empty name suffix
                .name("total")

                // options: noMeasurables()
                // default: the slice's measurables { COUNT, MEAN, PERCENTILE_50, PERCENTILE_95, MAX }
                .measurables(COUNT, MIN, MEAN, MAX)

                // the properties specific to the metrics implementation
                // default: no properties (no overrides)
                .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_2"
                .put("key_2", "value_2_2")) // overrides "key_2" -> "value_2_1"
);
```

### Timer

```c.r.p.metrics.timer.Timer``` measures the number of times something happens,      
the speed at which it happens (rate), and the statistical distribution of its duration.     
In fact, it combines ```Rate``` and ```Histogram```.         

Supported measurables:  
- ```Counter.COUNT```
- ```Rate.MEAN_RATE```
- ```Rate.ONE_MINUTE_RATE```
- ```Rate.FIVE_MINUTES_RATE```
- ```Rate.FIFTEEN_MINUTES_RATE```
- ```Rate.RATE_UNIT```
- ```Histogram.MIN```
- ```Histogram.MAX```
- ```Histogram.MEAN```
- ```Histogram.STANDARD_DEVIATION```
- ```Histogram.Percentile``` (including the predefined ```Histogram.PERCENTILE_5```, ```Histogram.PERCENTILE_10```, ...)
- ```Histogram.Bucket``` (including the predefined ```Histogram.MS_10_BUCKET```, ```Histogram.SEC_1_BUCKET```, ...)
- ```Timer.DURATION_UNIT```

#### Timer Config

```TimerSample.java```
```java
Timer fullConfigTimer = registry.timer(
    withName("timer", "fullConfig"),
    () -> withTimer()
        // options: disable(), enabled(boolean)
        // default: enabled
        .enable()

        // default: no prefix label values
        .prefix(labelValues(SAMPLE.value("timer")))
    
        // default: no labels
        .labels(SERVICE, SERVER, PORT)
    
        // options: noExclusions()
        // default: no exclusions
        .exclude(labelValuesMatchingAny(
            SERVICE.mask("serv*2|serv*4*"),
            SERVER.mask("server_5")))
    
        // default: unlimited
        .maxLabeledInstancesPerSlice(5)
    
        // options: notExpireLabeledInstances()
        // default: no expiration
        .expireLabeledInstanceAfter(25, SECONDS)
    
        // options: noMeasurables()
        // default: {
        //   Counter.COUNT,
        //
        //   Rate.MEAN_RATE,
        //   Rate.ONE_MINUTE_RATE,
        //   Rate.FIVE_MINUTES_RATE,
        //   Rate.FIFTEEN_MINUTES_RATE,
        //   Rate.RATE_UNIT,
        //
        //   Histogram.MIN,
        //   Histogram.MAX,
        //   Histogram.MEAN,
        //   Histogram.PERCENTILE_50,
        //   Histogram.PERCENTILE_90,
        //   Histogram.PERCENTILE_99,
        //
        //   Timer.DURATION_UNIT
        // }
        .measurables(COUNT, MEAN_RATE, MAX, MEAN)
    
        // the properties specific to the metrics implementation
        // default: no properties
        .put("key_1", "value_1_1")

        /**
         * options:
         *   - expMovingAverage() == {@link ExpMovingAverageRateImplConfigBuilder#expMovingAverage()},
         *   - custom impl, e.g. countAndMean() == {@link CountScalingRateConfigBuilder#countScaling()}.
         *     Custom impls must be registered: registry.extendWith(new CountScalingRateConfigBuilder()).
         * default: expMovingAverage()
         */
        .impl(expMovingAverage())
        // .impl(countScaling().factor(2)) // custom impl

        /**
         * options:
         *   - hdr() == {@link HdrHistogramImplConfigBuilder#hdr()},
         *   - scale() == {@link com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfigBuilder#scale()},
         *   - custom impl, e.g. countAndTotalSum() == {@link CountAndTotalSumScalingHistogramConfigBuilder#countAndTotalSumScaling()}.
         *     Custom impls must be registered: registry.extendWith(new CountAndTotalSumScalingHistogramConfigBuilder()).
         * default: hdr()
         */
        .impl(hdr()
            .resetByChunks(6, Duration.ofMinutes(2))
            .lowestDiscernibleValue(MILLISECONDS.toNanos(1))
            .highestTrackableValue(DAYS.toNanos(7), REDUCE_TO_HIGHEST_TRACKABLE)
            .significantDigits(2)
            .snapshotTtl(30, SECONDS))
        // .impl(countAndTotalSumScaling().factor(2)) // custom impl

        .allSlice()
            // options: disable(), enabled(boolean)
            // default: enabled
            .enable()
    
            // default: the metric's labels [ SERVICE, SERVER, PORT ]
            .labels(SERVICE, SERVER)
    
            // options: noMaxLabeledInstances()
            // default: the metric's maxLabeledInstancesPerSlice = 5
            .maxLabeledInstances(10)
    
            // options: notExpireLabeledInstances()
            // default: the metric's expireLabeledInstanceAfter = 25 SECONDS
            .expireLabeledInstanceAfter(42, SECONDS)
    
            // options: noMeasurables()
            // default: the metric's measurables { COUNT, MEAN_RATE, MAX, MEAN }
            .measurables(COUNT, MEAN_RATE, MAX, MEAN, PERCENTILE_50)
    
            // options: disableTotal(), noTotal(), totalEnabled(boolean)
            // default: enabled
            .enableTotal()
    
            // options: disableLevels(), noLevels(), levelsEnabled(boolean)
            // default: enabled
            .enableLevels()
    
            // the properties specific to the metrics implementation
            // default: no properties (no overrides)
            .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_1"
            .put("key_2", "value_2_1")
    
            .total(timerInstance()
                // default: empty name suffix
                .name("total")
    
                // options: noMeasurables()
                // default: the slice's measurables { COUNT, MEAN_RATE, MAX, MEAN, PERCENTILE_50 }
                .measurables(COUNT, MEAN_RATE, MAX, MEAN, PERCENTILE_50, PERCENTILE_90)
    
                // the properties specific to the metrics implementation
                // default: no properties (no overrides)
                .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_2"
                .put("key_2", "value_2_2")) // overrides "key_2" -> "value_2_1"
        .slice("byService")
            // options: disable(), enabled(boolean)
            // default: enabled
            .enable()
    
            // default: no predicate
            .predicate(labelValuesMatchingAll(
                SERVICE.mask("serv*_1*"),
                SERVER.predicate(s -> s.equals("server_1_1"))))
    
            // default: no labels
            .labels(SERVICE)
    
            // options: noMaxLabeledInstances()
            // default: the metric's maxLabeledInstancesPerSlice = 5
            .maxLabeledInstances(2)
    
            // options: notExpireLabeledInstances()
            // default: the metric's expireLabeledInstanceAfter = 25 SECONDS
            .expireLabeledInstanceAfter(42, SECONDS)
    
            // options: noMeasurables()
            // default: the metric's measurables { COUNT, MEAN_RATE, MAX, MEAN }
            .measurables(
                COUNT,
                MEAN_RATE,
                MAX,
                MEAN,
                PERCENTILE_75,
                MS_10_BUCKET,
                MS_30_BUCKET,
                MS_50_BUCKET,
                MS_75_BUCKET,
                MS_100_BUCKET,
                MS_250_BUCKET)

            // options: disableTotal(), noTotal(), totalEnabled(boolean)
            // default: enabled
            .enableTotal()
    
            // options: disableLevels(), noLevels(), levelsEnabled(boolean)
            // default: disabled
            .enableLevels()
    
            // the properties specific to the metrics implementation
            // default: no properties (no overrides)
            .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_1"
            .put("key_2", "value_2_1")
    
            .total(timerInstance()
                // default: empty name suffix
                .name("total")
    
                // options: noMeasurables()
                // default: the slice's measurables { COUNT, MEAN_RATE, MAX, MEAN, PERCENTILE_75 }
                .measurables(COUNT, MEAN_RATE, MIN, MAX, MEAN, PERCENTILE_75, PERCENTILE_90)
    
                // the properties specific to the metrics implementation
                // default: no properties (no overrides)
                .put("key_1", "value_1_2") // overrides "key_1" -> "value_1_2"
                .put("key_2", "value_2_2")) // overrides "key_2" -> "value_2_1"
);
```

### Var and CachingVar

```c.r.p.metrics.var.Var``` is an arbitrary (possibly cached) value of a specific type       
(```[Caching]ObjectVar```, ```[Caching]LongVar```, ```[Caching]DoubleVar```, ```[Caching]StringVar``` are supported)       
that can change over time.    

```VarSample.java```
```java
AtomicLong valueSupplier_1 = new AtomicLong();

// Supported var types: ObjectVar, LongVar, DoubleVar, StringVar
LongVar defaultConfigVar = registry.longVar(
    withName("var", "defaultConfig"),
    () -> valueSupplier_1.incrementAndGet());

AtomicLong valueSupplier_2 = new AtomicLong();

LongVar fullConfigVar = registry.longVar(
    withName("var", "fullConfig"),

    // options: Var.noTotal()
    () -> valueSupplier_2.incrementAndGet(),

    () -> withLongVar()
        // options: disable(), enabled(boolean)
        // default: enabled
        .enable()

        // default: no prefix label values
        .prefix(labelValues(SAMPLE.value("var")))

        .labels(SERVICE, SERVER, PORT)

        // the properties specific to the metrics implementation
        // default: no properties
        .put("key", "value"));

AtomicLong valueSupplier_3 = new AtomicLong();

fullConfigVar.register(
    () -> valueSupplier_3.incrementAndGet(),
    forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));

AtomicLong valueSupplier_4 = new AtomicLong();

fullConfigVar.register(
    () -> valueSupplier_4.incrementAndGet(),
    forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));

fullConfigVar.deregister(labelValues(
    SERVICE.value("service_1"),
    SERVER.value("server_1_1"),
    PORT.value("111")));

AtomicLong valueSupplier_5 = new AtomicLong();

// Supported caching var types:
//   CachingObjectVar,
//   CachingLongVar,
//   CachingDoubleVar,
//   CachingStringVar
CachingDoubleVar defaultConfigCachingVar = registry.cachingDoubleVar(
    withName("cachingVar", "defaultConfig"),
    () -> valueSupplier_5.incrementAndGet() + 0.5);

AtomicLong valueSupplier_6 = new AtomicLong();

CachingDoubleVar fullConfigCachingVar = registry.cachingDoubleVar(
    withName("cachingVar", "fullConfig"),

    // options: Var.noTotal()
    () -> valueSupplier_6.incrementAndGet() + 0.5,

    () -> withCachingDoubleVar()
        // options: disable(), enabled(boolean)
        // default: enabled
        .enable()

        // default: no prefix label values
        .prefix(labelValues(SAMPLE.value("var")))

        .labels(SERVICE, SERVER, PORT)

        // default: 30 SECONDS
        .ttl(10, SECONDS)

        // the properties specific to the metrics implementation
        // default: no properties
        .put("key", "value"));
```

## Metrics Reporters

Typically, metrics are used to provide runtime information about an application instance.  
In order to do this, it is necessary not only to collect metrics but also to export them to    
external monitoring systems that provide various means of visualizing and analyzing the collected metrics.    
For example, Prometheus (https://prometheus.io).  

***Metrics reporter*** *allows you to present metrics in the format of a specific monitoring system;  
some reporters additionally send metrics to an external monitoring system in the appropriate format.*

The following describes the reporters provided by the library out of the box.

### PrometheusMetricsExporter

```PrometheusMetricsExporter``` exports metrics in the Prometheus format (https://prometheus.io).

Dependencies:
```xml
<dependency>
    <groupId>com.ringcentral.platform.metrics</groupId>
    <artifactId>metrics-facade-prometheus</artifactId>
    <version>3.1.0-RELEASE</version>
</dependency>
```

Note that ```metrics-facade-prometheus``` uses
```xml
<dependency>
    <groupId>io.prometheus</groupId>
    <artifactId>simpleclient_common</artifactId>
    <version>...</version>
</dependency>
```

In case of conflict between the ```simpleclient_common``` version used in ```Metrics Facade``` and the one used in your project, you can resolve it with ```dependency.exclusions``` or any other available means. For example, see ```metrics-facade-samples/pom.xml```:
```xml
<dependency>
    <groupId>com.ringcentral.platform.metrics</groupId>
    <artifactId>metrics-facade-prometheus</artifactId>
    <version>${project.version}</version>

    <exclusions>
        <exclusion>
            <groupId>io.prometheus</groupId>
            <artifactId>simpleclient_common</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

Example:
```java
MetricRegistry registry = new DefaultMetricRegistry();
PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(registry);

Histogram h = registry.histogram(
    withName("histogram"),
    () -> withHistogram()
        .labels(SERVICE, SERVER, PORT)
        .measurables(MAX, MEAN));

h.update(1, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
h.update(2, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
h.update(3, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));

// Metric instances are added asynchronously
sleep(25); 

System.out.println(exporter.exportMetrics());
```

Output (simplified):
```bash
# TYPE histogram_max gauge
histogram_max{service="service_1",server="server_1_1",port="111",} 1.0
histogram_max{service="service_1",server="server_1_2",port="121",} 2.0
histogram_max{service="service_2",server="server_2_1",port="211",} 3.0

# TYPE histogram_mean gauge
histogram_mean{service="service_1",server="server_1_1",port="111",} 1.0
histogram_mean{service="service_1",server="server_1_2",port="121",} 2.0
histogram_mean{service="service_2",server="server_2_1",port="211",} 3.0
```

If you want to modify the export (change the names, disable some metrics or measured values, etc.),    
you need to configure the ```PrometheusMetricsExporter``` accordingly:  

```PrometheusMetricsExporterSample.java```
```java
MetricRegistry registry = new DefaultMetricRegistry();

// Default config
// PrometheusMetricsExporter prometheusMetricsExporter = new PrometheusMetricsExporter(registry);

PrometheusInstanceSampleSpecProvider miSampleSpecProvider = new PrometheusInstanceSampleSpecProvider(
    true, // exportTotalInstances. defaults to true
    false, // exportLabeledTotalInstances. defaults to false
    false); // exportLevelInstances. defaults to true

PrometheusInstanceSampleSpecModsProvider miSampleSpecModsProvider = new PrometheusInstanceSampleSpecModsProvider();

miSampleSpecModsProvider.addMod(
    forMetricInstancesMatching(
        nameMask("Histogram.**"),
        instance -> "service_2".equals(instance.valueOf(SERVICE))),
    (metric, instance, currSpec) -> instanceSampleSpec().disable());

miSampleSpecModsProvider.addMod(
    forMetricWithName("Histogram"),
    (metric, instance, currSpec) -> instanceSampleSpec()
        .name(instance.name().withNewPart(instance.valueOf(SERVICE)))
        .labelValues(currSpec.labelValuesWithout(SERVICE)));

miSampleSpecModsProvider.addMod(
    forMetricsWithNamePrefix("Histogram"),
    (metric, instance, currSpec) ->
        instanceSampleSpec().name(currSpec.name().replaceLast(currSpec.name().lastPart() + "_svc")));

PrometheusInstanceSamplesProducer miSamplesProducer = new PrometheusInstanceSamplesProducer(
    null, // totalInstanceNameSuffix. defaults to null that means no suffix
    "all"); // labeledMetricTotalInstanceNameSuffix. defaults to "all"

PrometheusSampleSpecProvider sampleSpecProvider = new PrometheusSampleSpecProvider();
PrometheusSampleSpecModsProvider sampleSpecModsProvider = new PrometheusSampleSpecModsProvider();

sampleSpecModsProvider.addMod(
    forMetricInstancesMatching(
        nameMask("Histogram.**"),
        instance -> instance instanceof HistogramInstance),
    (instanceSampleSpec, instance, measurableValues, measurable, currSpec) ->
        measurable instanceof Max ? sampleSpec().disable() : sampleSpec());

PrometheusSamplesProducer samplesProducer = new PrometheusSamplesProducer();

PrometheusInstanceSamplesProvider miSamplesProvider = new PrometheusInstanceSamplesProvider(
    miSampleSpecProvider,
    miSampleSpecModsProvider,
    miSamplesProducer,
    sampleSpecProvider,
    sampleSpecModsProvider,
    samplesProducer,
    registry);

PrometheusMetricsExporter exporter = new PrometheusMetricsExporter(
    true,
    Locale.ENGLISH,
    miSamplesProvider);

Histogram h = registry.histogram(
    withName("Histogram"),
    () -> withHistogram()
        .description("Histogram for " + PrometheusMetricsExporterSample.class.getSimpleName())
        .labels(SERVICE, SERVER, PORT)
        .measurables(MIN, MAX, MEAN));

h.update(1, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
h.update(2, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
h.update(3, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
h.update(4, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));

Timer t = registry.timer(
    withName("Timer"),
    () -> withTimer()
        .description("Timer for " + PrometheusMetricsExporterSample.class.getSimpleName())
        .labels(SERVICE, SERVER, PORT)
        .measurables(MIN, MAX, MEAN));

t.update(SECONDS.toNanos(1), forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
t.update(SECONDS.toNanos(2), forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
t.update(SECONDS.toNanos(3), forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));
```

Output:  
```bash
# HELP histogram_service_1_svc_mean Histogram for PrometheusMetricsExporterSample
# TYPE histogram_service_1_svc_mean gauge
histogram_service_1_svc_mean{server="server_1_1",port="111",} 1.5
histogram_service_1_svc_mean{server="server_1_2",port="121",} 3.0
# HELP histogram_service_1_svc_min Histogram for PrometheusMetricsExporterSample
# TYPE histogram_service_1_svc_min gauge
histogram_service_1_svc_min{server="server_1_1",port="111",} 1.0
histogram_service_1_svc_min{server="server_1_2",port="121",} 3.0
# HELP timer_max Timer for PrometheusMetricsExporterSample
# TYPE timer_max gauge
timer_max{service="service_1",server="server_1_1",port="111",} 1.002438655
timer_max{service="service_1",server="server_1_2",port="121",} 2.004877311
timer_max{service="service_2",server="server_2_1",port="211",} 3.003121663
# HELP timer_mean Timer for PrometheusMetricsExporterSample
# TYPE timer_mean gauge
timer_mean{service="service_1",server="server_1_1",port="111",} 1.000341504
timer_mean{service="service_1",server="server_1_2",port="121",} 2.000683008
timer_mean{service="service_2",server="server_2_1",port="211",} 2.9947330560000003
# HELP timer_min Timer for PrometheusMetricsExporterSample
# TYPE timer_min gauge
timer_min{service="service_1",server="server_1_1",port="111",} 0.998244352
timer_min{service="service_1",server="server_1_2",port="121",} 1.996488704
timer_min{service="service_2",server="server_2_1",port="211",} 2.986344448
```

### ZabbixMetricsJsonExporter and ZabbixLldMetricsReporter

```ZabbixMetricsJsonExporter``` exports metrics in the format that can be adapted for Zabbix.  
This is a non-standard format used in a number of RingCentral projects.    

```ZabbixLldMetricsReporter``` exports MBeans that can be used for Zabbix low-level discovery  
(https://www.zabbix.com/documentation/current/manual/discovery/low_level_discovery).  

Dependencies:
```xml
<dependency>
    <groupId>com.ringcentral.platform.metrics</groupId>
    <artifactId>metrics-facade-base</artifactId>
    <version>3.1.0-RELEASE</version>
</dependency>
```

```ZabbixMetricsJsonExporter``` and ```ZabbixLldMetricsReporter``` are best explained with:  
```ZabbixReportersSample.java```
```java
MetricRegistry registry = new DefaultMetricRegistry();
DefaultInstanceSampleSpecModsProvider miSampleSpecModsProvider = new DefaultInstanceSampleSpecModsProvider();

miSampleSpecModsProvider.addMod(
    forMetricInstancesMatching(
        nameMask("histogram.**"),
        instance -> "service_2".equals(instance.valueOf(SERVICE))),
    (metric, instance, currSpec) -> instanceSampleSpec().disable());

miSampleSpecModsProvider.addMod(
    forMetricWithName("histogram"),
    (metric, instance, currSpec) -> instanceSampleSpec().name(instance.name().withNewPart("test")));

DefaultSampleSpecModsProvider sampleSpecModsProvider = new DefaultSampleSpecModsProvider();

sampleSpecModsProvider.addMod(
    forMetricInstancesMatching(
        nameMask("histogram.**"),
        instance -> instance instanceof HistogramInstance),
    (instanceSampleSpec, instance, measurableValues, measurable, currSpec) ->
        measurable instanceof Max ? sampleSpec().disable() : sampleSpec());

DefaultInstanceSamplesProvider miSamplesProvider = new DefaultInstanceSamplesProvider(
    miSampleSpecModsProvider,
    sampleSpecModsProvider,
    new DefaultSampleSpecProvider(CustomMeasurableNameProvider.INSTANCE),
    registry);

ZabbixMetricsJsonExporter exporter = new ZabbixMetricsJsonExporter(miSamplesProvider);

// LLD
ZGroupMBeansExporter zGroupMBeansExporter = new ZGroupMBeansExporter(
    "zabbixReportersSample.zabbix.lld:type=",
    DefaultZGroupJsonMapper.INSTANCE,
    "JsonData");

zGroupMBeansExporter.ensureGroup("server");
ZabbixLldMetricsReporter lldReporter = new ZabbixLldMetricsReporter(zGroupMBeansExporter);

lldReporter.addRules(
    forMetricInstancesMatching(nameMask("histogram.**")),
    new Rule(
        "service",
        List.of(new RuleItem(SERVICE, "service"), new RuleItem(SERVER, "server"))));

lldReporter.addRules(
    forMetricInstancesMatching(nameMask("histogram.**")),
    new Rule(
        "server",
        List.of(new RuleItem(i -> i.valueOf(SERVICE) + "/" + i.valueOf(SERVER), "server"))));

registry.addListener(lldReporter);

// Metrics
Histogram h = registry.histogram(
    withName("histogram"),
    () -> withHistogram()
        .labels(SERVICE, SERVER, PORT)
        .measurables(COUNT, MAX, MEAN, Buckets.of(scale())));

h.update(1, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
h.update(2, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
h.update(3, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));

...

static ScaleBuilder<?> scale() {
    return
        // 100 ms
        first(linear().steps(25, MILLISECONDS, 4))
        // 500 ms
        .then(linear().steps(100, MILLISECONDS, 4).withInf());
}
```

```exporter.exportMetrics()``` as JSON:  
```json
{
  "delta": [
    {
      "timer.test.count": 3
    },
    {
      "timer.test.service_1.count": 2
    },
    {
      "timer.test.service_1.server_1_1.111.count": 1
    },
    {
      "timer.test.service_1.server_1_1.count": 1
    },
    {
      "timer.test.service_1.server_1_2.121.count": 1
    },
    {
      "timer.test.service_1.server_1_2.count": 1
    }
  ],
  "instant": [
    {
      "timer.test.duration.0ms_bucket": 0
    },
    {
      "timer.test.duration.100ms_bucket": 3
    },
    {
      "timer.test.duration.200ms_bucket": 3
    },
    {
      "timer.test.duration.25ms_bucket": 3
    },
    {
      "timer.test.duration.300ms_bucket": 3
    },
    {
      "timer.test.duration.400ms_bucket": 3
    },
    {
      "timer.test.duration.500ms_bucket": 3
    },
    {
      "timer.test.duration.50ms_bucket": 3
    },
    {
      "timer.test.duration.75ms_bucket": 3
    },
    {
      "timer.test.duration.inf_bucket": 3
    },
    {
      "timer.test.duration.mean": 2.0E-6
    },
    {
      "timer.test.service_1.duration.0ms_bucket": 0
    },
    {
      "timer.test.service_1.duration.100ms_bucket": 2
    },
    {
      "timer.test.service_1.duration.200ms_bucket": 2
    },
    {
      "timer.test.service_1.duration.25ms_bucket": 2
    },
    {
      "timer.test.service_1.duration.300ms_bucket": 2
    },
    {
      "timer.test.service_1.duration.400ms_bucket": 2
    },
    {
      "timer.test.service_1.duration.500ms_bucket": 2
    },
    {
      "timer.test.service_1.duration.50ms_bucket": 2
    },
    {
      "timer.test.service_1.duration.75ms_bucket": 2
    },
    {
      "timer.test.service_1.duration.inf_bucket": 2
    },
    {
      "timer.test.service_1.duration.mean": 1.5E-6
    },
    {
      "timer.test.service_1.server_1_1.111.duration.0ms_bucket": 0
    },
    {
      "timer.test.service_1.server_1_1.111.duration.100ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.111.duration.200ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.111.duration.25ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.111.duration.300ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.111.duration.400ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.111.duration.500ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.111.duration.50ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.111.duration.75ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.111.duration.inf_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.111.duration.mean": 1.0E-6
    },
    {
      "timer.test.service_1.server_1_1.duration.0ms_bucket": 0
    },
    {
      "timer.test.service_1.server_1_1.duration.100ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.duration.200ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.duration.25ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.duration.300ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.duration.400ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.duration.500ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.duration.50ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.duration.75ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.duration.inf_bucket": 1
    },
    {
      "timer.test.service_1.server_1_1.duration.mean": 1.0E-6
    },
    {
      "timer.test.service_1.server_1_2.121.duration.0ms_bucket": 0
    },
    {
      "timer.test.service_1.server_1_2.121.duration.100ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.121.duration.200ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.121.duration.25ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.121.duration.300ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.121.duration.400ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.121.duration.500ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.121.duration.50ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.121.duration.75ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.121.duration.inf_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.121.duration.mean": 2.0E-6
    },
    {
      "timer.test.service_1.server_1_2.duration.0ms_bucket": 0
    },
    {
      "timer.test.service_1.server_1_2.duration.100ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.duration.200ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.duration.25ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.duration.300ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.duration.400ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.duration.500ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.duration.50ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.duration.75ms_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.duration.inf_bucket": 1
    },
    {
      "timer.test.service_1.server_1_2.duration.mean": 2.0E-6
    }
  ]
}
```

JMX MBean attribute ```zabbixReportersSample.zabbix.lld:type=service.JsonData```:
```json
{
  "data": [
    {
      "{#SERVICE}": "service_1",
      "{#SERVER}": "server_1_1"
    },
    {
      "{#SERVICE}": "service_1",
      "{#SERVER}": "server_1_2"
    },
    {
      "{#SERVICE}": "service_2",
      "{#SERVER}": "server_2_1"
    }
  ]
}
```

JMX MBean attribute ```zabbixReportersSample.zabbix.lld:type=server.JsonData```:
```json
{
  "data": [
    {
      "{#SERVER}": "service_1/server_1_1"
    },
    {
      "{#SERVER}": "service_1/server_1_2"
    },
    {
      "{#SERVER}": "service_2/server_2_1"
    }
  ]
}
```

### TelegrafMetricsJsonExporter

```TelegrafMetricsJsonExporter``` exports metrics in the Telegraf format (https://github.com/influxdata/telegraf).

Dependencies:
```xml
<dependency>
    <groupId>com.ringcentral.platform.metrics</groupId>
    <artifactId>metrics-facade-base</artifactId>
    <version>3.1.0-RELEASE</version>
</dependency>
```

```TelegrafMetricsJsonExporter``` is best explained with:  
```TelegrafMetricsJsonExporterSample.java```
```java
MetricRegistry registry = new DefaultMetricRegistry();
DefaultInstanceSampleSpecModsProvider miSampleSpecModsProvider = new DefaultInstanceSampleSpecModsProvider();

miSampleSpecModsProvider.addMod(
    forMetricInstancesMatching(
        nameMask("histogram.**"),
        instance -> "service_2".equals(instance.valueOf(SERVICE))),
    (metric, instance, currSpec) -> instanceSampleSpec().disable());

miSampleSpecModsProvider.addMod(
    forMetricWithName("histogram"),
    (metric, instance, currSpec) -> instanceSampleSpec().name(instance.name().withNewPart("test")));

DefaultSampleSpecModsProvider sampleSpecModsProvider = new DefaultSampleSpecModsProvider();

sampleSpecModsProvider.addMod(
    forMetricInstancesMatching(
        nameMask("histogram.**"),
        instance -> instance instanceof HistogramInstance),
    (instanceSampleSpec, instance, measurableValues, measurable, currSpec) ->
        measurable instanceof Max ? sampleSpec().disable() : sampleSpec());

DefaultInstanceSamplesProvider miSamplesProvider = new DefaultInstanceSamplesProvider(
    miSampleSpecModsProvider,
    sampleSpecModsProvider,
    new DefaultSampleSpecProvider(CustomMeasurableNameProvider.INSTANCE),
    registry);

// Metrics
Histogram h = registry.histogram(
    withName("histogram"),
    () -> withHistogram()
        .labels(SERVICE, SERVER, PORT)
        .measurables(COUNT, MAX, MEAN));

h.update(1, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
h.update(2, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
h.update(3, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));
```

```exporter.exportMetrics()``` as JSON (without grouping by types):
```json
{
  "histogram.test.mean": 2.0,
  "histogram.test.count": 3,
  "histogram.test.service_1.server_1_1.111.mean": 1.0,
  "histogram.test.service_1.server_1_1.111.count": 1,
  "histogram.test.service_1.server_1_2.121.mean": 2.0,
  "histogram.test.service_1.server_1_2.121.count": 1,
  "histogram.test.service_1.mean": 1.5,
  "histogram.test.service_1.count": 2,
  "histogram.test.service_1.server_1_2.mean": 2.0,
  "histogram.test.service_1.server_1_2.count": 1,
  "histogram.test.service_1.server_1_1.mean": 1.0,
  "histogram.test.service_1.server_1_1.count": 1
}
```

```exporter.exportMetrics()``` as JSON (with grouping by types - a non-standard format):
```json
{
  "instant": {
    "histogram.test.mean": 2.0,
    "histogram.test.service_1.server_1_1.111.mean": 1.0,
    "histogram.test.service_1.server_1_2.121.mean": 2.0,
    "histogram.test.service_1.mean": 1.5,
    "histogram.test.service_1.server_1_2.mean": 2.0,
    "histogram.test.service_1.server_1_1.mean": 1.0
  },
  "delta": {
    "histogram.test.count": 3,
    "histogram.test.service_1.server_1_1.111.count": 1,
    "histogram.test.service_1.server_1_2.121.count": 1,
    "histogram.test.service_1.count": 2,
    "histogram.test.service_1.server_1_2.count": 1,
    "histogram.test.service_1.server_1_1.count": 1
  }
}
```

### JmxMetricsReporter

```JmxMetricsReporter``` exports MBeans for the corresponding metric instances.  

Dependencies:
```xml
<dependency>
    <groupId>com.ringcentral.platform.metrics</groupId>
    <artifactId>metrics-facade-base</artifactId>
    <version>3.1.0-RELEASE</version>
</dependency>
```

```JmxMetricsReporter``` is best explained with:  
```JmxMetricsReporterSample.java```
```java
MetricRegistry registry = new DefaultMetricRegistry();

// Default config
// registry.addListener(new JmxMetricsReporter());

MaskTreeMetricNamedInfoProvider<MBeanSpecProvider> mBeanSpecs = new MaskTreeMetricNamedInfoProvider<>();

mBeanSpecs.addInfo(
    forMetricInstancesMatching(
        nameMask("rate.**"),
        instance -> "service_2".equals(instance.valueOf(SERVICE))),
    instance -> mBeanSpec().disable());

mBeanSpecs.addInfo(
    forMetricsWithNamePrefix("rate"),
    instance -> mBeanSpec()
        .name(
            instance.isTotalInstance() ?
            instance.name() :
            instance.name().withNewPart(instance.valueOf(SERVICE)))
        .labelValues(instance.labelValuesWithout(SERVICE)));

JmxMetricsReporter jmxReporter = new JmxMetricsReporter(
    mBeanSpecs,
    getPlatformMBeanServer(),
    new DefaultObjectNameProvider(),
    new CustomMeasurableNameProvider(),
    "JmxMetricsReporterSample");

registry.addListener(jmxReporter);

Rate r = registry.rate(
    withName("rate"),
    () -> withRate()
        .labels(SERVICE, SERVER, PORT)
        .measurables(COUNT, MEAN_RATE, ONE_MINUTE_RATE));

r.mark(1, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_1"), PORT.value("111")));
r.mark(2, forLabelValues(SERVICE.value("service_1"), SERVER.value("server_1_2"), PORT.value("121")));
r.mark(3, forLabelValues(SERVICE.value("service_2"), SERVER.value("server_2_1"), PORT.value("211")));
```

JMX MBean attribute ```JmxMetricsReporterSample:name=rate.service_1,server=server_1_2,port=121.count = 2```

## Metrics Producers

***Metric producer*** *adds some predefined metrics to a registry*.    
Metrics producers are represented by subclasses of ```MetricsProducer```.   

```MetricsProducersSample.java```  
```java
MetricRegistry registry = new DefaultMetricRegistry();

// adds some system metrics
new SystemMetricsProducer().produceMetrics(registry);
```

### SystemMetricsProducer

```SystemMetricsProducer``` just combines:
- ```RuntimeMetricsProducer```
- ```OperatingSystemMetricsProducer```
- ```GarbageCollectorsMetricsProducer```
- ```MemoryMetricsProducer```
- ```ThreadsMetricsProducer```
- ```BufferPoolsMetricsProducer```
- ```ClassesMetricsProducer```

### RuntimeMetricsProducer

Adds a number of metrics related to the JVM.<br>
See [RuntimeMetricsProducer](metrics-facade-base/src/main/java/com/ringcentral/platform/metrics/producers/RuntimeMetricsProducer.java) for details.<br>
It is based on ```java.lang.management.RuntimeMXBean```.

### OperatingSystemMetricsProducer

Adds a number of metrics related to operating system:
OS name, OS architecture, CPU time used by the process, etc.<br>
See [DefaultOperatingSystemMetricsProducer](metrics-facade-base/src/main/java/com/ringcentral/platform/metrics/producers/nonlabeled/DefaultOperatingSystemMetricsProducer.java) and [LabeledOperatingSystemMetricsProducer](metrics-facade-base/src/main/java/com/ringcentral/platform/metrics/producers/labeled/LabeledOperatingSystemMetricsProducer.java) for details.<br>It is based on ```com.sun.management.OperatingSystemMXBean```.  

### GarbageCollectorsMetricsProducer

Adds a number of metrics related to garbage collection:        
the total number of collections that have occurred, the approximate accumulated collection elapsed time, etc.<br>
See [DefaultGarbageCollectorsMetricsProducer](metrics-facade-base/src/main/java/com/ringcentral/platform/metrics/producers/nonlabeled/DefaultGarbageCollectorsMetricsProducer.java) and [LabeledGarbageCollectorsMetricsProducer](metrics-facade-base/src/main/java/com/ringcentral/platform/metrics/producers/labeled/LabeledGarbageCollectorsMetricsProducer.java) for details.<br> It is based on ```java.lang.management.GarbageCollectorMetricSet```.      

### MemoryMetricsProducer

Adds a number of metrics related to memory:  
heap memory usage, non-heap memory usage, etc.<br>
See [DefaultMemoryMetricsProducer](metrics-facade-base/src/main/java/com/ringcentral/platform/metrics/producers/nonlabeled/DefaultMemoryMetricsProducer.java) and [LabeledMemoryMetricsProducer](metrics-facade-base/src/main/java/com/ringcentral/platform/metrics/producers/labeled/LabeledMemoryMetricsProducer.java) for details.<br> It is based on ```java.lang.management.MemoryMXBean```.    

### ThreadsMetricsProducer

Adds a number of metrics related to threads:    
the current number of live threads, the total number of threads created and also started since the JVM started, etc.<br>
See [DefaultThreadsMetricsProducer](metrics-facade-base/src/main/java/com/ringcentral/platform/metrics/producers/nonlabeled/DefaultThreadsMetricsProducer.java) and [LabeledThreadsMetricsProducer](metrics-facade-base/src/main/java/com/ringcentral/platform/metrics/producers/labeled/LabeledThreadsMetricsProducer.java) for details.<br> It is based on ```java.lang.management.ThreadMXBean```      

### BufferPoolsMetricsProducer

Adds a number of metrics related to buffer pools.<br>
See [DefaultBufferPoolsMetricsProducer](metrics-facade-base/src/main/java/com/ringcentral/platform/metrics/producers/nonlabeled/DefaultBufferPoolsMetricsProducer.java) and [LabeledBufferPoolsMetricsProducer](metrics-facade-base/src/main/java/com/ringcentral/platform/metrics/producers/labeled/LabeledBufferPoolsMetricsProducer.java) for details.<br> It is based on the MBeans ```java.nio:type=BufferPool,name=<pool_name>```.      

### ClassesMetricsProducer

Adds a number of metrics related to the class loading system of the JVM.
See [ClassesMetricsProducer](metrics-facade-base/src/main/java/com/ringcentral/platform/metrics/producers/ClassesMetricsProducer.java) for details.<br>
It is based on ```java.lang.management.ClassLoadingMXBean```.

## Integration

### Dropwizard MetricRegistry integration

#### Export using DropwizardMetricRegistryDefaultInstanceSamplesProvider

`DropwizardMetricRegistryDefaultInstanceSamplesProvider` helps to export metrics from existing `com.codahale.metrics.MetricRegistry` using [Metrics Reporters](#metrics-reporters) (except PrometheusMetricsExporter).

For example,
```java
MetricRegistry registry = new MetricRegistry();

AtomicInteger g1 = new AtomicInteger();
registry.gauge("g1", () -> new Gauge<Integer>() {
  @Override
  public Integer getValue() {
    return g1.incrementAndGet();
  }
});

Counter c1 = registry.counter("c1");
c1.inc();

Histogram h1 = registry.histogram("h1");
h1.update(10);
h1.update(100);
h1.update(1000);

Timer t1 = registry.timer("t1");
t1.update(Duration.ofMinutes(10));

Meter m1 = registry.meter("m1");
m1.mark();
m1.mark(10);

DropwizardMetricRegistryDefaultInstanceSamplesProvider provider = new DropwizardMetricRegistryDefaultInstanceSamplesProvider(registry);

TelegrafMetricsJsonExporter telegrafMetricsJsonExporter = new TelegrafMetricsJsonExporter(true, provider);
MetricsJson metrics = telegrafMetricsJsonExporter.exportMetrics();

ObjectMapper mapper = new ObjectMapper();
System.out.println(mapper.writeValueAsString(metrics));
```
Output:
```json
{"instant":{"g1":1.0,"h1.mean":370.0,"h1.max":1000,"h1.min":10,"h1.median":100.0,"h1.std_dev":446.9899327725402,"h1.75_percentile":1000.0,"h1.95_percentile":1000.0,"h1.98_percentile":1000.0,"h1.99_percentile":1000.0,"h1.999_percentile":1000.0,"t1.mean":600.0,"t1.max":600.0,"t1.min":600.0,"t1.median":600.0,"t1.std_dev":0.0,"t1.75_percentile":600.0,"t1.95_percentile":600.0,"t1.98_percentile":600.0,"t1.99_percentile":600.0,"t1.999_percentile":600.0,"m1.1_minute_rate":0.0,"m1.5_minute_rate":0.0,"m1.15_minute_rate":0.0,"m1.mean_rate":288.9463950588065},"delta":{"c1":1,"h1.count":3,"t1.count":1,"m1.total":11}}
```
Required dependency:
```xml
<dependency>
  <groupId>com.ringcentral.platform.metrics</groupId>
  <artifactId>metrics-facade-dropwizard</artifactId>
  <version>3.1.0-RELEASE</version>
</dependency>
```

#### Export using DropwizardMetricRegistryPrometheusInstanceSamplesProvider

`DropwizardMetricRegistryPrometheusInstanceSamplesProvider` helps to export metrics from existing `com.codahale.metrics.MetricRegistry` using PrometheusMetricsExporter.

For example,
```java
MetricRegistry registry = new MetricRegistry();

AtomicInteger g1 = new AtomicInteger();
registry.gauge("g1", () -> new Gauge<Integer>() {
  @Override
  public Integer getValue() {
    return g1.incrementAndGet();
  }
});

Counter c1 = registry.counter("c1");
c1.inc();

Histogram h1 = registry.histogram("h1");
h1.update(10);
h1.update(100);
h1.update(1000);

Timer t1 = registry.timer("t1");
t1.update(Duration.ofMinutes(10));

Meter m1 = registry.meter("m1");
m1.mark();
m1.mark(10);

DropwizardMetricRegistryDefaultInstanceSamplesProvider provider = new DropwizardMetricRegistryDefaultInstanceSamplesProvider(registry);

TelegrafMetricsJsonExporter telegrafMetricsJsonExporter = new TelegrafMetricsJsonExporter(true, provider);
MetricsJson metrics = telegrafMetricsJsonExporter.exportMetrics();

System.out.println(metrics);
```
Output:
```bash
# HELP h1 Generated from Dropwizard metric import (metric=h1, type=com.codahale.metrics.Histogram)
# TYPE h1 summary
h1{quantile="0.5",} 100.0
h1{quantile="0.75",} 1000.0
h1{quantile="0.95",} 1000.0
h1{quantile="0.98",} 1000.0
h1{quantile="0.99",} 1000.0
h1{quantile="0.999",} 1000.0
h1_count 3.0
# HELP g1 Generated from Dropwizard metric import (metric=g1, type=com.ringcentral.platform.metrics.samples.temp.DropwizardMetricRegistryPrometheusInstanceSamplesProviderSample$1)
# TYPE g1 gauge
g1 0.0
# HELP t1 Generated from Dropwizard metric import (metric=t1, type=com.codahale.metrics.Timer)
# TYPE t1 summary
t1{quantile="0.5",} 600.0
t1{quantile="0.75",} 600.0
t1{quantile="0.95",} 600.0
t1{quantile="0.98",} 600.0
t1{quantile="0.99",} 600.0
t1{quantile="0.999",} 600.0
t1_count 1.0
# HELP c1 Generated from Dropwizard metric import (metric=c1, type=com.codahale.metrics.Counter)
# TYPE c1 gauge
c1 1.0
```

Required dependency:
```xml
<dependency>
  <groupId>com.ringcentral.platform.metrics</groupId>
  <artifactId>metrics-facade-dropwizard-to-prometheus</artifactId>
  <version>3.1.0-RELEASE</version>
</dependency>
```

## License

MIT    