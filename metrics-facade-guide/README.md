# Getting started

## Setup

To use lib you need to declare dependencies.

<details>
<summary>Maven</summary>

Base (Core):
```xml
<dependency>
    <groupId>com.ringcentral.platform.metrics</groupId>
    <artifactId>metrics-facade-base</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
```

```MetricRegistry``` implementation (for example, ```DefaultMetricRegistry```):
```xml
<dependency>
    <groupId>com.ringcentral.platform.metrics</groupId>
    <artifactId>metrics-facade-default-impl</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
```

Metrics reporter(s) (for example, ```PrometheusMetricsExporter```):
```xml
<dependency>
    <groupId>com.ringcentral.platform.metrics</groupId>
    <artifactId>metrics-facade-prometheus</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
```

[example of pom.xml](examples/chapter-01/pom.xml)

</details>

<details>
<summary>Gradle</summary>
Base (Core):

```groovy
implementation("com.ringcentral.platform.metrics:metrics-facade-base:3.0.0-SNAPSHOT")
```

```MetricRegistry``` implementation (for example, ```DefaultMetricRegistry```):
```groovy
implementation("com.ringcentral.platform.metrics:metrics-facade-default-impl:3.0.0-SNAPSHOT")
```

Metrics reporter(s) (for example, ```PrometheusMetricsExporter```):
```groovy
    implementation("com.ringcentral.platform.metrics:metrics-facade-prometheus:3.0.0-SNAPSHOT")
```
</details>

## Table of Contents
* Overview
* Basics
  * [First metric creation and export](./chapters/01-first-creation-and-export.md)
  * [Metric types and Measurables](./chapters/02-metric-types.md)
  * [Labeled metrics](./chapters/03-labeled-metrics.md)
  * Configuration
  * Exporters
* Advanced
  * Metric Instance
  * Slices and Levels
  * Reporters
  * Producers
  * Spring Integration
