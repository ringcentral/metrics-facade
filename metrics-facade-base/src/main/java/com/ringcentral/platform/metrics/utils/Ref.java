package com.ringcentral.platform.metrics.utils;

public class Ref<V> {

    private V value;

    public Ref() {}

    public Ref(V value) {
        this.value = value;
    }

    public static <V> Ref<V> of(V value) {
        return new Ref<>(value);
    }

    public boolean hasValue() {
        return value != null;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public V value() {
        return value;
    }
}
