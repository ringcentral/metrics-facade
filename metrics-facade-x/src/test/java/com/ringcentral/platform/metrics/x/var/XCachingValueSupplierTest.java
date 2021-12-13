package com.ringcentral.platform.metrics.x.var;

import com.ringcentral.platform.metrics.UnmodifiableMetricContext;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.test.time.TestTimeNanosProvider;
import com.ringcentral.platform.metrics.utils.TimeNanosProvider;
import com.ringcentral.platform.metrics.var.configs.BaseCachingVarConfig;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.UnmodifiableMetricContext.emptyUnmodifiableMetricContext;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.NO_DIMENSION_VALUES;
import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class XCachingValueSupplierTest {

    static final long TTL_SEC = 10L;

    static final BaseCachingVarConfig CACHING_VAR_CONFIG = new BaseCachingVarConfig(
        true,
        "description",
        NO_DIMENSION_VALUES,
        emptyList(),
        true,
        emptyUnmodifiableMetricContext(),
        TTL_SEC,
        SECONDS);

    TestTimeNanosProvider timeNanosProvider = new TestTimeNanosProvider();

    @Test
    public void cachingValue() {
        Supplier<Long> valueSupplier = mock(Supplier.class);
        when(valueSupplier.get()).thenReturn(1L, 2L);

        XCachingValueSupplier<Long> xCachingValueSupplier = new XCachingValueSupplier<>(
            CACHING_VAR_CONFIG,
            valueSupplier,
            timeNanosProvider);

        assumeThat(xCachingValueSupplier.get(), is(1L));
        assumeThat(xCachingValueSupplier.get(), is(1L));

        timeNanosProvider.increaseSec(9L);
        assumeThat(xCachingValueSupplier.get(), is(1L));
        assumeThat(xCachingValueSupplier.get(), is(1L));

        timeNanosProvider.increaseSec(1L);
        assumeThat(xCachingValueSupplier.get(), is(2L));
        assumeThat(xCachingValueSupplier.get(), is(2L));

        timeNanosProvider.increaseSec(9L);
        assumeThat(xCachingValueSupplier.get(), is(2L));
        assumeThat(xCachingValueSupplier.get(), is(2L));
    }
}