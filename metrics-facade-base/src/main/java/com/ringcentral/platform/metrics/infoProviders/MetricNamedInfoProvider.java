package com.ringcentral.platform.metrics.infoProviders;

import java.util.List;
import com.ringcentral.platform.metrics.names.MetricNamed;

public interface MetricNamedInfoProvider<I> {
    default I infoFor(MetricNamed named) {
        List<I> infos = infosFor(named);
        return infos.isEmpty() ? null : infos.get(infos.size() - 1);
    }

    List<I> infosFor(MetricNamed named);
}