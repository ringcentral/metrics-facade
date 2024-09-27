package com.ringcentral.platform.metrics.samples.instrument.executors;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistryBuilder;
import com.ringcentral.platform.metrics.samples.AbstractSample;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ringcentral.platform.metrics.instrument.executors.MonitoredExecutorServiceBuilder.monitoredExecutorService;
import static com.ringcentral.platform.metrics.labels.LabelValues.labelValues;
import static com.ringcentral.platform.metrics.names.MetricName.name;

public class MonitoredExecutorServiceSample extends AbstractSample {

    public static void main(String[] args) throws Exception {
        DefaultMetricRegistry registry = new DefaultMetricRegistryBuilder().build();

        // Example 1: Default naming (based in a sequence of positive integer number)
        // executor_service_submitted_total{name="1",} 6.0
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ExecutorService monitoredExecutor = monitoredExecutorService(executor, registry).build();
        useExecutor(monitoredExecutor, "1");

        // Example 2: Custom name
        // executor_service_submitted_total{name="customName",} 6.0
        executor = Executors.newSingleThreadExecutor();
        monitoredExecutor = monitoredExecutorService(executor, registry).name("customName").build();
        useExecutor(monitoredExecutor, "customName");

        // Example 3: Exclude name from labels and make it a part of the name
        // executor_service_nameNotLabel_submitted_total 6.0
        executor = Executors.newSingleThreadExecutor();
        monitoredExecutor = monitoredExecutorService(executor, registry).name("nameNotLabel").nameAsLabel(false).build();
        useExecutor(monitoredExecutor, "nameNotLabel");

        // Example 4: Custom name prefix and additional labels
        // customMetricNamePrefix_submitted_total{sample="MonitoredExecutorService",service="myService",name="customMetricNamePrefixAndAdditionalLabels",} 6.0
        executor = Executors.newSingleThreadExecutor();

        monitoredExecutor = monitoredExecutorService(executor, registry)
            .name("customMetricNamePrefixAndAdditionalLabels")
            .metricNamePrefix(name("customMetricNamePrefix"))
            .prefixLabelValues(labelValues(SAMPLE.value("MonitoredExecutorService")))
            .additionalLabelValues(labelValues(SERVICE.value("myService")))
            .build();

        useExecutor(monitoredExecutor, "customMetricNamePrefixAndAdditionalLabels");

        export(registry);
        hang();
    }

    private static void useExecutor(ExecutorService executor, String name) {
        for (int i = 1; i <= 3; ++i) {
            int j = i;
            executor.execute(() -> { sleep(j); System.out.println(name + "_" + j); });
            executor.submit(() -> { sleep(j); System.out.println(name + "_" + j); });
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
