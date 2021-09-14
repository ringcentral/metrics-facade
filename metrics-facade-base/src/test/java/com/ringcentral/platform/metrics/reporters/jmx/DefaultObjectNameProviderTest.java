package com.ringcentral.platform.metrics.reporters.jmx;

import com.ringcentral.platform.metrics.dimensions.MetricDimension;
import com.ringcentral.platform.metrics.names.MetricName;
import org.junit.Test;

import javax.management.ObjectName;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DefaultObjectNameProviderTest {

    static final String DOMAIN_NAME = "metrics";

    static final MetricDimension DIMENSION_1 = new MetricDimension("dimension_1");
    static final MetricDimension DIMENSION_2 = new MetricDimension("dimension_2");

    DefaultObjectNameProvider objectNameProvider = new DefaultObjectNameProvider();

    @Test
    public void escaping() {
        ObjectName objectName = objectNameProvider.objectNameFor(
            DOMAIN_NAME,
            MetricName.of("aa \t\\  *?,=:\\aa.b"),
            List.of(DIMENSION_1.value("d_1_value"), DIMENSION_2.value("aa \t\\  *?,=:\\aa")));

        assertThat(objectName.toString(), is("metrics:name=aa___________aa.b,dimension_1=d_1_value,dimension_2=aa___________aa"));
    }
}