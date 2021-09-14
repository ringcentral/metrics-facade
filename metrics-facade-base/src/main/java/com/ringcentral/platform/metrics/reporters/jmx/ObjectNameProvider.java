package com.ringcentral.platform.metrics.reporters.jmx;

import java.util.List;
import javax.management.ObjectName;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.names.MetricName;

public interface ObjectNameProvider {
    ObjectName objectNameFor(
        String domainName,
        MetricName name,
        List<MetricDimensionValue> dimensionValues);
}
