package com.ringcentral.platform.metrics.utils;

import javax.management.*;
import static java.util.Objects.*;

public abstract class AbstractDynamicMBean implements DynamicMBean {

    @Override
    public AttributeList getAttributes(String[] names) {
        requireNonNull(names);
        AttributeList attrs = new AttributeList();

        for (String name : names) {
            try {
                attrs.add(new Attribute(name, getAttribute(name)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return attrs;
    }

    @Override
    public void setAttribute(Attribute attr) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AttributeList setAttributes(AttributeList attrs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) {
        throw new UnsupportedOperationException();
    }
}
