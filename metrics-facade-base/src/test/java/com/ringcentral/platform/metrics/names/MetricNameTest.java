package com.ringcentral.platform.metrics.names;

import org.junit.Test;

import static com.ringcentral.platform.metrics.names.MetricName.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class MetricNameTest {

    @Test
    public void making() {
        MetricName name = fromDotSeparated("A.B.C");
        assertThat(name.size(), is(3));
        assertThat(name.part(0), is("A"));
        assertThat(name.part(1), is("B"));
        assertThat(name.part(2), is("C"));

        name = fromDotSeparated(".A.B.C.");
        assertThat(name.size(), is(5));
        assertThat(name.part(0), is(""));
        assertThat(name.part(1), is("A"));
        assertThat(name.part(2), is("B"));
        assertThat(name.part(3), is("C"));
        assertThat(name.part(4), is(""));

        name = fromDotSeparated("..A..B..C..");
        assertThat(name.size(), is(9));
        assertThat(name.part(0), is(""));
        assertThat(name.part(1), is(""));
        assertThat(name.part(2), is("A"));
        assertThat(name.part(3), is(""));
        assertThat(name.part(4), is("B"));
        assertThat(name.part(5), is(""));
        assertThat(name.part(6), is("C"));
        assertThat(name.part(7), is(""));
        assertThat(name.part(8), is(""));

        assertThat(MetricName.of("A").toString(), is("A"));
        assertThat(withName("A").toString(), is("A"));
        assertThat(name("A").toString(), is("A"));
        assertThat(metricName("A").toString(), is("A"));
        assertThat(metricName("").toString(), is(""));

        assertThat(MetricName.of("A", "B").toString(), is("A.B"));
        assertThat(withName("A", "B").toString(), is("A.B"));
        assertThat(name("A", "B").toString(), is("A.B"));
        assertThat(metricName("A", "B").toString(), is("A.B"));
        assertThat(metricName().toString(), is(""));
        assertThat(metricName("A", "B", "C").toString(), is("A.B.C"));
        assertThat(metricName("A", "", "C").toString(), is("A..C"));

        assertThat(MetricName.of(MetricName.of("A"), "B").toString(), is("A.B"));
        assertThat(withName(withName("A"), "B").toString(), is("A.B"));
        assertThat(name(name("A"), "B").toString(), is("A.B"));
        assertThat(metricName(metricName("A"), "B").toString(), is("A.B"));
        assertThat(metricName(metricName("A", "B"), "C").toString(), is("A.B.C"));
        assertThat(metricName(metricName("A"), "B", "C").toString(), is("A.B.C"));
        assertThat(MetricName.of(MetricName.of("A", "B"), "C", "D").toString(), is("A.B.C.D"));
        assertThat(withName(withName("A", "B"), "C", "D").toString(), is("A.B.C.D"));
        assertThat(name(withName("A", "B"), "C", "D").toString(), is("A.B.C.D"));
        assertThat(metricName(metricName("A", "B"), "C", "D").toString(), is("A.B.C.D"));
        assertThat(metricName(metricName("A"), "", "C").toString(), is("A..C"));
        assertThat(metricName(metricName(), "C").toString(), is("C"));
        assertThat(metricName(metricName("A")).toString(), is("A"));
        assertThat(metricName(metricName(), "A", "B").toString(), is("A.B"));

        assertThat(MetricName.of(MetricName.of("A"), MetricName.of("B")).toString(), is("A.B"));
        assertThat(withName(withName("A"), withName("B")).toString(), is("A.B"));
        assertThat(name(name("A"), name("B")).toString(), is("A.B"));
        assertThat(metricName(metricName("A"), metricName("B")).toString(), is("A.B"));
        assertThat(metricName(metricName("A", "B"), metricName("C")).toString(), is("A.B.C"));
        assertThat(metricName(metricName("A"), metricName("B", "C")).toString(), is("A.B.C"));
        assertThat(metricName(metricName("A"), metricName("", "C")).toString(), is("A..C"));
        assertThat(metricName(metricName("A", "B"), metricName()).toString(), is("A.B"));
        assertThat(metricName(metricName(), metricName("C")).toString(), is("C"));

        assertTrue(emptyMetricName().isEmpty());
    }

    @Test
    public void lastPart() {
        assertNull(emptyMetricName().lastPart());
        assertThat(metricName("A").lastPart(), is("A"));
        assertThat(metricName("A", "B").lastPart(), is("B"));
    }

    @Test
    public void withNewPart() {
        assertThat(emptyMetricName().withNewPart("A"), is(name("A")));
        assertThat(metricName("A", "B").withNewPart("C"), is(name("A", "B", "C")));
        assertThat(metricName("A", "B").withNewPart("C", 0), is(name("C", "A", "B")));
        assertThat(metricName("A", "B").withNewPart("C", 1), is(name("A", "C", "B")));
        assertThat(metricName("A", "B").withNewPart("C", 2), is(name("A", "B", "C")));
    }

    @Test
    public void replacement() {
        assertThat(metricName("A", "B").replace("C", 0), is(name("C", "B")));
        assertThat(metricName("A", "B").replace("C", 1), is(name("A", "C")));

        assertThat(metricName("A", "B").replace("C", 0, "D", 1), is(name("C", "D")));
        assertThat(metricName("A", "B").replace("C", 1, "D", 0), is(name("D", "C")));
        assertThat(metricName("A", "B", "C").replace("D", 0, "E", 2), is(name("D", "B", "E")));
        assertThat(metricName("A", "B", "C").replace("D", 2, "E", 0), is(name("E", "B", "D")));

        assertThat(metricName("A").replaceLast("D"), is(name("D")));
        assertThat(metricName("A", "B").replaceLast("D"), is(name("A", "D")));
        assertThat(metricName("A", "B", "C").replaceLast("D"), is(name("A", "B", "D")));
    }

    @Test
    public void lowerCase() {
        assertThat(emptyMetricName().lowerCase(), is(emptyMetricName()));
        assertThat(metricName("A", "B").lowerCase(), is(metricName("a", "b")));
    }

    @Test
    public void equalsAndHashCode() {
        MetricName name = metricName("A", "B");
        assertEquals(name, name);
        assertEquals(metricName("A", "B"), metricName("A", "B"));
        assertEquals(metricName("A", "B").hashCode(), metricName("A", "B").hashCode());
        assertNotEquals(metricName("A", "B"), metricName("A", "B", "C"));
        assertNotEquals(metricName("A", "B"), new Object());

        assertTrue(metricName("A", "B").equals(emptyMetricName(), metricName("A", "B")));
        assertTrue(metricName("A", "B").equals(metricName("A", "B"), emptyMetricName()));
        assertTrue(emptyMetricName().equals(emptyMetricName(), emptyMetricName()));
        assertTrue(metricName("A", "B").equals(metricName("A"), metricName("B")));
        assertFalse(metricName("A", "B").equals(metricName("A"), metricName("B", "C")));
        assertFalse(metricName("A", "B").equals(metricName("A"), metricName("C")));
        assertFalse(metricName("A", "B", "C").equals(metricName("D"), metricName("B", "C")));
        assertFalse(metricName("A", "B", "C").equals(metricName("A"), metricName("D", "C")));
    }
}