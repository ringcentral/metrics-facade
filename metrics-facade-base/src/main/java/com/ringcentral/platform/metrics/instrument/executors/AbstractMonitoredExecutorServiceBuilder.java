package com.ringcentral.platform.metrics.instrument.executors;

import com.ringcentral.platform.metrics.MetricKey;
import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static com.ringcentral.platform.metrics.PrefixLabelValuesMetricKey.prefixLabelValuesMetricKey;
import static com.ringcentral.platform.metrics.labels.LabelValues.*;
import static com.ringcentral.platform.metrics.names.MetricName.*;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public abstract class AbstractMonitoredExecutorServiceBuilder<ES extends ExecutorService, B extends AbstractMonitoredExecutorServiceBuilder<ES, B>> {

    public static final MetricName DEFAULT_METRIC_NAME_PREFIX = metricName("executor", "service");

    public static final Label CLASS = new Label("class");
    public static final Label NAME = new Label("name");

    private final ES parent;
    private final MetricRegistry registry;
    private boolean withClass = false;
    private boolean classAsLabel = true;
    private String name;
    private boolean nameAsLabel = true;
    private MetricName metricNamePrefix = DEFAULT_METRIC_NAME_PREFIX;
    private LabelValues prefixLabelValues;
    private LabelValues additionalLabelValues = noLabelValues();

    protected static final AtomicLong orderNum = new AtomicLong();

    /**
     * Returns the next order number as a string (e.g., "1", "2", ...).
     * This is used to generate a metric name when no explicit name is provided.
     *
     * @return the next order number as a string
     */
    protected static String nextOrderNumAsString() {
        return Long.toString(orderNum.incrementAndGet());
    }

    /**
     * @param parent   the parent {@link ExecutorService} that is being monitored
     * @param registry the {@link MetricRegistry} used to register metrics
     */
    public AbstractMonitoredExecutorServiceBuilder(@Nonnull ES parent, @Nonnull MetricRegistry registry) {
        this.parent = parent;
        this.registry = registry;
    }

    /**
     * Configures whether the {@link Class#getSimpleName()} of the parent {@link ExecutorService} should be included,
     * and if so, whether it should be part of the metric name or added as a "class" label.
     *
     * @param included if {@code true}, the {@link Class#getSimpleName()} of the {@link ExecutorService}
     *                 will be included either in the metric name or as a label. If {@code false}, the
     *                 class name will not be included.
     * @param asLabel if {@code true}, the {@link Class#getSimpleName()} will be added as the "class" label.
     *                If {@code false}, it will be included as part of the metric name, provided {@code included}
     *                is {@code true}.
     * @return this builder
     */
    public B withExecutorServiceClass(boolean included, boolean asLabel) {
        this.withClass = included;
        this.classAsLabel = asLabel;
        return builder();
    }

    /**
     * Sets the name of the executor service.
     * See also {@link AbstractMonitoredExecutorServiceBuilder#nameAsLabel(boolean)}
     *
     * @param name the name to assign to the executor service
     * @return this builder
     */
    public B name(String name) {
        this.name = name;
        return builder();
    }

    /**
     * Configures whether the name of the executor service should be used as the value of the "name" label
     * or included directly as part of the metric names.
     *
     * @param nameAsLabel {@code true} to use the name as a label, {@code false} to include it as part of the metric names
     * @return this builder
     */
    public B nameAsLabel(boolean nameAsLabel) {
        this.nameAsLabel = nameAsLabel;
        return builder();
    }

    /**
     * Sets the metric name prefix.
     *
     * @param metricNamePrefix the prefix to use for the metric names
     * @return this builder
     * @throws IllegalArgumentException if {@code metricNamePrefix} is empty
     */
    public B metricNamePrefix(@Nonnull MetricName metricNamePrefix) {
        checkArgument(!metricNamePrefix.isEmpty(), "metricNamePrefix is empty");
        this.metricNamePrefix = metricNamePrefix;
        return builder();
    }

    /**
     * Sets the label values to prefix the metrics with.
     *
     * @param prefixLabelValues the label values to prefix the metrics with
     * @return this builder
     */
    public B prefixLabelValues(LabelValues prefixLabelValues) {
        this.prefixLabelValues = prefixLabelValues;
        return builder();
    }

    /**
     * Sets additional label values to be applied to the metrics.
     *
     * @param additionalLabelValues additional label values to be applied
     * @return this builder
     */
    public B additionalLabelValues(@Nonnull LabelValues additionalLabelValues) {
        this.additionalLabelValues = requireNonNull(additionalLabelValues);
        return builder();
    }

    /**
     * Helper method to cast the current builder to the concrete builder type.
     *
     * @return this builder
     */
    @SuppressWarnings("unchecked")
    protected B builder() {
        return (B)this;
    }

    /**
     * Builds and returns the monitored {@link ExecutorService}.
     *
     * <p>This method constructs the monitored executor service with the provided configuration and registers
     * metrics using the configured {@link MetricRegistry}.
     *
     * @return the constructed {@link ExecutorService}
     */
    public ES build() {
        String resultName = name != null ? name : nextOrderNumAsString();
        Function<MetricName, MetricKey> metricKeyProvider = varPart -> metricKey(makeMetricName(resultName, varPart));
        LabelValues labelValues = makeLabelValues(resultName);
        return build(parent, registry, metricKeyProvider, labelValues);
    }

    /**
     * @param name the name of the parent executor service
     * @param varPart the variable part, for example: name("pool", "size")
     */
    private MetricName makeMetricName(String name, MetricName varPart) {
        MetricName result = metricNamePrefix;

        if (withClass && !classAsLabel) {
            result = result.withNewPart(parent.getClass().getSimpleName());
        }

        if (!nameAsLabel) {
            result = result.withNewPart(name);
        }

        result = withName(result, varPart);
        return result;
    }

    /**
     * @param name the name of the parent executor service
     */
    private LabelValues makeLabelValues(String name) {
        LabelValues labelValues =
            withClass && classAsLabel ?
            labelValues(additionalLabelValues, CLASS.value(parent.getClass().getSimpleName())) :
            additionalLabelValues;

        if (nameAsLabel) {
            labelValues = labelValues(labelValues, NAME.value(name));
        }

        return labelValues;
    }

    /**
     * Creates a {@link MetricKey} using the provided metric name. If prefix label values are set,
     * a {@link com.ringcentral.platform.metrics.PrefixLabelValuesMetricKey} will be used to include them in the metric key.
     *
     * @param name the metric name
     * @return the created {@link MetricKey}
     */
    private MetricKey metricKey(MetricName name) {
        return
            prefixLabelValues != null && !prefixLabelValues.isEmpty() ?
            prefixLabelValuesMetricKey(name, prefixLabelValues) :
            name;
    }

    /**
     * Abstract method to be implemented by concrete subclasses to build the monitored {@link ExecutorService}.
     *
     * @param parent              the parent {@link ExecutorService} being monitored
     * @param registry            the {@link MetricRegistry} for metric registration
     * @param metricKeyProvider   a function that provides {@link MetricKey} for each metric
     * @param labelValues         the label values to apply to the metrics
     * @return the constructed {@link ExecutorService}
     */
    protected abstract ES build(
        ES parent,
        MetricRegistry registry,
        Function<MetricName, MetricKey> metricKeyProvider,
        LabelValues labelValues);
}
