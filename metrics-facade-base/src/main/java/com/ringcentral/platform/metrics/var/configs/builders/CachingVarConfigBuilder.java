package com.ringcentral.platform.metrics.var.configs.builders;

import com.ringcentral.platform.metrics.var.configs.CachingVarConfig;

public interface CachingVarConfigBuilder<C extends CachingVarConfig, CB extends CachingVarConfigBuilder<C, CB>>
    extends VarConfigBuilder<C, CB> {}
