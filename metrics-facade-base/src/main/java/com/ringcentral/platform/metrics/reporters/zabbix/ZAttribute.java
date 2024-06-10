package com.ringcentral.platform.metrics.reporters.zabbix;

import static java.util.Objects.*;

public class ZAttribute {

    private final String name;
    private final String value;
    private final int hashCode;

    public ZAttribute(String name, String value) {
        this.name = requireNonNull(name);
        this.value = requireNonNull(value);
        this.hashCode = hash(name, value);
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ZAttribute that = (ZAttribute)other;

        if (hashCode != that.hashCode) {
            return false;
        }

        return name.equals(that.name) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "ZAttribute{" +
            "name='" + name + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
