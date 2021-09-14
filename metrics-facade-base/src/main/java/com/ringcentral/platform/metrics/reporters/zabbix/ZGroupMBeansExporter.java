package com.ringcentral.platform.metrics.reporters.zabbix;

import org.slf4j.Logger;

import javax.management.*;
import java.util.*;

import static java.lang.management.ManagementFactory.*;
import static org.slf4j.LoggerFactory.*;

public class ZGroupMBeansExporter implements ZabbixLldMetricsReporterListener {

    private final MBeanServer mBeanServer;
    private final String objectNamePrefix;
    private final ZGroupJsonMapper groupJsonMapper;
    private final String groupJsonAttrName;
    private final Map<String, ZGroupMBeanImpl> groupMBeans = new HashMap<>();
    private final Map<ObjectName, ObjectName> actualObjectNames = new HashMap<>();

    private static final Logger logger = getLogger(ZGroupMBeansExporter.class);

    public ZGroupMBeansExporter(
        String objectNamePrefix,
        ZGroupJsonMapper groupJsonMapper,
        String groupJsonAttrName) {

        this(
            getPlatformMBeanServer(),
            objectNamePrefix,
            groupJsonMapper,
            groupJsonAttrName
        );
    }

    public ZGroupMBeansExporter(
        MBeanServer mBeanServer,
        String objectNamePrefix,
        ZGroupJsonMapper groupJsonMapper,
        String groupJsonAttrName) {

        this.objectNamePrefix = objectNamePrefix;
        this.mBeanServer = mBeanServer;
        this.groupJsonAttrName = groupJsonAttrName;
        this.groupJsonMapper = groupJsonMapper;
    }

    public synchronized void ensureGroup(String groupName) {
        if (!groupMBeans.containsKey(groupName)) {
            addGroup(new ZGroup(groupName));
        }
    }

    private void addGroup(ZGroup group) {
        ZGroupMBeanImpl groupMBean = new ZGroupMBeanImpl(group, groupJsonMapper, groupJsonAttrName);
        ObjectName objectName;

        try {
            objectName = new ObjectName(objectNamePrefix + group.name());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (registerMBean(objectName, groupMBean)) {
            groupMBeans.put(group.name(), groupMBean);
        }
    }

    @Override
    public synchronized void entityAdded(ZEntity entity) {
        ZGroupMBeanImpl groupMBean = groupMBeans.get(entity.groupName());

        if (groupMBean != null) {
            groupMBean.addEntity(entity);
        } else {
            ZGroup group = new ZGroup(entity.groupName());
            group.addEntity(entity);
            addGroup(group);
        }
    }

    private boolean registerMBean(ObjectName objectName, DynamicMBean mBean) {
        try {
            ObjectInstance registeredMBean = mBeanServer.registerMBean(mBean, objectName);

            if (registeredMBean != null) {
                actualObjectNames.put(objectName, registeredMBean.getObjectName());
            } else {
                actualObjectNames.put(objectName, objectName);
            }

            return true;
        } catch (InstanceAlreadyExistsException e) {
            logger.debug("Failed to register MBean '{}'", objectName.getCanonicalName(), e);
            return false;
        } catch (Exception e) {
            logger.warn("Failed to register MBean '{}'", objectName.getCanonicalName(), e);
            return false;
        }
    }

    @Override
    public synchronized void entityRemoved(ZEntity entity) {
        ZGroupMBeanImpl groupMBean = groupMBeans.get(entity.groupName());

        if (groupMBean != null) {
            groupMBean.removeEntity(entity);
        }
    }

    @Override
    public synchronized void close() {
        Iterator<ObjectName> objectNamesIter = actualObjectNames.keySet().iterator();

        while (objectNamesIter.hasNext()) {
            deregisterMBean(objectNamesIter.next());
            objectNamesIter.remove();
        }
    }

    private void deregisterMBean(ObjectName objectName) {
        try {
            mBeanServer.unregisterMBean(actualObjectNames.get(objectName));
        } catch (InstanceNotFoundException e) {
            logger.debug("Failed to deregister MBean '{}'", objectName.getCanonicalName(), e);
        } catch (Exception e) {
            logger.warn("Failed to deregister MBean '{}'", objectName.getCanonicalName(), e);
        }
    }
}
