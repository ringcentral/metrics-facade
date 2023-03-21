package com.ringcentral.platform.metrics.measurables;

import com.ringcentral.platform.metrics.*;
import com.ringcentral.platform.metrics.counter.CounterInstance;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.histogram.HistogramInstance;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.rate.RateInstance;
import com.ringcentral.platform.metrics.timer.TimerInstance;
import com.ringcentral.platform.metrics.var.VarInstance;
import org.junit.Test;

import java.util.*;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.measurables.MeasurableType.OBJECT;
import static com.ringcentral.platform.metrics.rate.Rate.*;
import static com.ringcentral.platform.metrics.timer.Timer.DURATION_UNIT;
import static com.ringcentral.platform.metrics.var.objectVar.ObjectVar.OBJECT_VALUE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class DefaultMeasurableNameProviderTest {

    DefaultMeasurableNameProvider p = new DefaultMeasurableNameProvider();

    static class UnsupportedMetricInstance implements MetricInstance {
        @Override public List<LabelValue> labelValues() { return null; }
        @Override public boolean isTotalInstance() { return false; }
        @Override public boolean isLabeledMetricTotalInstance() { return false; }
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
        assertThat(p.nameFor(instance, TOTAL_SUM), is("duration.totalSum"));
        assertThat(p.nameFor(instance, MIN), is("duration.min"));
        assertThat(p.nameFor(instance, MAX), is("duration.max"));
        assertThat(p.nameFor(instance, MEAN), is("duration.mean"));
        assertThat(p.nameFor(instance, STANDARD_DEVIATION), is("duration.stdDev"));

        assertThat(p.nameFor(instance, PERCENTILE_5), is("duration.05_percentile"));
        assertThat(p.nameFor(instance, PERCENTILE_50), is("duration.50_percentile"));
        assertThat(p.nameFor(instance, PERCENTILE_999), is("duration.999_percentile"));

        assertThat(p.nameFor(instance, MS_5_BUCKET), is("duration.5ms_bucket"));
        assertThat(p.nameFor(instance, MS_10_BUCKET), is("duration.10ms_bucket"));
        assertThat(p.nameFor(instance, MS_25_BUCKET), is("duration.25ms_bucket"));
        assertThat(p.nameFor(instance, MS_50_BUCKET), is("duration.50ms_bucket"));
        assertThat(p.nameFor(instance, MS_75_BUCKET), is("duration.75ms_bucket"));
        assertThat(p.nameFor(instance, MS_100_BUCKET), is("duration.100ms_bucket"));
        assertThat(p.nameFor(instance, MS_250_BUCKET), is("duration.250ms_bucket"));
        assertThat(p.nameFor(instance, MS_500_BUCKET), is("duration.500ms_bucket"));
        assertThat(p.nameFor(instance, MS_750_BUCKET), is("duration.750ms_bucket"));
        assertThat(p.nameFor(instance, SEC_1_BUCKET), is("duration.1sec_bucket"));
        assertThat(p.nameFor(instance, SEC_2p5_BUCKET), is("duration.2p5sec_bucket"));
        assertThat(p.nameFor(instance, SEC_5_BUCKET), is("duration.5sec_bucket"));
        assertThat(p.nameFor(instance, SEC_7p5_BUCKET), is("duration.7p5sec_bucket"));
        assertThat(p.nameFor(instance, SEC_10_BUCKET), is("duration.10sec_bucket"));
        assertThat(p.nameFor(instance, SEC_20_BUCKET), is("duration.20sec_bucket"));
        assertThat(p.nameFor(instance, SEC_30_BUCKET), is("duration.30sec_bucket"));
        assertThat(p.nameFor(instance, INF_BUCKET), is("duration.inf_bucket"));

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
        assertThat(p.nameFor(instance, TOTAL_SUM), is("totalSum"));
        assertThat(p.nameFor(instance, MIN), is("min"));
        assertThat(p.nameFor(instance, MAX), is("max"));
        assertThat(p.nameFor(instance, MEAN), is("mean"));
        assertThat(p.nameFor(instance, STANDARD_DEVIATION), is("stdDev"));

        assertThat(p.nameFor(instance, PERCENTILE_5), is("05_percentile"));
        assertThat(p.nameFor(instance, PERCENTILE_50), is("50_percentile"));
        assertThat(p.nameFor(instance, PERCENTILE_999), is("999_percentile"));

        assertThat(p.nameFor(instance, Bucket.of(5)), is("5_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(10)), is("10_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(25)), is("25_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(50)), is("50_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(75)), is("75_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(100)), is("100_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(250)), is("250_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(500)), is("500_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(750)), is("750_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(1)), is("1_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(2.5)), is("2p5_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(5)), is("5_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(7.5)), is("7p5_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(10)), is("10_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(20)), is("20_bucket"));
        assertThat(p.nameFor(instance, Bucket.of(30)), is("30_bucket"));
        assertThat(p.nameFor(instance, INF_BUCKET), is("inf_bucket"));

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