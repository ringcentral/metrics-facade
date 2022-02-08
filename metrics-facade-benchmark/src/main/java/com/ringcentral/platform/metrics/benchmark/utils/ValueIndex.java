package com.ringcentral.platform.metrics.benchmark.utils;

public class ValueIndex {

    private final int max;
    private int i;

    public ValueIndex(int size) {
        this.max = size - 1;
    }

    public int next() {
        if (i > max) {
            i = 0;
        }

        return i++;
    }
}
