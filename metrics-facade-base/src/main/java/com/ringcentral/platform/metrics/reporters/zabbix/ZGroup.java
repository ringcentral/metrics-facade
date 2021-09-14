package com.ringcentral.platform.metrics.reporters.zabbix;

import java.util.LinkedHashSet;
import static java.util.Objects.*;

public class ZGroup {

    private final String name;
    private final LinkedHashSet<ZEntity> entities;

    public ZGroup(String name) {
        this(name, new LinkedHashSet<>());
    }

    public ZGroup(
        String name,
        LinkedHashSet<ZEntity> entities) {

        this.name = requireNonNull(name);
        this.entities = requireNonNull(entities);
    }

    public String name() {
        return name;
    }

    public void addEntity(ZEntity entity) {
        entities.add(entity);
    }

    public void removeEntity(ZEntity entity) {
        entities.remove(entity);
    }

    public LinkedHashSet<ZEntity> entities() {
        return entities;
    }
}
