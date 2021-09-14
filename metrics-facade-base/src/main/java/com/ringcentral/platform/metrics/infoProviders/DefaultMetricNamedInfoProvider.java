package com.ringcentral.platform.metrics.infoProviders;

import java.util.*;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;
import static java.util.stream.Collectors.*;

public class DefaultMetricNamedInfoProvider<I> implements PredicativeMetricNamedInfoProvider<I> {

    private final List<Entry<I>> entries;

    public DefaultMetricNamedInfoProvider() {
        this(new ArrayList<>());
    }

    protected DefaultMetricNamedInfoProvider(List<Entry<I>> entries) {
        this.entries = entries;
    }

    @Override
    public DefaultMetricNamedInfoProvider<I> addInfo(MetricNamedPredicate predicate, I info) {
        entries.add(new Entry<>(predicate, info));
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
