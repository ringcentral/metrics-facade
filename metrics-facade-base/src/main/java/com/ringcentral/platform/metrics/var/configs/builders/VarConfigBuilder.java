package com.ringcentral.platform.metrics.var.configs.builders;

import com.ringcentral.platform.metrics.configs.builders.*;
import com.ringcentral.platform.metrics.var.configs.VarConfig;

public interface VarConfigBuilder<C extends VarConfig, CB extends VarConfigBuilder<C, CB>>
    extends MetricConfigBuilder<C>, MetricConfigBuilderProvider<CB> {}
