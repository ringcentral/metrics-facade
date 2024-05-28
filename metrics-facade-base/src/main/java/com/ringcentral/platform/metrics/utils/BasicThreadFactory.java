package com.ringcentral.platform.metrics.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;

public class BasicThreadFactory implements ThreadFactory {

    private final ThreadFactory parent;
    private final String namePattern;
    private final boolean daemon;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public BasicThreadFactory(String namePattern) {
        this(Executors.defaultThreadFactory(), namePattern, true);
    }

    public BasicThreadFactory(ThreadFactory parent, String namePattern, boolean daemon) {
        this.parent = requireNonNull(parent);
        this.daemon = daemon;
        this.namePattern = requireNonNull(namePattern);
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Thread newThread(Runnable runnable) {
        Thread t = parent.newThread(runnable);
        t.setName(String.format(namePattern, threadNumber.getAndIncrement()));
        t.setDaemon(daemon);
        return t;
    }
}
