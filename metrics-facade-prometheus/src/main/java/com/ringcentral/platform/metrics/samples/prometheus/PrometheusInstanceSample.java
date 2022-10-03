package com.ringcentral.platform.metrics.samples.prometheus;

import com.ringcentral.platform.metrics.counter.Counter.Count;
import com.ringcentral.platform.metrics.histogram.Histogram.Bucket;
import com.ringcentral.platform.metrics.histogram.Histogram.Percentile;
import com.ringcentral.platform.metrics.histogram.Histogram.TotalSum;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.samples.AbstractInstanceSample;
import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class PrometheusInstanceSample extends AbstractInstanceSample<PrometheusSample> {

    private final MetricName instanceName;
    private final MetricName name;
    private final String description;
    private final Collector.Type type;
    private List<PrometheusInstanceSample> children;

    public PrometheusInstanceSample(
        MetricName instanceName,
        MetricName name,
        String description,
        Collector.Type type) {

        this.instanceName = requireNonNull(instanceName);
        this.name = requireNonNull(name);
        this.description = description;
        this.type = requireNonNull(type);
    }

    public MetricName instanceName() {
        return instanceName;
    }

    public MetricName name() {
        return name;
    }

    public boolean hasDescription() {
        return !isBlank(description);
    }

    public String description() {
        return description;
    }

    public Collector.Type type() {
        return type;
    }

    @Override
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void add(PrometheusSample sample) {
        if (sample.belongsToChildInstanceSample()) {
            if (children != null) {
                for (int i = 0; i < children.size(); ++i) {
                    MetricName childName = children.get(i).name();

                    if (childName.equals(name, sample.childInstanceSampleNameSuffix())) {
                        children.get(i).add(sample.notBelongingToChildInstanceSample());
                        return;
                    }
                }
            } else {
                children = new ArrayList<>(2);
            }

            MetricName childName = MetricName.of(name, sample.childInstanceSampleNameSuffix());

            PrometheusInstanceSample child = new PrometheusInstanceSample(
                instanceName,
                childName,
                description,
                sample.childInstanceSampleType());

            child.add(sample.notBelongingToChildInstanceSample());
            children.add(child);
        } else {
            super.add(sample);
        }
    }

    @Override
    public List<PrometheusSample> samples() {
        if (type != Collector.Type.HISTOGRAM && type != Collector.Type.SUMMARY) {
            return super.samples();
        }

        if (samples.isEmpty()) {
            return samples;
        }

        samples.sort((left, right) -> {
            if (!left.hasMeasurable()) {
                return right.hasMeasurable() ? 1 : 0;
            }

            if (!right.hasMeasurable()) {
                return -1;
            }

            Measurable leftM = left.measurable();
            Measurable rightM = right.measurable();

            // Bucket
            if (leftM instanceof Bucket) {
                return
                    rightM instanceof Bucket ?
                    ((Bucket)leftM).compareTo(((Bucket)rightM)) :
                    -1;
            }

            if (rightM instanceof Bucket) {
                return 1;
            }

            // Percentile
            if (leftM instanceof Percentile) {
                return
                    rightM instanceof Percentile ?
                    ((Percentile)leftM).compareTo(((Percentile)rightM)) :
                    -1;
            }

            if (rightM instanceof Percentile) {
                return 1;
            }

            // Count
            if (leftM instanceof Count) {
                return rightM instanceof Count ? 0 : -1;
            }

            if (rightM instanceof Count) {
                return 1;
            }

            // TotalSum
            if (leftM instanceof TotalSum) {
                return rightM instanceof TotalSum ? 0 : -1;
            }

            if (rightM instanceof TotalSum) {
                return 1;
            }

            return 0;
        });

        return samples;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && !hasChildren();
    }

    public boolean hasChildren() {
        return children != null;
    }

    public List<PrometheusInstanceSample> children() {
        return hasChildren() ? children : emptyList();
    }
}
