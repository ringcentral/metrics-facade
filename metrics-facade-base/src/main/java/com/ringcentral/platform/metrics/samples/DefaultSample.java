package com.ringcentral.platform.metrics.samples;

public class DefaultSample implements Sample {

    private final String name;
    private final Object value;
    private final String type;

    public DefaultSample(String name, Object value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String name() {
        return name;
    }

    public Object value() {
        return value;
    }

    public String type() {
        return type;
    }
}
