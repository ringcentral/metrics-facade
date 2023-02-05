package com.ringcentral.platform.metrics.var;

import com.ringcentral.platform.metrics.AbstractMetric;
import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.MetricListener;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.configs.VarConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.labels.LabelValues.NO_LABEL_VALUES;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.Collections.emptyList;

public abstract class AbstractVar<V> extends AbstractMetric implements Var<V> {

    public interface InstanceMaker<V> {
        VarInstance<V> makeInstance(
            MetricName name,
            List<LabelValue> labelValues,
            boolean totalInstance,
            boolean labeledMetricTotalInstance,
            boolean nonDecreasing,
            Measurable valueMeasurable,
            Supplier<V> valueSupplier);
    }

    private final VarConfig config;
    private final boolean nonDecreasing;

    private volatile boolean removed;
    private final List<MetricListener> listeners = new ArrayList<>();

    private final LabelValues prefixLabelValues;
    private final List<Label> labels;
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
            this.prefixLabelValues =
                config.hasPrefixLabelValues() ?
                config.prefixLabelValues() :
                NO_LABEL_VALUES;

            if (config.hasLabels()) {
                this.labels = config.labels();
                this.instances = new ConcurrentHashMap<>();
            } else {
                this.labels = emptyList();
                this.instances = null;
            }

            if (valueSupplier != null) {
                this.totalInstance = instanceMaker.makeInstance(
                    name,
                    this.prefixLabelValues.list(),
                    true,
                    !this.labels.isEmpty(),
                    config.isNonDecreasing(),
                    valueMeasurable,
                    valueSupplier);
            } else {
                this.totalInstance = null;
            }

            this.valueMeasurable = valueMeasurable;
            this.instanceMaker = instanceMaker;
        } else {
            this.prefixLabelValues = null;
            this.labels = null;
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
    public void register(Supplier<V> valueSupplier, LabelValues labelValues) {
        if (!isEnabled() || isRemoved()) {
            return;
        }

        checkLabelValues(labelValues.list());

        executor.execute(() -> {
            InstanceKey instanceKey = new InstanceKey(labelValues.list());

            if (isRemoved() || instances.containsKey(instanceKey)) {
                return;
            }

            List<LabelValue> instanceLabelValues;

            if (!prefixLabelValues.isEmpty()) {
                instanceLabelValues = new ArrayList<>(prefixLabelValues.size() + labelValues.size());
                instanceLabelValues.addAll(prefixLabelValues.list());
                instanceLabelValues.addAll(labelValues.list());
            } else {
                instanceLabelValues = labelValues.list();
            }

            VarInstance<V> instance = instanceMaker.makeInstance(
                name(),
                instanceLabelValues,
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

    private void checkLabelValues(List<LabelValue> labelValues) {
        checkArgument(
            labelValues != null && !labelValues.isEmpty(),
            "labelValues is null or empty");

        if (!labels.isEmpty()) {
            if (labelValues.size() != labels.size()) {
                unexpected(labelValues);
            }

            for (int i = 0; i < labels.size(); ++i) {
                if (!labels.get(i).equals(labelValues.get(i).label())) {
                    unexpected(labelValues);
                }
            }
        } else {
            unexpected(labelValues);
        }
    }

    private void unexpected(List<LabelValue> labelValues) {
        throw new IllegalArgumentException("labelValues = " + labelValues + " do not match labels = " + labels);
    }

    @Override
    public void deregister(LabelValues labelValues) {
        if (!isEnabled() || isRemoved()) {
            return;
        }

        checkLabelValues(labelValues.list());
        InstanceKey instanceKey = new InstanceKey(labelValues.list());

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

        final List<LabelValue> labelValues;
        final int hashCode;

        InstanceKey(List<LabelValue> labelValues) {
            this.labelValues = labelValues;
            this.hashCode = labelValues.hashCode();
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

            if (labelValues == that.labelValues) {
                return true;
            }

            for (int i = 0; i < labelValues.size(); ++i) {
                if (!labelValues.get(i).equals(that.labelValues.get(i))) {
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
