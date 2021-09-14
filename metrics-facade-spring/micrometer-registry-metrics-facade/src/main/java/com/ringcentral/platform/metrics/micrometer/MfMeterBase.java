package com.ringcentral.platform.metrics.micrometer;

import com.ringcentral.platform.metrics.MetricRegistry;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.names.MetricName;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Tag;

import java.util.*;

import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;

public class MfMeterBase {

    private final MetricRegistry mfRegistry;
    private final Id id;
    private final MetricName name;
    private final List<MetricDimension> dimensions;
    private final MetricDimensionValues dimensionValues;

    public MfMeterBase(MetricRegistry mfRegistry, Id id) {
        this.mfRegistry = mfRegistry;
        this.id = id;
        this.name = MetricName.fromDotSeparated(id.getName());
        List<Tag> tags = id.getTags();

        if (tags.isEmpty()) {
            this.dimensions = null;
            this.dimensionValues = NO_DIMENSION_VALUES;
        } else {
            this.dimensions = new ArrayList<>(tags.size());
            List<MetricDimensionValue> dimensionValueList = new ArrayList<>(tags.size());

            tags.forEach(tag -> {
                MetricDimension dimension = new MetricDimension(tag.getKey());
                dimensions.add(dimension);
                dimensionValueList.add(new MetricDimensionValue(dimension, tag.getValue()));
            });

            this.dimensionValues = forDimensionValues(dimensionValueList);
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

    public boolean hasDimensions() {
        return dimensions != null && !dimensions.isEmpty();
    }

    public List<MetricDimension> dimensions() {
        return dimensions;
    }

    public MetricDimensionValues dimensionValues() {
        return dimensionValues;
    }
}
