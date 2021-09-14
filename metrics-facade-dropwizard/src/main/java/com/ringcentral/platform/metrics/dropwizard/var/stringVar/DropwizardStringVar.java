package com.ringcentral.platform.metrics.dropwizard.var.stringVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import com.ringcentral.platform.metrics.var.AbstractVar;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.stringVar.StringVar;

public class DropwizardStringVar extends AbstractVar<String> implements StringVar {

    public DropwizardStringVar(
        MetricName name,
        VarConfig config,
        Supplier<String> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            STRING_VALUE,
            valueSupplier,
            executor);
    }
}