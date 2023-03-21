package com.ringcentral.platform.metrics.reporters.jmx;

import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.names.MetricName;
import org.junit.Test;

import javax.management.ObjectName;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DefaultObjectNameProviderTest {

    static final String DOMAIN_NAME = "metrics";

    static final Label LABEL_1 = new Label("label_1");
    static final Label LABEL_2 = new Label("label_2");

    DefaultObjectNameProvider objectNameProvider = new DefaultObjectNameProvider();

    @Test
    public void escaping() {
        ObjectName objectName = objectNameProvider.objectNameFor(
            DOMAIN_NAME,
            MetricName.of("aa \t\\  *?,=:\\aa.b"),
            List.of(LABEL_1.value("l_1_value"), LABEL_2.value("aa \t\\  *?,=:\\aa")));

        assertThat(objectName.toString(), is("metrics:name=aa___________aa.b,label_1=l_1_value,label_2=aa___________aa"));
    }
}