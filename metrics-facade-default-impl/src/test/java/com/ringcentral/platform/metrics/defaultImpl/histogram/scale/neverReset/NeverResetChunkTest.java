package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.neverReset;

import com.ringcentral.platform.metrics.defaultImpl.histogram.AbstractHistogramImpl.MeasurementSpec;
import com.ringcentral.platform.metrics.histogram.HistogramMeasurable;
import com.ringcentral.platform.metrics.scale.LinearScale;
import org.junit.Test;

import java.util.Set;

import static com.ringcentral.platform.metrics.counter.Counter.COUNT;
import static com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfigBuilder.scaleImpl;
import static com.ringcentral.platform.metrics.histogram.Histogram.*;
import static com.ringcentral.platform.metrics.scale.LinearScaleBuilder.linearScale;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NeverResetChunkTest {

    static final LinearScale SCALE_1 = linearScale().from(1).steps(1, 99).build();

    @Test
    public void snapshotIsConsistent() {
        Set<HistogramMeasurable> measurables = Set.of(
            COUNT,
            TOTAL_SUM,
            MIN,
            MAX,
            MEAN,
            STANDARD_DEVIATION,

            // percentiles
            PERCENTILE_1,
            PERCENTILE_5,
            PERCENTILE_15,
            PERCENTILE_25,
            PERCENTILE_35,
            PERCENTILE_45,
            PERCENTILE_50,
            PERCENTILE_55,
            PERCENTILE_70,
            PERCENTILE_75,
            PERCENTILE_80,
            PERCENTILE_95,
            PERCENTILE_99,
            PERCENTILE_999,

            // buckets
            Bucket.of(-5),
            Bucket.of(1),
            Bucket.of(2),
            Bucket.of(3),
            Bucket.of(4),
            Bucket.of(5),
            Bucket.of(27),
            Bucket.of(50),
            Bucket.of(88),
            Bucket.of(107));

        NeverResetChunk chunk = new NeverResetChunk(
            scaleImpl().maxLazyTreeLevel(1).neverReset().with(SCALE_1).build(),
            new MeasurementSpec(
                measurables,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                measurables.stream().filter(m -> m instanceof Percentile).mapToDouble(p -> ((Percentile)p).quantile()).toArray(),
                measurables.stream().filter(m -> m instanceof Percentile).mapToDouble(p -> ((Percentile)p).percentile()).toArray(),
                true,
                upperBoundsOf(measurables.stream().filter(m -> m instanceof Bucket).map(m -> (Bucket)m).collect(toSet()))));

        for (int v = 1; v <= 50; ++v) {
            chunk.update(v);
        }

        chunk.startSnapshot();
        chunk.calcLazySubtreeUpdateCounts();
        assertThat(chunk.max(), is(50L));
        chunk.endSnapshot();

        chunk.startSnapshot();
        chunk.calcLazySubtreeUpdateCounts();

        for (int v = 51; v <= 75; ++v) {
            chunk.update(v);
        }

        assertThat(chunk.max(), is(50L));
        chunk.endSnapshot();

        chunk.startSnapshot();
        chunk.calcLazySubtreeUpdateCounts();
        assertThat(chunk.max(), is(75L));
        chunk.endSnapshot();

        for (int v = 76; v <= 85; ++v) {
            chunk.update(v);
        }

        chunk.startSnapshot();
        chunk.calcLazySubtreeUpdateCounts();
        assertThat(chunk.max(), is(85L));
        chunk.endSnapshot();

        chunk.startSnapshot();
        chunk.calcLazySubtreeUpdateCounts();

        for (int v = 86; v <= 100; ++v) {
            chunk.update(v);
        }

        assertThat(chunk.max(), is(85L));
        chunk.endSnapshot();

        chunk.startSnapshot();
        chunk.calcLazySubtreeUpdateCounts();
        assertThat(chunk.max(), is(100L));
        chunk.endSnapshot();
    }

    long[] upperBoundsOf(Set<Bucket> buckets) {
        long[] bounds = buckets.stream()
            .mapToLong(Bucket::upperBoundAsLong)
            .sorted()
            .toArray();

        if (bounds[bounds.length - 1] != Long.MAX_VALUE) {
            long[] boundsWithInf = new long[bounds.length + 1];
            System.arraycopy(bounds, 0, boundsWithInf, 0, bounds.length);
            boundsWithInf[bounds.length] = Long.MAX_VALUE;
            bounds = boundsWithInf;
        }

        return bounds;
    }
}