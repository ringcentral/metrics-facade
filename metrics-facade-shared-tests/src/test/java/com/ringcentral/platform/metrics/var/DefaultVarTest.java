package com.ringcentral.platform.metrics.var;

import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;
import com.ringcentral.platform.metrics.var.longVar.AbstractLongVar;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.var.longVar.LongVar.LONG_VALUE;

public class DefaultVarTest extends AbstractVarTest<Long, AbstractVar<Long>> {

    protected static class TestVar extends AbstractLongVar {

        protected TestVar(
            MetricName name,
            VarConfig config,
            Measurable valueMeasurable,
            Supplier<Long> valueSupplier,
            ScheduledExecutorService executor) {

            super(
                name,
                config,
                valueMeasurable,
                valueSupplier,
                executor);
        }
    }

    public DefaultVarTest() {
        super(
            (name, configBuilder, valueSupplier, executor) ->
                new TestVar(name, configBuilder.build(), LONG_VALUE, valueSupplier, executor),
            new ValueSupplierMaker<>() {

                final AtomicLong orderNum = new AtomicLong();

                @Override
                public Supplier<Long> makeValueSupplier() {
                    return orderNum::incrementAndGet;
                }
            });
    }
}
