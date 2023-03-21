package com.ringcentral.platform.metrics.reporters.jmx;

import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.names.MetricName;
import org.slf4j.Logger;

import javax.management.*;
import java.util.*;

import static java.lang.String.join;
import static org.slf4j.LoggerFactory.getLogger;

public class DefaultObjectNameProvider implements ObjectNameProvider {

    public static final DefaultObjectNameProvider INSTANCE = new DefaultObjectNameProvider();
    private static final Logger logger = getLogger(DefaultObjectNameProvider.class);

    @Override
    public ObjectName objectNameFor(
        String domainName,
        MetricName name,
        List<LabelValue> labelValues) {

        try {
            ObjectName objectName;
            String namePropertyValue = escape(join(".", name));

            if (labelValues == null || labelValues.isEmpty()) {
                objectName = new ObjectName(domainName, "name", namePropertyValue);
            } else {
                Set<String> names = new HashSet<>();
                StringBuilder builder = new StringBuilder(domainName).append(":name=").append(namePropertyValue);

                for (LabelValue lv : labelValues) {
                    String labelName = lv.label().name();

                    if (!names.contains(labelName)) {
                        builder.append(',').append(escapeLabelName(labelName)).append('=').append(escape(lv.value()));
                        names.add(labelName);
                    } else {
                        logger.warn("Ignoring duplicate label name: {}", labelName);
                    }
                }

                objectName = new ObjectName(builder.toString());
            }

            return objectName;
        } catch (MalformedObjectNameException e) {
            logger.warn(
                "Failed to create ObjectName: domain name = '{}', metric name = '{}'",
                domainName, name, e);

            throw new RuntimeException(e);
        }
    }

    /**
     * https://docs.oracle.com/javase/7/docs/api/javax/management/ObjectName.html
     */
    private String escape(String v) {
        return v.replaceAll("[\\s*?,=:\\\\]", "_");
    }

    private String escapeLabelName(String labelName) {
        if (labelName.equals("name")) {
            return "_name";
        }

        return escape(labelName);
    }
}
