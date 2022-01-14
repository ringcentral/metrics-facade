package com.ringcentral.platform.metrics.utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ObjectUtils {

    public static int hashCodeFor(Object o1, Object o2) {
        return new HashCodeBuilder(17, 37)
            .append(o1)
            .append(o2)
            .toHashCode();
    }

    public static int hashCodeFor(Object o1, Object o2, Object o3) {
        return new HashCodeBuilder(17, 37)
            .append(o1)
            .append(o2)
            .append(o3)
            .toHashCode();
    }
}
