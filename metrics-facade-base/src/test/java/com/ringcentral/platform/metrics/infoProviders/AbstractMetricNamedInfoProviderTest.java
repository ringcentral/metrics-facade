package com.ringcentral.platform.metrics.infoProviders;

import com.ringcentral.platform.metrics.MetricInstance;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.predicates.DefaultMetricNamedPredicate;
import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.names.MetricName.name;
import static com.ringcentral.platform.metrics.names.MetricName.withName;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static com.ringcentral.platform.metrics.predicates.CompositeMetricNamedPredicateBuilder.forMetrics;
import static com.ringcentral.platform.metrics.predicates.DefaultMetricInstancePredicate.metricInstancesMatching;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractMetricNamedInfoProviderTest {

    private final PredicativeMetricNamedInfoProvider<String> infoProvider;

    protected AbstractMetricNamedInfoProviderTest(PredicativeMetricNamedInfoProvider<String> infoProvider) {
        this.infoProvider = infoProvider;
    }

    @Test
    public void providingInfos() {
        assertThat(infoProvider.infosFor(name("a", "b")), is(emptyList()));
        assertThat(infoProvider.infosFor(metricInstance(withName("a", "b"))), is(emptyList()));

        assertThat(infoProvider.infosFor(name("a", "b", "c_1")), is(emptyList()));
        assertThat(infoProvider.infosFor(metricInstance(withName("a", "b", "c_1"))), is(emptyList()));

        assertThat(infoProvider.infosFor(name("a", "b", "c_2")), is(emptyList()));
        assertThat(infoProvider.infosFor(metricInstance(withName("a", "b", "c_2"))), is(emptyList()));

        assertThat(infoProvider.infosFor(name("d", "e")), is(emptyList()));
        assertThat(infoProvider.infosFor(metricInstance(withName("d", "e"))), is(emptyList()));

        assertThat(infoProvider.infosFor(name("d", "e", "f_1")), is(emptyList()));
        assertThat(infoProvider.infosFor(metricInstance(withName("d", "e", "f_1"))), is(emptyList()));

        assertThat(infoProvider.infosFor(name("d", "e", "f_2")), is(emptyList()));
        assertThat(infoProvider.infosFor(metricInstance(withName("d", "e", "f_2"))), is(emptyList()));

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
        assertThat(infoProvider.infosFor(metricInstance(withName("a", "b"))), is(List.of("info_1", "info_2")));

        assertThat(infoProvider.infosFor(name("a", "b", "c_1")), is(emptyList()));
        assertThat(infoProvider.infosFor(metricInstance(withName("a", "b", "c_1"))), is(emptyList()));

        assertThat(infoProvider.infosFor(name("a", "b", "c_2")), is(List.of("info_1", "info_2")));
        assertThat(infoProvider.infosFor(metricInstance(withName("a", "b", "c_2"))), is(List.of("info_1", "info_2")));

        assertThat(infoProvider.infosFor(name("d", "e")), is(List.of("info_1", "info_2")));
        assertThat(infoProvider.infosFor(metricInstance(withName("d", "e"))), is(List.of("info_1", "info_2")));

        assertThat(infoProvider.infosFor(name("d", "e", "f_1")), is(List.of("info_2")));
        assertThat(infoProvider.infosFor(metricInstance(withName("d", "e", "f_1"))), is(List.of("info_2")));

        assertThat(infoProvider.infosFor(name("d", "e", "f_1", "g", "h")), is(List.of("info_2", "info_3")));
        assertThat(infoProvider.infosFor(metricInstance(withName("d", "e", "f_1", "g", "h"))), is(List.of("info_2", "info_3")));

        assertThat(infoProvider.infosFor(name("d", "e", "f_1", "g", "h", "i", "j")), is(List.of("info_2", "info_4")));
        assertThat(infoProvider.infosFor(metricInstance(withName("d", "e", "f_1", "g", "h", "i", "j"))), is(List.of("info_2", "info_4")));

        assertThat(infoProvider.infosFor(name("d", "e", "f_2")), is(emptyList()));
        assertThat(infoProvider.infosFor(metricInstance(withName("d", "e", "f_2"))), is(emptyList()));

        infoProvider.addInfo(
            forMetrics()
                .including(metricInstancesMatching(metricsMatchingNameMask("k.**.m")))
                .excluding(metricInstancesMatching(metricWithName("k.l_1.m")))
                .excluding(metricInstancesMatching(metricsMatchingNameMask("k.**.n.m"))),
            "info_5");

        assertThat(infoProvider.infosFor(name("k", "l_1", "m")), is(emptyList()));
        assertThat(infoProvider.infosFor(metricInstance(withName("k", "l_1", "m"))), is(emptyList()));

        assertThat(infoProvider.infosFor(name("k", "l_2", "m")), is(List.of("info_5")));
        assertThat(infoProvider.infosFor(metricInstance(withName("k", "l_2", "m"))), is(List.of("info_5")));

        assertThat(infoProvider.infosFor(name("k", "l_2", "n", "m")), is(emptyList()));
        assertThat(infoProvider.infosFor(metricInstance(withName("k", "l_2", "n", "m"))), is(emptyList()));
    }

    @Test
    public void removingInfos() {
        // add infos
        infoProvider.addInfo(
            "key_1",
            forMetrics()
                .including(metricsWithNamePrefix("a.b")).excluding(metricsMatchingNameMask("a.b.c_1"))
                .including(metricsMatchingNameMask("d.e")),
            "info_1");

        infoProvider.addInfo(
            "key_2",
            forMetrics()
                .including(metricsWithNamePrefix("a.b")).excluding(named -> named.name().equals(name("a", "b", "c_1")))
                .including(metricsMatchingNameMask("d.e")),
            "info_2");

        infoProvider.addInfo("key_3", forMetricsWithNamePrefix("d.e.f_1"), "info_3");
        infoProvider.addInfo("key_4", new DefaultMetricNamedPredicate(forMetricsWithNamePrefix("d.e.f_1"), named -> named.name().size() == 5), "info_4");
        infoProvider.addInfo("key_5", named -> named.name().size() == 7, "info_5");

        infoProvider.addInfo(
            "key_6",
            forMetrics()
                .including(metricInstancesMatching(metricsMatchingNameMask("k.**.m")))
                .excluding(metricInstancesMatching(metricWithName("k.l_1.m")))
                .excluding(metricInstancesMatching(metricsMatchingNameMask("k.**.n.m"))),
            "info_6");

        // check infos
        assertThat(infoProvider.infosFor(name("a", "b")), is(List.of("info_1", "info_2")));
        assertThat(infoProvider.infosFor(name("a", "b", "c_2")), is(List.of("info_1", "info_2")));
        assertThat(infoProvider.infosFor(name("d", "e")), is(List.of("info_1", "info_2")));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1")), is(List.of("info_3")));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1", "g", "h")), is(List.of("info_3", "info_4")));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1", "g", "h", "i", "j")), is(List.of("info_3", "info_5")));
        assertThat(infoProvider.infosFor(name("k", "l_2", "m")), is(List.of("info_6")));

        // remove key_1, key_2
        infoProvider.removeInfo("key_1").removeInfo("key_2");

        // check infos
        assertThat(infoProvider.infosFor(name("a", "b")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("a", "b", "c_2")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("d", "e")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1")), is(List.of("info_3")));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1", "g", "h")), is(List.of("info_3", "info_4")));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1", "g", "h", "i", "j")), is(List.of("info_3", "info_5")));
        assertThat(infoProvider.infosFor(name("k", "l_2", "m")), is(List.of("info_6")));

        // remove key_3, key_5
        infoProvider.removeInfos(key -> key.endsWith("3") || key.endsWith("5"));

        // check infos
        assertThat(infoProvider.infosFor(name("a", "b")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("a", "b", "c_2")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("d", "e")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1", "g", "h")), is(List.of("info_4")));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1", "g", "h", "i", "j")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("k", "l_2", "m")), is(List.of("info_6")));

        // remove key_4, key_6
        infoProvider.removeInfo("key_4").removeInfo("key_6");

        // check infos
        assertThat(infoProvider.infosFor(name("a", "b")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("a", "b", "c_2")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("d", "e")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1", "g", "h")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("d", "e", "f_1", "g", "h", "i", "j")), is(emptyList()));
        assertThat(infoProvider.infosFor(name("k", "l_2", "m")), is(emptyList()));
    }

    MetricInstance metricInstance(MetricName name) {
        MetricInstance metricInstance = mock(MetricInstance.class);
        when(metricInstance.name()).thenReturn(name);
        return metricInstance;
    }
}