package com.ringcentral.platform.metrics.measurables;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateInstance;
import com.ringcentral.platform.metrics.timer.TimerInstance;
import com.ringcentral.platform.metrics.var.VarInstance;
import org.junit.Test;

import java.util.*;

import static com.ringcentral.platform.metrics.counter.Counter.*;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.measurables.MeasurableType.*;
import static com.ringcentral.platform.metrics.rate.Rate.*;
import static com.ringcentral.platform.metrics.timer.Timer.*;
import static com.ringcentral.platform.metrics.var.objectVar.ObjectVar.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

public class DefaultMeasurableNameProviderTest {

    DefaultMeasurableNameProvider p = new DefaultMeasurableNameProvider();

    static class UnsupportedMetricInstance implements MetricInstance {
        @Override public List<MetricDimensionValue> dimensionValues() { return null; }
        @Override public boolean isTotalInstance() { return false; }
        @Override public boolean isDimensionalTotalInstance() { return false; }
        @Override public boolean isLevelInstance() { return false; }
        @Override public Set<Measurable> measurables() { return null; }
        @Override public MeasurableValues measurableValues() { return null; }
        @Override public <V> V valueOf(Measurable measurable) throws NotMeasuredException { return null; }
        @Override public MetricName name() { return null; }
    }

    UnsupportedMetricInstance unsupportedMetricInstance = new UnsupportedMetricInstance();

    static class UnsupportedMeasurable implements Measurable {

        @Override
        public MeasurableType type() {
            return OBJECT;
        }
    }

    UnsupportedMeasurable unsupportedMeasurable = new UnsupportedMeasurable();

    @Test
    public void timerInstance() {
        TimerInstance instance = mock(TimerInstance.class);
        assertThat(p.nameFor(instance, COUNT), is("count"));
        assertThat(p.nameFor(instance, MEAN_RATE), is("rate.mean"));
        assertThat(p.nameFor(instance, ONE_MINUTE_RATE), is("rate.1_minute"));
        assertThat(p.nameFor(instance, FIVE_MINUTES_RATE), is("rate.5_minutes"));
        assertThat(p.nameFor(instance, FIFTEEN_MINUTES_RATE), is("rate.15_minutes"));
        assertThat(p.nameFor(instance, RATE_UNIT), is("rate.unit"));
        assertThat(p.nameFor(instance, MIN), is("duration.min"));
        assertThat(p.nameFor(instance, MAX), is("duration.max"));
        assertThat(p.nameFor(instance, MEAN), is("duration.mean"));
        assertThat(p.nameFor(instance, STANDARD_DEVIATION), is("duration.stdDev"));
        assertThat(p.nameFor(instance, PERCENTILE_5), is("duration.05_percentile"));
        assertThat(p.nameFor(instance, PERCENTILE_50), is("duration.50_percentile"));
        assertThat(p.nameFor(instance, PERCENTILE_999), is("duration.999_percentile"));
        assertThat(p.nameFor(instance, DURATION_UNIT), is("duration.unit"));
        assertThat(p.nameFor(instance, unsupportedMeasurable), is("unsupportedmeasurable"));
    }

    @Test
    public void counterInstance() {
        CounterInstance instance = mock(CounterInstance.class);
        assertThat(p.nameFor(instance, COUNT), is("count"));
        assertThat(p.nameFor(instance, unsupportedMeasurable), is("unsupportedmeasurable"));
    }

    @Test
    public void histogramInstance() {
        HistogramInstance instance = mock(HistogramInstance.class);
        assertThat(p.nameFor(instance, COUNT), is("count"));
        assertThat(p.nameFor(instance, MIN), is("min"));
        assertThat(p.nameFor(instance, MAX), is("max"));
        assertThat(p.nameFor(instance, MEAN), is("mean"));
        assertThat(p.nameFor(instance, STANDARD_DEVIATION), is("stdDev"));
        assertThat(p.nameFor(instance, PERCENTILE_5), is("05_percentile"));
        assertThat(p.nameFor(instance, PERCENTILE_50), is("50_percentile"));
        assertThat(p.nameFor(instance, PERCENTILE_999), is("999_percentile"));
        assertThat(p.nameFor(instance, unsupportedMeasurable), is("unsupportedmeasurable"));
    }

    @Test
    public void rateInstance() {
        RateInstance instance = mock(RateInstance.class);
        assertThat(p.nameFor(instance, COUNT), is("count"));
        assertThat(p.nameFor(instance, MEAN_RATE), is("rate.mean"));
        assertThat(p.nameFor(instance, ONE_MINUTE_RATE), is("rate.1_minute"));
        assertThat(p.nameFor(instance, FIVE_MINUTES_RATE), is("rate.5_minutes"));
        assertThat(p.nameFor(instance, FIFTEEN_MINUTES_RATE), is("rate.15_minutes"));
        assertThat(p.nameFor(instance, RATE_UNIT), is("rate.unit"));
        assertThat(p.nameFor(instance, unsupportedMeasurable), is("unsupportedmeasurable"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void varInstance() {
        VarInstance<Object> instance = mock(VarInstance.class);
        assertThat(p.nameFor(instance, OBJECT_VALUE), is("value"));
        assertThat(p.nameFor(instance, unsupportedMeasurable), is("unsupportedmeasurable"));
    }

    @Test
    public void unsupportedMetricInstance() {
        assertThat(p.nameFor(unsupportedMetricInstance, OBJECT_VALUE), is("unsupportedmetricinstance.objectvalue"));
    }
}