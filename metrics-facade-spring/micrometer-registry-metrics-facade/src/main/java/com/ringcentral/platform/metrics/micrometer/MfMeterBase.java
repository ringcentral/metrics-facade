package com.ringcentral.platform.metrics.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.names.MetricName;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Tag;

import java.util.*;

import static com.ringcentral.platform.metrics.labels.LabelValues.*;

public class MfMeterBase {

    private final MetricRegistry mfRegistry;
    private final Id id;
    private final MetricName name;
    private final List<Label> labels;
    private final LabelValues labelValues;

    public MfMeterBase(MetricRegistry mfRegistry, Id id) {
        this.mfRegistry = mfRegistry;
        this.id = id;
        this.name = MetricName.fromDotSeparated(id.getName());
        List<Tag> tags = id.getTags();

        if (tags.isEmpty()) {
            this.labels = null;
            this.labelValues = NO_LABEL_VALUES;
        } else {
            this.labels = new ArrayList<>(tags.size());
            List<LabelValue> labelValueList = new ArrayList<>(tags.size());

            tags.forEach(tag -> {
                Label label = new Label(tag.getKey());
                labels.add(label);
                labelValueList.add(new LabelValue(label, tag.getValue()));
            });

            this.labelValues = forLabelValues(labelValueList);
        }
    }

    public MetricRegistry mfRegistry() {
        return mfRegistry;
    }

    public Id id() {
        return id;
    }

    public MetricName name() {
        return name;
    }

    public boolean hasLabels() {
        return labels != null && !labels.isEmpty();
    }

    public List<Label> labels() {
        return labels;
    }

    public LabelValues labelValues() {
        return labelValues;
    }
}
