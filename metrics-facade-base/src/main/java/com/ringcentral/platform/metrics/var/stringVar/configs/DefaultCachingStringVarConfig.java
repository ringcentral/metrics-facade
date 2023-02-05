package com.ringcentral.platform.metrics.var.stringVar.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.var.configs.AbstractCachingVarConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DefaultCachingStringVarConfig extends AbstractCachingVarConfig implements CachingStringVarConfig {

    public DefaultCachingStringVarConfig(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        boolean nonDecreasing,
        MetricContext context,
        long ttl,
        TimeUnit ttlUnit) {

        super(
            enabled,
            description,
            prefixLabelValues,
            labels,
            nonDecreasing,
            context,
            ttl,
            ttlUnit);
    }
}