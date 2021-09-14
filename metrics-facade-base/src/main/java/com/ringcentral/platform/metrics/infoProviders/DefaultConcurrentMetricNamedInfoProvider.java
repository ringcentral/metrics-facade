package com.ringcentral.platform.metrics.infoProviders;

import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultConcurrentMetricNamedInfoProvider<I>
    extends DefaultMetricNamedInfoProvider<I>
    implements ConcurrentMetricNamedInfoProvider<I> {

    public DefaultConcurrentMetricNamedInfoProvider() {
        super(new CopyOnWriteArrayList<>());
    }
}
