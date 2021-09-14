package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.names.MetricName;

public class MetricCollisionException extends RuntimeException {

    public MetricCollisionException(MetricName metricName, String existingMetricType, String newMetricType) {
        super(
            "Metric collision: " +
            "metric name = '" + metricName + "', " +
            "existing metric type = " + existingMetricType + ", " +
            "new metric type = " + newMetricType);
    }
}
