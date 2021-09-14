package com.ringcentral.platform.metrics.producers;

import org.slf4j.Logger;

import javax.management.*;
import java.io.IOException;
import java.util.Set;
import java.util.function.Supplier;

import static java.lang.management.ManagementFactory.*;
import static java.util.Objects.*;
import static org.slf4j.LoggerFactory.*;

public class JmxAttrValueSupplier implements Supplier<Object> {

    private final MBeanServerConnection mBeanServerConnection;
    private final ObjectName objectName;
    private final String attrName;

    private static final Logger logger = getLogger(JmxAttrValueSupplier.class);

    public JmxAttrValueSupplier(ObjectName objectName, String attrName) {
        this(getPlatformMBeanServer(), objectName, attrName);
    }

    public JmxAttrValueSupplier(
        MBeanServerConnection mBeanServerConnection,
        ObjectName objectName,
        String attrName) {

        this.mBeanServerConnection = requireNonNull(mBeanServerConnection);
        this.objectName = requireNonNull(objectName);
        this.attrName = requireNonNull(attrName);
    }

    @Override
    public Object get() {
        try {
            return mBeanServerConnection.getAttribute(objectName(), attrName);
        } catch (Exception e) {
            logger.debug("Failed to get JMX attr value", e);
            return null;
        }
    }

    private ObjectName objectName() throws IOException {
        if (objectName.isPattern()) {
            Set<ObjectName> foundNames = mBeanServerConnection.queryNames(objectName, null);

            if (foundNames.size() == 1) {
                return foundNames.iterator().next();
            }
        }

        return objectName;
    }
}
