package com.ringcentral.platform.metrics.var.stringVar;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVar;
import com.ringcentral.platform.metrics.var.configs.VarConfig;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class AbstractStringVar extends AbstractVar<String> implements StringVar {

    public interface StringVarInstanceMaker extends InstanceMaker<String> {}

    public static class DefaultStringVarInstanceMaker implements StringVarInstanceMaker {

        public static DefaultStringVarInstanceMaker INSTANCE = new DefaultStringVarInstanceMaker();

        @Override
        public StringVarInstance makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean nonDecreasing,
            Measurable valueMeasurable,
            Supplier<String> valueSupplier) {

            return new DefaultStringVarInstance(
                name,
                dimensionValues,
                totalInstance,
                dimensionalTotalInstance,
                nonDecreasing,
                valueMeasurable,
                valueSupplier);
        }
    }

    protected AbstractStringVar(
        MetricName name,
        VarConfig config,
        Measurable valueMeasurable,
        Supplier<String> valueSupplier,
        ScheduledExecutorService executor) {

        super(
            name,
            config,
            valueMeasurable,
            valueSupplier,
            DefaultStringVarInstanceMaker.INSTANCE,
            executor);
    }

    protected AbstractStringVar(
        MetricName name,
        VarConfig config,
        Measurable valueMeasurable,
        Supplier<String> valueSupplier,
        InstanceMaker<String> instanceMaker,
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
