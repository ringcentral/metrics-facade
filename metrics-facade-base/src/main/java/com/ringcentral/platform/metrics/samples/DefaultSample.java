package com.ringcentral.platform.metrics.samples;

import java.util.Objects;

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DefaultSample sample = (DefaultSample) o;
        return Objects.equals(name, sample.name) && Objects.equals(value, sample.value) && Objects.equals(type, sample.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, type);
    }

    @Override
    public String toString() {
        return "DefaultSample{" +
            "name='" + name + '\'' +
            ", value=" + value +
            ", type='" + type + '\'' +
            '}';
    }
}
