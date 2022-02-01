package com.ringcentral.platform.metrics.utils;

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CachingSupplierTest {

    @Test
    public void caching() {
        AtomicLong counter = new AtomicLong();
        TestTimeNanosProvider timeNanosProvider = new TestTimeNanosProvider();
        CachingSupplier<Long> cachingSupplier = new CachingSupplier<>(counter::incrementAndGet, Duration.ofSeconds(10), timeNanosProvider);
        assertThat(cachingSupplier.get(), is(1L));
        timeNanosProvider.increaseSec(10);
        assertThat(cachingSupplier.get(), is(1L));
        timeNanosProvider.increaseSec(1);
        assertThat(cachingSupplier.get(), is(2L));
    }
}