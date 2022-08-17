package com.ringcentral.platform.metrics.var;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.NO_DIMENSION_VALUES;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.Collections.emptyList;

public abstract class AbstractVar<V> extends AbstractMetric implements Var<V> {

    public interface InstanceMaker<V> {
        VarInstance<V> makeInstance(
            MetricName name,
            List<MetricDimensionValue> dimensionValues,
            boolean totalInstance,
            boolean dimensionalTotalInstance,
            boolean nonDecreasing,
            Measurable valueMeasurable,
            Supplier<V> valueSupplier);
    }

    private final VarConfig config;
    private final boolean nonDecreasing;

    private volatile boolean removed;
    private final List<MetricListener> listeners = new ArrayList<>();

    private final MetricDimensionValues prefixDimensionValues;
    private final List<MetricDimension> dimensions;
    private final VarInstance<V> totalInstance;
    private final ConcurrentHashMap<InstanceKey, VarInstance<V>> instances;

    private final Measurable valueMeasurable;

    private final InstanceMaker<V> instanceMaker;
    private final ScheduledExecutorService executor;

    protected AbstractVar(
        MetricName name,
        VarConfig config,
        Measurable valueMeasurable,
        Supplier<V> valueSupplier,
        InstanceMaker<V> instanceMaker,
        ScheduledExecutorService executor) {

        super(
            config.isEnabled(),
            name,
            config.description());

        this.config = config;
        this.nonDecreasing = config.isNonDecreasing();

        if (config.isEnabled()) {
            this.prefixDimensionValues =
                config.hasPrefixDimensionValues() ?
                config.prefixDimensionValues() :
                NO_DIMENSION_VALUES;

            if (config.hasDimensions()) {
                this.dimensions = config.dimensions();
                this.instances = new ConcurrentHashMap<>();
            } else {
                this.dimensions = emptyList();
                this.instances = null;
            }

            if (valueSupplier != null) {
                this.totalInstance = instanceMaker.makeInstance(
                    name,
                    this.prefixDimensionValues.list(),
                    true,
                    !this.dimensions.isEmpty(),
                    config.isNonDecreasing(),
                    valueMeasurable,
                    valueSupplier);
            } else {
                this.totalInstance = null;
            }

            this.valueMeasurable = valueMeasurable;
            this.instanceMaker = instanceMaker;
        } else {
            this.prefixDimensionValues = null;
            this.dimensions = null;
            this.totalInstance = null;
            this.instances = null;
            this.valueMeasurable = null;
            this.instanceMaker = null;
        }

        this.executor = executor;
    }

    protected VarConfig config() {
        return config;
    }

