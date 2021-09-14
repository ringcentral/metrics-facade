package com.ringcentral.platform.metrics.names;

import org.junit.Test;

import static com.ringcentral.platform.metrics.names.MetricName.*;
import static com.ringcentral.platform.metrics.names.MetricNameMask.ItemType.*;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static org.junit.Assert.*;

public class MetricNameMaskTest {

    @Test
    public void matching() {
        MetricNameMask m = MetricNameMask.of("**");
        assertTrue(m.matches(emptyMetricName()));
        assertTrue(m.matches(name("a")));
        assertTrue(m.matches(name("a", "b")));

        m = allMetrics();
        assertTrue(m.matches(emptyMetricName()));
        assertTrue(m.matches(name("a")));
        assertTrue(m.matches(name("a", "b")));

        m = MetricNameMask.of("a");
        assertTrue(m.matches(name("a")));
        assertFalse(m.matches(emptyMetricName()));
        assertFalse(m.matches(name("a", "b")));
        assertFalse(m.matches(name("b", "a")));

        m = forMetricWithName("a.b");
        assertTrue(m.matches(name("a", "b")));
        assertFalse(m.matches(emptyMetricName()));
        assertFalse(m.matches(name("a")));
        assertFalse(m.matches(name("a", "b", "c")));
        assertFalse(m.matches(name("b", "c", "a")));

        m = MetricNameMask.of("**.b");
        assertTrue(m.matches(name("b")));
        assertTrue(m.matches(name("a", "b")));
        assertTrue(m.matches(name("a", "c", "b")));
        assertFalse(m.matches(emptyMetricName()));
        assertFalse(m.matches(name("a")));
        assertFalse(m.matches(name("b", "c")));
        assertFalse(m.matches(name("a", "b", "c")));

        m = MetricNameMask.of("**.b.c");
        assertTrue(m.matches(name("b", "c")));
        assertTrue(m.matches(name("a", "b", "c")));
        assertFalse(m.matches(emptyMetricName()));
        assertFalse(m.matches(name("a")));
        assertFalse(m.matches(name("a", "b", "c", "d")));
        assertFalse(m.matches(name("b", "a", "c")));

        m = MetricNameMask.of("**.b.c.**.d.**.e.f");
        assertTrue(m.matches(name("b", "c", "d", "e", "f")));
        assertTrue(m.matches(name("x", "b", "c", "x", "x", "d", "x", "x", "x", "e", "f")));
        assertFalse(m.matches(emptyMetricName()));
        assertFalse(m.matches(name("a")));
        assertFalse(m.matches(name("a", "b", "c", "d")));
        assertFalse(m.matches(name("b", "a", "c")));
        assertFalse(m.matches(name("x", "b", "x", "c", "x", "x", "d", "x", "x", "x", "e", "f")));
        assertFalse(m.matches(name("x", "b", "x", "d", "x", "x", "x", "e", "f")));
        assertFalse(m.matches(name("x", "b", "c", "x", "x", "d", "x", "x", "x", "e", "f", "x")));

        m = forMetricsWithNamePrefix("b.c");
        assertTrue(m.matches(name("b", "c")));
        assertTrue(m.matches(name("b", "c", "a")));
        assertFalse(m.matches(emptyMetricName()));
        assertFalse(m.matches(name("a")));
        assertFalse(m.matches(name("a", "b", "c", "d")));
        assertFalse(m.matches(name("b", "a", "c")));

        m = MetricNameMask.of("b.c.**.d.**.e.f.**");
        assertTrue(m.matches(name("b", "c", "d", "e", "f")));
        assertTrue(m.matches(name("b", "c", "x", "x", "d", "x", "x", "x", "e", "f", "x")));
        assertFalse(m.matches(emptyMetricName()));
        assertFalse(m.matches(name("a")));
        assertFalse(m.matches(name("a", "b", "c", "d")));
        assertFalse(m.matches(name("b", "a", "c")));
        assertFalse(m.matches(name("x", "b", "x", "c", "x", "x", "d", "x", "x", "x", "e", "f")));
        assertFalse(m.matches(name("x", "b", "x", "d", "x", "x", "x", "e", "f")));
        assertFalse(m.matches(name("x", "b", "c", "x", "x", "d", "x", "x", "x", "e", "f", "x")));

        m = MetricNameMask.of("**.b.c.**");
        assertTrue(m.matches(name("b", "c")));
        assertTrue(m.matches(name("a", "b", "c")));
        assertTrue(m.matches(name("b", "c")));
        assertTrue(m.matches(name("b", "c", "a")));
        assertTrue(m.matches(name("a", "b", "c", "d")));
        assertFalse(m.matches(emptyMetricName()));
        assertFalse(m.matches(name("a")));
        assertFalse(m.matches(name("b", "a", "c")));

        m = MetricNameMask.of("**.b.c.**.d.**.e.f.**");
        assertTrue(m.matches(name("b", "c", "d", "e", "f")));
        assertTrue(m.matches(name("x", "b", "c", "x", "x", "d", "x", "x", "x", "e", "f")));
        assertTrue(m.matches(name("b", "c", "d", "e", "f")));
        assertTrue(m.matches(name("b", "c", "x", "x", "d", "x", "x", "x", "e", "f", "x")));
        assertTrue(m.matches(name("x", "b", "c", "x", "x", "d", "x", "x", "x", "e", "f", "x")));
        assertFalse(m.matches(emptyMetricName()));
        assertFalse(m.matches(name("a")));
        assertFalse(m.matches(name("a", "b", "c", "d")));
        assertFalse(m.matches(name("b", "a", "c")));
        assertFalse(m.matches(name("x", "b", "x", "c", "x", "x", "d", "x", "x", "x", "e", "f")));
        assertFalse(m.matches(name("x", "b", "x", "d", "x", "x", "x", "e", "f")));

        m = MetricNameMask.of("**.m");
        assertTrue(m.matches(name("k", "l_2", "m"), 1));
    }

    @Test
    public void submask() {
        MetricNameMask m = forMetricsMatchingNameMask("**").submask(0);
        assertEquals(1, m.size());
        assertEquals(ANY_PARTS, m.item(0).type());

        m = metricWithName("a.b").submask(0);
        assertEquals(2, m.size());
        assertEquals("a", m.item(0).fixedPart());
        assertEquals("b", m.item(1).fixedPart());

        m = metricWithName("a.b").submask(1);
        assertEquals(1, m.size());
        assertEquals("b", m.item(0).fixedPart());

        m = metricWithName("a.**.c").submask(1);
        assertEquals(2, m.size());
        assertEquals(ANY_PARTS, m.item(0).type());
        assertEquals("c", m.item(1).fixedPart());
    }
}