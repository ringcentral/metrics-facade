package com.ringcentral.platform.metrics.articles.intro;

import com.ringcentral.platform.metrics.defaultImpl.DefaultMetricRegistry;
import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.measurables.MeasurableValues;

import java.util.List;

import static com.ringcentral.platform.metrics.histogram.configs.builders.HistogramConfigBuilder.withHistogram;
import static com.ringcentral.platform.metrics.labels.LabelValues.forLabelValues;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static java.lang.Thread.sleep;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class Listing06 {

    public static void main(String[] args) throws InterruptedException {
        // 1) Create registry
        var registry = new DefaultMetricRegistry();

        // 2) Define labels. Typically, we define labels as constants, but in this example, we deviate from that convention for the sake of brevity.
        var service = new Label("service");
        var server = new Label("server");

        Histogram httpClientFailoverCountHistogram = registry.histogram(
            withName("histogram"),
            () -> withHistogram()
                .labels(service, server)
                // Slices and levels will be descrobed in the next session
                .allSlice().noLevels());

        // 3) Update the total instance and the instance (auth, 127.0.0.1)
        httpClientFailoverCountHistogram.update(10, forLabelValues(service.value("auth"), server.value("127.0.0.1")));

        // 4) Update the total instance and the instance (auth, 127.0.0.2)
        httpClientFailoverCountHistogram.update(20, forLabelValues(service.value("auth"), server.value("127.0.0.2")));

        // 5) Update the total instance and the instance (contacts, 127.0.0.3)
        httpClientFailoverCountHistogram.update(30, forLabelValues(service.value("contacts"), server.value("127.0.0.3")));

        // 6) Update the total instance and the instance (contacts, 127.0.0.4)
        httpClientFailoverCountHistogram.update(40, forLabelValues(service.value("contacts"), server.value("127.0.0.4")));

        // Labeled metric instances are added asynchronously.
        sleep(100);

        httpClientFailoverCountHistogram.forEach(instance -> {
            List<String> labelValuesString = instance.labelValues().stream()
                .map(lv -> lv.label().name() + "=" + lv.value())
                .collect(toList());

            // Snapshot-based approach for getting values
            MeasurableValues values = instance.measurableValues();

            String valuesString = "{" + instance.measurables().stream()
                .sorted(comparing(m -> m.getClass().getName()))
                .map(m -> {
                    String name =
                        m instanceof Histogram.Percentile ?
                            "Percentile_" + ((Histogram.Percentile)m).quantileAsString() :
                            m.getClass().getSimpleName();

                    // or instance.valueOf(m) but this approach is not snapshot-based, e.i., is not consistent, and should not be used unnecessarily
                    return name + "=" + values.valueOf(m);
                })
                .collect(joining(", ")) + "}";

            System.out.println(
                "Metric instance:\n"
                + "  label values = " + labelValuesString + ",\n"
                + "  total instance = " + instance.isTotalInstance() + ",\n"
                + "  measurable values = " + valuesString);
        });
    }
}
