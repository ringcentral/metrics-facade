package com.ringcentral.platform.metrics.utils;

import java.util.LinkedHashSet;
import java.util.Set;

public class ClassUtils {

    /**
     * Retrieves all interfaces implemented by the given class and its superclasses.
     *
     * @param clazz the class to analyze, may be null
     * @return a set of interfaces implemented by the class and its superclasses, or null if the class is null
     */
    public static Set<Class<?>> interfacesOf(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        LinkedHashSet<Class<?>> result = new LinkedHashSet<>();
        collectInterfacesOf(clazz, result);
        return result;
    }

    private static void collectInterfacesOf(Class<?> currClass, LinkedHashSet<Class<?>> result) {
        while (currClass != null) {
            Class<?>[] currClassInterfaces = currClass.getInterfaces();

            for (Class<?> i : currClassInterfaces) {
                if (result.add(i)) {
                    collectInterfacesOf(i, result);
                }
            }

            currClass = currClass.getSuperclass();
        }
    }
}
