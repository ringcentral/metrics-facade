package com.ringcentral.platform.metrics.var.longVar;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVar;
import com.ringcentral.platform.metrics.var.configs.VarConfig;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class AbstractLongVar extends AbstractVar<Long> implements LongVar {

    public interface LongVarInstanceMaker extends InstanceMaker<Long> {}

    public static class DefaultLongVarInstanceMaker implements LongVarInstanceMaker {

        public static DefaultLongVarInstanceMaker INSTANCE = new DefaultLongVarInstanceMaker();

        @Override
        public LongVarInstance makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean nonDecreasing,
            Measurable valueMeasurable,
            Supplier<Long> valueSupplier) {

            return new DefaultLongVarInstance(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
                nonDecreasing,
                valueMeasurable,
                valueSupplier);
        }
    }

    protected AbstractLongVar(
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
            DefaultLongVarInstanceMaker.INSTANCE,
            executor);
    }

    protected AbstractLongVar(
        MetricName name,
        VarConfig config,
        Measurable valueMeasurable,
        Supplier<Long> valueSupplier,
        InstanceMaker<Long> instanceMaker,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            valueMeasurable,
            valueSupplier,
            instanceMaker,
            executor);
    }
}
