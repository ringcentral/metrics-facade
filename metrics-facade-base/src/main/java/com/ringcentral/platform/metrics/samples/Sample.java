package com.ringcentral.platform.metrics.samples;

import java.util.List;

import static java.util.Collections.emptyList;

public interface Sample<S extends Sample<S>> {
    default boolean hasChildren() {
        return !children().isEmpty();
    }

    default List<S> children() {
        return emptyList();
    }
}
