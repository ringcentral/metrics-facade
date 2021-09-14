package com.ringcentral.platform.metrics.reporters.jmx;

import java.util.*;
import javax.management.*;
import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.utils.AbstractDynamicMBean;

public class MBeanImpl extends AbstractDynamicMBean {

    private final MetricInstance instance;
    private final Map<String, Measurable> attrNameToMeasurable = new HashMap<>();
    private final MBeanInfo info;

    public MBeanImpl(
        MetricInstance instance,
        Set<Measurable> measurables,
        MeasurableNameProvider measurableNameProvider) {

        this.instance = instance;

        MBeanAttributeInfo[] attrInfos = new MBeanAttributeInfo[measurables.size()];
        int attrInfoIndex = 0;

        for (Measurable measurable : measurables) {
            String attrName = measurableNameProvider.nameFor(instance, measurable);
            attrNameToMeasurable.put(attrName, measurable);

            attrInfos[attrInfoIndex++] = new MBeanAttributeInfo(
                attrName,
                measurable.type().clazz().getName(),
                null,
                true,
                false,
                false);
        }

        info = new MBeanInfo(
            MBeanImpl.class.getName(),
            null,
            attrInfos,
            null,
            null,
            null);
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return info;
    }

    @Override
    public Object getAttribute(String attrName) throws AttributeNotFoundException {
        if (attrNameToMeasurable.containsKey(attrName)) {
            return instance.valueOf(attrNameToMeasurable.get(attrName));
        } else {
            throw new AttributeNotFoundException("Unknown attribute '" + attrName + "'");
        }
    }
}