    public Measurable valueMeasurable() {
        return valueMeasurable;
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void addListener(MetricListener listener) {
        if (!isEnabled() || isRemoved()) {
            return;
        }

        executor.execute(() -> {
            if (isRemoved()) {
                return;
            }

            listeners.add(listener);

            forEach(instance -> {
                notifyListener(listener, l -> l.metricInstanceAdded(instance));
                instance.metricInstanceAdded();
            });
        });
    }

    protected boolean isRemoved() {
        return removed;
    }

    @Override
    public Iterator<MetricInstance> iterator() {
        return new InstancesIterator<>(
            totalInstance,
            instances != null ? instances.values().iterator() : null);
    }

    public void metricRemoved() {
        if (isRemoved()) {
            return;
        }

        if (!isEnabled()) {
            removed = true;
            return;
        }

        executor.execute(() -> {
            if (isRemoved()) {
                return;
            }

            removed = true;

            forEach(instance -> {
                listeners.forEach(listener -> listener.metricInstanceRemoved(instance));
                instance.metricInstanceRemoved();
            });

            listeners.clear();
        });
    }

    @Override
    public void register(Supplier<V> valueSupplier, MetricDimensionValues dimensionValues) {
        if (!isEnabled() || isRemoved()) {
            return;
        }

        checkDimensionValues(dimensionValues.list());

        executor.execute(() -> {
            InstanceKey instanceKey = new InstanceKey(dimensionValues.list());

            if (isRemoved() || instances.containsKey(instanceKey)) {
                return;
            }

            List<MetricDimensionValue> instanceDimensionValues;

            if (!prefixDimensionValues.isEmpty()) {
                instanceDimensionValues = new ArrayList<>(prefixDimensionValues.size() + dimensionValues.size());
                instanceDimensionValues.addAll(prefixDimensionValues.list());
                instanceDimensionValues.addAll(dimensionValues.list());
            } else {
                instanceDimensionValues = dimensionValues.list();
            }

            VarInstance<V> instance = instanceMaker.makeInstance(
                name(),
                instanceDimensionValues,
                false,
                false,
                nonDecreasing,
                valueMeasurable,
                valueSupplier);

            instances.put(instanceKey, instance);
            listeners.forEach(listener -> notifyListener(listener, l -> l.metricInstanceAdded(instance)));
            instance.metricInstanceAdded();
        });
    }

    private void checkDimensionValues(List<MetricDimensionValue> dimensionValues) {
        checkArgument(
            dimensionValues != null && !dimensionValues.isEmpty(),
            "dimensionValues is null or empty");

        if (!dimensions.isEmpty()) {
            if (dimensionValues.size() != dimensions.size()) {
                unexpected(dimensionValues);
            }

            for (int i = 0; i < dimensions.size(); ++i) {
                if (!dimensions.get(i).equals(dimensionValues.get(i).dimension())) {
                    unexpected(dimensionValues);
                }
            }
        } else {
            unexpected(dimensionValues);
        }
    }

    private void unexpected(List<MetricDimensionValue> dimensionValues) {
        throw new IllegalArgumentException(
            "dimensionValues = " + dimensionValues +
            " do not match dimensions = " + dimensions);
    }

    @Override
    public void deregister(MetricDimensionValues dimensionValues) {
        if (!isEnabled() || isRemoved()) {
            return;
        }

        checkDimensionValues(dimensionValues.list());
        InstanceKey instanceKey = new InstanceKey(dimensionValues.list());

        executor.execute(() -> {
            if (isRemoved()) {
                return;
            }

            VarInstance<V> instance = instances.remove(instanceKey);

            if (instance != null) {
                listeners.forEach(listener -> notifyListener(listener, l -> l.metricInstanceRemoved(instance)));
                instance.metricInstanceRemoved();
            }
        });
    }

    private static class InstanceKey {

        final List<MetricDimensionValue> dimensionValues;
        final int hashCode;

        InstanceKey(List<MetricDimensionValue> dimensionValues) {
            this.dimensionValues = dimensionValues;
            this.hashCode = dimensionValues.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            InstanceKey that = (InstanceKey)other;

            if (hashCode != that.hashCode) {
                return false;
            }

            if (dimensionValues == that.dimensionValues) {
                return true;
            }

            for (int i = 0; i < dimensionValues.size(); ++i) {
                if (!dimensionValues.get(i).equals(that.dimensionValues.get(i))) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    private static class InstancesIterator<V> implements Iterator<MetricInstance> {

        VarInstance<V> totalInstance;
        Iterator<? extends MetricInstance> instancesIter;

        InstancesIterator(
            VarInstance<V> totalInstance,
            Iterator<? extends MetricInstance> instancesIter) {

            this.totalInstance = totalInstance;
            this.instancesIter = instancesIter;
        }

        @Override
        public boolean hasNext() {
            return totalInstance != null || (instancesIter != null && instancesIter.hasNext());
        }

        @Override
        public MetricInstance next() {
            if (totalInstance != null) {
                MetricInstance result = totalInstance;
                totalInstance = null;
                return result;
            }

            if (instancesIter != null) {
                if (instancesIter.hasNext()) {
                    return instancesIter.next();
                } else {
                    instancesIter = null;
                }
            }

            throw new NoSuchElementException();
        }
    }
}
