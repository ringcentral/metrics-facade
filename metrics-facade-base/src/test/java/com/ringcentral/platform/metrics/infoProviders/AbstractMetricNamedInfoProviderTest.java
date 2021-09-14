package com.ringcentral.platform.metrics.infoProviders;

import com.ringcentral.platform.metrics.predicates.DefaultMetricNamedPredicate;
import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.names.MetricName.*;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static com.ringcentral.platform.metrics.predicates.CompositeMetricNamedPredicateBuilder.*;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public abstract class AbstractMetricNamedInfoProviderTest {

    private final PredicativeMetricNamedInfoProvider<String> infoProvider;

    protected AbstractMetricNamedInfoProviderTest(PredicativeMetricNamedInfoProvider<String> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Test
    public void providingInfos() {
        assertThat(infoProvider.infosFor(name("a", "b")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("a", "b", "c_1")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("a", "b", "c_2")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("d", "e")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("d", "e", "f_2")), is(emptyList()));

        infoProvider.addInfo(
            forMetrics()
                .including(metricsWithNamePrefix("a.b")).excluding(metricsMatchingNameMask("a.b.c_1"))
                .including(metricsMatchingNameMask("d.e")),
            "info_1");

        infoProvider.addInfo(
            forMetrics()
                .including(metricsWithNamePrefix("a.b")).excluding(named -> named.name().equals(name("a", "b", "c_1")))
                .including(metricsMatchingNameMask("d.e")),
            "info_2");

        infoProvider.addInfo(forMetricsWithNamePrefix("d.e.f_1"), "info_2");
        infoProvider.addInfo(new DefaultMetricNamedPredicate(forMetricsWithNamePrefix("d.e.f_1"), named -> named.name().size() == 5), "info_3");
        infoProvider.addInfo(named -> named.name().size() == 7, "info_4");

        assertThat(infoProvider.infosFor(name("a", "b")), is(List.of("info_1", "info_2")));
        assertThat(infoProvider.infosFor(name("a", "b", "c_1")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("a", "b", "c_2")), is(List.of("info_1", "info_2")));
        assertThat(infoProvider.infosFor(name("d", "e")), is(List.of("info_1", "info_2")));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1")), is(List.of( "info_2")));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1", "g", "h")), is(List.of( "info_2", "info_3")));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1", "g", "h", "i", "j")), is(List.of( "info_2", "info_4")));
        assertThat(infoProvider.infosFor(name("d", "e", "f_2")), is(emptyList()));

        infoProvider.addInfo(
            forMetrics()
                .including(metricsMatchingNameMask("k.**.m"))
                .excluding(metricWithName("k.l_1.m"))
                .excluding(metricsMatchingNameMask("k.**.n.m")),
            "info_5");

        assertThat(infoProvider.infosFor(name("k", "l_1", "m")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("k", "l_2", "m")), is(List.of("info_5")));
        assertThat(infoProvider.infosFor(name("k", "l_2", "n", "m")), is(emptyList()));
    }
}