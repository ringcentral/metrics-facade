package com.ringcentral.platform.metrics.infoProviders;

import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class DefaultMetricNamedInfoProvider<I> implements PredicativeMetricNamedInfoProvider<I> {

    private final List<Entry<I>> entries;
    private final Map<String, Entry<I>> keyToEntry;

    public DefaultMetricNamedInfoProvider() {
        this(new ArrayList<>(), new HashMap<>());
    }

    protected DefaultMetricNamedInfoProvider(List<Entry<I>> entries, Map<String, Entry<I>> keyToEntry) {
        this.entries = requireNonNull(entries);
        this.keyToEntry = requireNonNull(keyToEntry);
    }

    @Override
    public DefaultMetricNamedInfoProvider<I> addInfo(MetricNamedPredicate predicate, I info) {
        return addInfo(null, predicate, info);
    }

    @Override
    public DefaultMetricNamedInfoProvider<I> addInfo(String key, MetricNamedPredicate predicate, I info) {
        Entry<I> entry = new Entry<>(predicate, info);

        if (key != null) {
            if (keyToEntry.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate key " + key);
            }

            keyToEntry.put(key, entry);
        }

        entries.add(entry);
        return this;
    }

    @Override
    public DefaultMetricNamedInfoProvider<I> removeInfo(String key) {
        Entry<I> entry = keyToEntry.remove(key);

        if (entry != null) {
            entries.remove(entry);
        }

        return this;
    }

    @Override
    public List<I> infosFor(MetricNamed named) {
        return entries.stream().filter(e -> e.predicate.matches(named)).map(e -> e.info).collect(toList());
    }

    private static class Entry<I> {

        final MetricNamedPredicate predicate;
        final I info;

        Entry(MetricNamedPredicate predicate, I info) {
            this.predicate = predicate;
            this.info = info;
        }
    }
}
