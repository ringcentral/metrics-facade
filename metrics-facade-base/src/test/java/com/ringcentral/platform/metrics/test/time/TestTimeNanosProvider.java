package com.ringcentral.platform.metrics.test.time;

import com.ringcentral.platform.metrics.utils.TimeNanosProvider;

import java.util.*;

import static java.util.concurrent.TimeUnit.*;

public class TestTimeNanosProvider implements TimeNanosProvider {

    public interface Listener {
        void whenTimeNanos(long timeNanos);
    }

    public interface PeriodicListener extends Listener {
        long periodNanos();
    }

    long time;
    final NavigableMap<Long, Set<Listener>> listeners = new TreeMap<>();

    public synchronized void addListener(Listener listener, long delayNanos) {
        if (delayNanos > 0L) {
            listeners.computeIfAbsent(time + delayNanos, t -> new LinkedHashSet<>()).add(listener);
        } else {
            listener.whenTimeNanos(time);

            if (listener instanceof PeriodicListener) {
                PeriodicListener periodicListener = (PeriodicListener)listener;
                addListener(periodicListener, periodicListener.periodNanos());
            }
        }
    }

    @Override
    public long timeNanos() {
        return time;
    }

    public void incrementMs() {
        increaseMs(1L);
    }

    public void increaseMs(long increaseMs) {
        increase(MILLISECONDS.toNanos(increaseMs));
    }

    public void increaseSec(long increaseSec) {
        increase(SECONDS.toNanos(increaseSec));
    }

    public void increment() {
        increase(1L);
    }

    public synchronized void increase(long increase) {
        long oldTime = time;
        time += increase;

        SortedMap<Long, Set<Listener>> affectedListeners = listeners.subMap(
            oldTime,
            false,
            time,
            true);

        affectedListeners.forEach((t, tListeners) -> tListeners.forEach(tListener -> {
            tListener.whenTimeNanos(t);

            if (tListener instanceof PeriodicListener) {
                PeriodicListener periodicTListener = (PeriodicListener)tListener;
                long pt = t + periodicTListener.periodNanos();

                while (pt <= time) {
                    periodicTListener.whenTimeNanos(pt);
                    pt += periodicTListener.periodNanos();
                }

                addListener(periodicTListener, pt - time);
            }
        }));

        affectedListeners.clear();
    }
}
