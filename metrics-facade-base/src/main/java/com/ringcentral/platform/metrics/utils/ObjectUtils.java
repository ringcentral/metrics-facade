package com.ringcentral.platform.metrics.utils;

import java.util.Objects;

public class ObjectUtils {

    public static int hashCodeFor(Object o1, Object o2) {
        return Objects.hash(o1, o2);
    }

    public static int hashCodeFor(Object o1, Object o2, Object o3) {
        return Objects.hash(o1, o2, o3);
    }
}
