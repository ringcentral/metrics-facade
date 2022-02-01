package com.ringcentral.platform.metrics.utils;

import com.ringcentral.platform.metrics.MetricContext;

public class MetricContextUtils {

    public static boolean has(Object key, MetricContext... contexts) {
        return get(key, contexts) != null;
    }

    public static  <V> V get(Object key, V defaultValue, MetricContext... contexts) {
        V v = get(key, contexts);
        return v != null ? v : defaultValue;
    }

    public static  <V> V getForClass(Class<? extends V> key, MetricContext... contexts) {
        return get(key, contexts);
    }

    public static <V> V get(Object key, MetricContext... contexts) {
        if (contexts == null || contexts.length == 0) {
            return null;
        }

        V value = null;

        for (MetricContext context : contexts) {
            if (context != null) {
                value = context.get(key);

                if (value != null) {
                    break;
                }
            }
        }

        return value;
    }
}
