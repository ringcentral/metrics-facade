package com.ringcentral.platform.metrics.labels;

import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.labels.LabelValues.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LabelValuesTest {

    static final Label LABEL_1 = new Label("label_1");
    static final Label LABEL_2 = new Label("label_2");
    static final Label LABEL_3 = new Label("label_3");
    static final Label LABEL_4 = new Label("label_4");

    @Test
    public void makingLabelValues() {
        LabelValues labelValues = labelValues(
            labelValues(LABEL_1.value("1"), LABEL_2.value("2")),
            LABEL_3.value("3"), LABEL_4.value("4"));

        assertThat(
            labelValues.list(),
            is(List.of(LABEL_1.value("1"), LABEL_2.value("2"), LABEL_3.value("3"), LABEL_4.value("4"))));

        labelValues = labelValues(
            noLabelValues(),
            LABEL_3.value("3"), LABEL_4.value("4"));

        assertThat(
            labelValues.list(),
            is(List.of(LABEL_3.value("3"), LABEL_4.value("4"))));

        labelValues = labelValues(labelValues(LABEL_1.value("1"), LABEL_2.value("2")));

        assertThat(
            labelValues.list(),
            is(List.of(LABEL_1.value("1"), LABEL_2.value("2"))));
    }

    @Test
    public void underlyingLabels() {
        LabelValues labelValues = labelValues(
            LABEL_1.value("1"), LABEL_2.value("2"),
            LABEL_3.value("3"), LABEL_4.value("4"));

        assertThat(labelValues.labels(), is(List.of(LABEL_1, LABEL_2, LABEL_3, LABEL_4)));
    }
}