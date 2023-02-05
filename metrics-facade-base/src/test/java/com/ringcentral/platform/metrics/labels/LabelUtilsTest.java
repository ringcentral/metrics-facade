package com.ringcentral.platform.metrics.labels;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.ringcentral.platform.metrics.labels.LabelUtils.*;
import static com.ringcentral.platform.metrics.utils.CollectionUtils.isNonEmpty;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

@SuppressWarnings("ConstantConditions")
public class LabelUtilsTest {

    static final Label LABEL_1 = new Label("label_1");
    static final Label LABEL_2 = new Label("label_2");
    static final Label LABEL_3 = new Label("label_3");

    static LabelValue LV_1 = LABEL_1.value("1");
    static LabelValue LV_2 = LABEL_2.value("2");
    static LabelValue LV_3 = LABEL_3.value("3");

    @Test
    public void test_hasLabelValues() {
        assertTrue(isNonEmpty(List.of(LV_1)));
        assertTrue(isNonEmpty(List.of(LV_1, LV_2)));
        assertFalse(isNonEmpty(null));
        assertFalse(isNonEmpty(emptyList()));
    }

    @Test
    public void test_hasLabel() {
        assertTrue(hasLabel(List.of(LV_1), LABEL_1));
        assertTrue(hasLabel(List.of(LV_1, LV_2), LABEL_1));
        assertTrue(hasLabel(List.of(LV_1, LV_2), LABEL_2));

        assertFalse(hasLabel(emptyList(), LABEL_1));
        assertFalse(hasLabel(List.of(LV_1, LV_2), LABEL_3));
    }

    @Test
    public void test_valueOf() {
        assertThat(valueOf(List.of(LV_1), LABEL_1), is("1"));
        assertThat(valueOf(List.of(LV_1, LV_2), LABEL_1), is("1"));
        assertThat(valueOf(List.of(LV_1, LV_2), LABEL_2), is("2"));

        assertNull(valueOf(emptyList(), LABEL_1));
        assertNull(valueOf(List.of(LV_1), LABEL_2));
        assertNull(valueOf(List.of(LV_1, LV_2), LABEL_3));
    }

    @Test
    public void test_labelValueOf() {
        assertThat(labelValueOf(List.of(LV_1), LABEL_1), is(LV_1));
        assertThat(labelValueOf(List.of(LV_1, LV_2), LABEL_1), is(LV_1));
        assertThat(labelValueOf(List.of(LV_1, LV_2), LABEL_2), is(LV_2));

        assertNull(labelValueOf(emptyList(), LABEL_1));
        assertNull(labelValueOf(List.of(LV_1), LABEL_2));
        assertNull(labelValueOf(List.of(LV_1, LV_2), LABEL_3));
    }

    @Test
    public void test_labelToValue() {
        assertThat(labelToValue(List.of(LV_1)), is(Map.of(LABEL_1, LV_1)));
        assertThat(labelToValue(List.of(LV_1, LV_2)), is(Map.of(LABEL_1, LV_1, LABEL_2, LV_2)));
        assertThat(labelToValue(List.of(LV_1, LV_2, LV_3)), is(Map.of(LABEL_1, LV_1, LABEL_2, LV_2, LABEL_3, LV_3)));

        assertThat(labelToValue(null), is(emptyMap()));
        assertThat(labelToValue(emptyList()), is(emptyMap()));
    }

    @Test
    public void test_labelValuesWithout() {
        assertThat(labelValuesWithout(List.of(LV_1), LABEL_1), is(emptyList()));
        assertThat(labelValuesWithout(List.of(LV_1, LV_2), LABEL_1), is(List.of(LV_2)));
        assertThat(labelValuesWithout(List.of(LV_1, LV_2), LABEL_2), is(List.of(LV_1)));
        assertThat(labelValuesWithout(List.of(LV_1, LV_2, LV_3), LABEL_1), is(List.of(LV_2, LV_3)));
        assertThat(labelValuesWithout(List.of(LV_1, LV_2, LV_3), LABEL_2), is(List.of(LV_1, LV_3)));
        assertThat(labelValuesWithout(List.of(LV_1, LV_2, LV_3), LABEL_3), is(List.of(LV_1, LV_2)));
        assertThat(labelValuesWithout(List.of(LV_1, LV_2, LV_3), LABEL_1, LABEL_2), is(List.of(LV_3)));
        assertThat(labelValuesWithout(List.of(LV_1, LV_2, LV_3), LABEL_2, LABEL_3), is(List.of(LV_1)));
        assertThat(labelValuesWithout(List.of(LV_1, LV_2, LV_3), LABEL_1, LABEL_3), is(List.of(LV_2)));
        assertThat(labelValuesWithout(List.of(LV_1, LV_2, LV_3), LABEL_1, LABEL_2, LABEL_3), is(emptyList()));

        assertThat(labelValuesWithout(null, LABEL_1), is(emptyList()));
        assertThat(labelValuesWithout(emptyList(), LABEL_1), is(emptyList()));
        assertThat(labelValuesWithout(List.of(LV_1), LABEL_2), is(List.of(LV_1)));
        assertThat(labelValuesWithout(List.of(LV_1, LV_2), LABEL_3), is(List.of(LV_1, LV_2)));
    }
}