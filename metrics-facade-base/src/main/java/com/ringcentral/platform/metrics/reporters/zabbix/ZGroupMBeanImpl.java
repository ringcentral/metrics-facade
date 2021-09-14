package com.ringcentral.platform.metrics.reporters.zabbix;

import javax.management.*;
import com.ringcentral.platform.metrics.utils.AbstractDynamicMBean;

public class ZGroupMBeanImpl extends AbstractDynamicMBean {

    private final ZGroup group;
    private final ZGroupJsonMapper groupJsonMapper;
    private volatile String groupJson;
    private final String groupJsonAttrName;
    private final MBeanInfo info;

    public ZGroupMBeanImpl(
        ZGroup group,
        ZGroupJsonMapper groupJsonMapper,
        String groupJsonAttrName) {

        this.group = group;
        this.groupJsonMapper = groupJsonMapper;
        this.groupJsonAttrName = groupJsonAttrName;
        this.groupJson = groupJsonMapper.toJson(group);

        MBeanAttributeInfo[] attrInfos = new MBeanAttributeInfo[] {
            new MBeanAttributeInfo(
                groupJsonAttrName,
                String.class.getName(),
                null,
                true,
                false,
                false)
        };

        info = new MBeanInfo(
            ZGroupMBeanImpl.class.getName(),
            null,
            attrInfos,
            null,
            null,
            null);
    }

    public synchronized void addEntity(ZEntity entity) {
        group.addEntity(entity);
        groupJson = groupJsonMapper.toJson(group);
    }

    public synchronized void removeEntity(ZEntity entity) {
        group.removeEntity(entity);
        groupJson = groupJsonMapper.toJson(group);
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return info;
    }

    @Override
    public Object getAttribute(String attrName) throws AttributeNotFoundException {
        if (groupJsonAttrName.equals(attrName)) {
            return groupJson;
        }

        throw new AttributeNotFoundException("Unknown attribute '" + attrName + "'");
    }
}