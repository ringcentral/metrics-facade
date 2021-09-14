package com.ringcentral.platform.metrics.predicates;

import com.ringcentral.platform.metrics.names.MetricName;
import org.junit.Test;

import static com.ringcentral.platform.metrics.names.MetricName.*;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static com.ringcentral.platform.metrics.predicates.CompositeMetricNamedPredicateBuilder.*;
import static org.junit.Assert.*;

public class CompositeMetricNamedPredicateTest {

    @Test
    public void empty() {
        MetricNamedPredicate p = metrics().build();
        MetricName n = name("A");
        assertTrue(p.matches(n));
    }

    @Test
    public void inclusionPredicatesOnly() {
        MetricNamedPredicate p = metrics().including(metricWithName("A")).build();
        assertTrue(p.matches(name("A")));
        assertFalse(p.matches(name("B")));

        p = metrics().including(metricWithName("A")).including(metricsWithNamePrefix("B")).build();
        assertTrue(p.matches(name("A")));
        assertFalse(p.matches(name("A", "A")));
        assertTrue(p.matches(name("B")));
        assertTrue(p.matches(name("B", "B")));
        assertFalse(p.matches(emptyMetricName()));
    }

    @Test
    public void exclusionPredicatesOnly() {
        MetricNamedPredicate p = metrics().excluding(metricWithName("A")).build();
        assertFalse(p.matches(name("A")));
        assertTrue(p.matches(name("B")));

        p = metrics().excluding(metricWithName("A")).excluding(metricsWithNamePrefix("B")).build();
        assertFalse(p.matches(name("A")));
        assertTrue(p.matches(name("A", "A")));
        assertFalse(p.matches(name("B")));
        assertFalse(p.matches(name("B", "B")));
        assertTrue(p.matches(emptyMetricName()));
    }

    @Test
    public void inclusionAndExclusionPredicates() {
        MetricNamedPredicate p = forMetrics().including(metricWithName("A")).excluding(metricsWithNamePrefix("A.B")).build();
        assertTrue(p.matches(name("A")));
        assertFalse(p.matches(name("A", "A")));
        assertFalse(p.matches(name("A", "B")));
        assertFalse(p.matches(name("A", "B", "C")));
        assertFalse(p.matches(name("B")));
        assertFalse(p.matches(name("B", "B")));
        assertFalse(p.matches(emptyMetricName()));
    }
}