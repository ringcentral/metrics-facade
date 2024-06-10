package com.ringcentral.platform.metrics.reporters.zabbix;

import java.util.List;

import static java.util.Collections.*;
import static java.util.Objects.*;

public class ZEntity {

    private final String groupName;
    private final List<ZAttribute> attrs;
    private final int hashCode;

    public ZEntity(String groupName, List<ZAttribute> attrs) {
        this.groupName = requireNonNull(groupName);
        this.attrs = attrs != null ? attrs : emptyList();
        this.hashCode = hash(groupName, this.attrs);
    }

    public String groupName() {
        return groupName;
    }

    public List<ZAttribute> attributes() {
        return attrs;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ZEntity that = (ZEntity)other;

        if (hashCode != that.hashCode) {
            return false;
        }

        return groupName.equals(that.groupName) && attrs.equals(that.attrs);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "ZEntity{" +
            "groupName='" + groupName + '\'' +
            ", attrs=" + attrs +
            '}';
    }
}
