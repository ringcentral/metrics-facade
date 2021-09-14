package com.ringcentral.platform.metrics.var.configs;

import java.util.concurrent.TimeUnit;

public interface CachingVarConfig extends VarConfig {
    long ttl();
    TimeUnit ttlUnit();
}
