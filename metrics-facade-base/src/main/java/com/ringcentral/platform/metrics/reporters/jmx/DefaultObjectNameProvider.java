package com.ringcentral.platform.metrics.reporters.jmx;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.management.*;
import org.slf4j.Logger;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.names.MetricName;
import static java.lang.String.*;
import static org.slf4j.LoggerFactory.*;

public class DefaultObjectNameProvider implements ObjectNameProvider {

    public static final DefaultObjectNameProvider INSTANCE = new DefaultObjectNameProvider();
    private static final Logger logger = getLogger(DefaultObjectNameProvider.class);

    @Override
    public ObjectName objectNameFor(
        String domainName,
        MetricName name,
        List<MetricDimensionValue> dimensionValues) {

        try {
            ObjectName objectName;
            String namePropertyValue = escape(join(".", name));

            if (dimensionValues == null || dimensionValues.isEmpty()) {
                objectName = new ObjectName(domainName, "name", namePropertyValue);
            } else {
                Set<String> names = new HashSet<>();
                StringBuilder builder = new StringBuilder(domainName).append(":name=").append(namePropertyValue);
                for (MetricDimensionValue dv : dimensionValues) {
                    String dname = dv.dimension().name();
                    if (!names.contains(dname)) {
                        builder.append(',').append(escapeDimensionName(dname)).append('=').append(escape(dv.value()));
                        names.add(dname);
                    } else {
                        logger.warn("Ignoring duplicate dimension name: {}", dname);
                    }
                }

                objectName = new ObjectName(builder.toString());
            }

            return objectName;
        } catch (MalformedObjectNameException e) {
            logger.warn("Failed to create ObjectName: domain name = '{}', metric name = '{}'", domainName, name, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * https://docs.oracle.com/javase/7/docs/api/javax/management/ObjectName.html
     */
    private String escape(String v) {
        return v.replaceAll("[\\s*?,=:\\\\]", "_");
    }

    private String escapeDimensionName(String dname) {
        if (dname.equals("name")) {
            return "_name";
        }
        return escape(dname);
    }
}
