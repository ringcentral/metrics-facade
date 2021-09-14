package com.ringcentral.platform.metrics.measurables;

public enum MeasurableType {
    OBJECT(Object.class),
    LONG(Long.class),
    DOUBLE(Double.class),
    STRING(String.class);

    private final Class<?> clazz;

    MeasurableType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> clazz() {
        return clazz;
    }
}
