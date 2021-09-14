package com.ringcentral.platform.metrics.infoProviders;

import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.names.MetricName.*;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class MetricNamedInfoProviderTest {

    @Test
    public void theLastInfoShouldTakePrecedence_When_ProvidingSingleInfo() {
        MetricNamedInfoProvider<String> provider = named -> emptyList();
        assertThat(provider.infoFor(name("n")), is(nullValue()));

        provider = named -> List.of("1", "2");
        assertThat(provider.infoFor(name("n")), is("2"));
    }
}