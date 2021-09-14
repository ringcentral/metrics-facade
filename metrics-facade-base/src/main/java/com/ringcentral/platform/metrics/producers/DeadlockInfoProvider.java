package com.ringcentral.platform.metrics.producers;

import java.lang.management.*;
import java.util.*;

import static java.lang.String.*;
import static java.lang.management.ManagementFactory.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

public class DeadlockInfoProvider {

    private static final int DEFAULT_MAX_STACK_TRACE_DEPTH = 100;

    private final int maxStackTraceDepth;
    private final ThreadMXBean threadMxBean;

    public DeadlockInfoProvider() {
        this(getThreadMXBean());
    }

    public DeadlockInfoProvider(ThreadMXBean threadMxBean) {
        this(DEFAULT_MAX_STACK_TRACE_DEPTH, threadMxBean);
    }

    public DeadlockInfoProvider(int maxStackTraceDepth, ThreadMXBean threadMxBean) {
        this.maxStackTraceDepth = maxStackTraceDepth;
        this.threadMxBean = requireNonNull(threadMxBean);
    }

    public Set<String> deadlockedThreadTextInfos() {
        long[] ids = threadMxBean.findDeadlockedThreads();

        if (ids == null || ids.length == 0) {
            return emptySet();
        }

        Set<String> result = new HashSet<>();

        for (ThreadInfo info : threadMxBean.getThreadInfo(ids, maxStackTraceDepth)) {
            StringBuilder stackTrace = new StringBuilder();

            for (StackTraceElement element : info.getStackTrace()) {
                stackTrace
                    .append("\t at ")
                    .append(element.toString())
                    .append(format("%n"));
            }

            result.add(format(
                "%s locked on %s (owned by %s):%n%s",
                info.getThreadName(),
                info.getLockName(),
                info.getLockOwnerName(),
                stackTrace.toString()));
        }

        return result;
    }
}
