package com.ringcentral.platform.metrics.defaultImpl.var;

import com.ringcentral.platform.metrics.test.time.TestTimeNanosProvider;
import com.ringcentral.platform.metrics.var.configs.BaseCachingVarConfig;
import org.junit.Test;

import java.util.function.Supplier;

import static com.ringcentral.platform.metrics.UnmodifiableMetricContext.emptyUnmodifiableMetricContext;
import static com.ringcentral.platform.metrics.labels.LabelValues.NO_LABEL_VALUES;
import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class DefaultCachingValueSupplierTest {

    static final long TTL_SEC = 10L;

    static final BaseCachingVarConfig CACHING_VAR_CONFIG = new BaseCachingVarConfig(
        true,
        "description",
        NO_LABEL_VALUES,
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

        DefaultCachingValueSupplier<Long> cachingValueSupplier = new DefaultCachingValueSupplier<>(
            CACHING_VAR_CONFIG,
            valueSupplier,
            timeNanosProvider);

        assumeThat(cachingValueSupplier.get(), is(1L));
        assumeThat(cachingValueSupplier.get(), is(1L));

        timeNanosProvider.increaseSec(9L);
        assumeThat(cachingValueSupplier.get(), is(1L));
        assumeThat(cachingValueSupplier.get(), is(1L));

        timeNanosProvider.increaseSec(1L);
        assumeThat(cachingValueSupplier.get(), is(2L));
        assumeThat(cachingValueSupplier.get(), is(2L));

        timeNanosProvider.increaseSec(9L);
        assumeThat(cachingValueSupplier.get(), is(2L));
        assumeThat(cachingValueSupplier.get(), is(2L));
    }
}