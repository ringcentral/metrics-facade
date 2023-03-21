package com.ringcentral.platform.metrics.var.configs.builders;

import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.var.configs.BaseCachingVarConfig;
import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.labels.LabelValues.labelValues;
import static com.ringcentral.platform.metrics.var.configs.builders.AbstractCachingVarConfigBuilder.DEFAULT_TTL_SEC;
import static com.ringcentral.platform.metrics.var.configs.builders.BaseCachingVarConfigBuilder.*;
import static java.util.concurrent.TimeUnit.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class BaseCachingVarConfigBuilderTest {

    static final Label LABEL_1 = new Label("label_1");
    static final Label LABEL_2 = new Label("label_2");
    static final Label LABEL_3 = new Label("label_3");
    static final Label LABEL_4 = new Label("label_4");

    @Test
    public void minConfig() {
        BaseCachingVarConfig config = new BaseCachingVarConfigBuilder().build();
        assertTrue(config.isEnabled());
        assertFalse(config.hasDescription());
        assertNull(config.description());
        assertTrue(config.prefixLabelValues().isEmpty());
        assertTrue(config.context().isEmpty());
        assertTrue(config.labels().isEmpty());
        assertThat(config.ttl(), is(DEFAULT_TTL_SEC));
        assertThat(config.ttlUnit(), is(SECONDS));
    }

    @Test
    public void maxConfig() {
        BaseCachingVarConfig config = cachingVar()
            .disable()
            .description("description")
            .prefix(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2")))
            .put("k_1", "v_1").put("k_2", "v_2")
            .labels(LABEL_3)
            .ttl(1L, HOURS)
            .build();

        assertFalse(config.isEnabled());
        assertTrue(config.hasDescription());
        assertThat(config.description(), is("description"));
        assertThat(config.prefixLabelValues(), is(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertThat(config.labels(), is(List.of(LABEL_3)));
        assertThat(config.ttl(), is(1L));
        assertThat(config.ttlUnit(), is(HOURS));
    }

    @Test
    public void rebase() {
        BaseCachingVarConfigBuilder builder = cachingVar();

        builder.rebase(withCachingVar()
            .disable()
            .description("description")
            .prefix(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2")))
            .put("k_1", "v_1_2").put("k_2", "v_2")
            .labels(LABEL_3)
            .ttl(1L, HOURS));

        BaseCachingVarConfig config = builder.build();
        assertFalse(config.isEnabled());
        assertThat(config.description(), is("description"));
        assertThat(config.prefixLabelValues(), is(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1_2"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.labels().isEmpty());
        assertThat(config.ttl(), is(1L));
        assertThat(config.ttlUnit(), is(HOURS));

        builder = cachingVar()
            .enable()
            .description("description_1")
            .prefix(labelValues(LABEL_3.value("v_3")))
            .put("k_1", "v_1_1")
            .labels(LABEL_1, LABEL_2)
            .ttl(2L, DAYS);

        builder.rebase(cachingVar()
            .disable()
            .description("description_2")
            .prefix(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2")))
            .put("k_1", "v_1_2").put("k_2", "v_2")
            .labels(LABEL_3)
            .ttl(1L, HOURS));

        config = builder.build();
        assertTrue(config.isEnabled());
        assertThat(config.description(), is("description_1"));
        assertThat(config.prefixLabelValues(), is(labelValues(LABEL_3.value("v_3"))));
        assertThat(config.context().get("k_1"), is("v_1_1"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertThat(config.labels(), is(List.of(LABEL_1, LABEL_2)));
        assertThat(config.ttl(), is(2L));
        assertThat(config.ttlUnit(), is(DAYS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rebase_labelsNotUnique() {
        BaseCachingVarConfigBuilder builder = cachingVar().labels(LABEL_1, LABEL_2);
        builder.rebase(withCachingVar().prefix(labelValues(LABEL_2.value("v_2"))));
    }

    @Test
    public void mod() {
        BaseCachingVarConfigBuilder builder = cachingVar();

        builder.modify(withCachingVar()
            .disable()
            .description("description")
            .prefix(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2")))
            .put("k_1", "v_1_2").put("k_2", "v_2")
            .labels(LABEL_3)
            .ttl(1L, HOURS));

        BaseCachingVarConfig config = builder.build();
        assertFalse(config.isEnabled());
        assertThat(config.description(), is("description"));
        assertThat(config.prefixLabelValues(), is(labelValues(LABEL_1.value("v_1"), LABEL_2.value("v_2"))));
        assertThat(config.context().get("k_1"), is("v_1_2"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertTrue(config.labels().isEmpty());
        assertThat(config.ttl(), is(1L));
        assertThat(config.ttlUnit(), is(HOURS));

        builder = cachingVar()
            .enable()
            .description("description_1")
            .prefix(labelValues(LABEL_3.value("v_3")))
            .put("k_1", "v_1_1")
            .labels(LABEL_1, LABEL_2)
            .ttl(2L, DAYS);

        builder.modify(cachingVar()
            .disable()
            .description("description_2")
            .prefix(labelValues(LABEL_4.value("v_4")))
            .put("k_1", "v_1_2").put("k_2", "v_2")
            .labels(LABEL_3)
            .ttl(1L, HOURS));

        config = builder.build();
        assertFalse(config.isEnabled());
        assertThat(config.description(), is("description_2"));
        assertThat(config.prefixLabelValues(), is(labelValues(LABEL_4.value("v_4"))));
        assertThat(config.context().get("k_1"), is("v_1_2"));
        assertThat(config.context().get("k_2"), is("v_2"));
        assertThat(config.labels(), is(List.of(LABEL_1, LABEL_2)));
        assertThat(config.ttl(), is(1L));
        assertThat(config.ttlUnit(), is(HOURS));
    }

    @Test(expected = IllegalArgumentException.class)
    public void mod_labelsNotUnique() {
        BaseCachingVarConfigBuilder builder = cachingVar().labels(LABEL_1, LABEL_2);
        builder.modify(withCachingVar().prefix(labelValues(LABEL_2.value("v_2"))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void labels_labelsNotUnique() {
        cachingVar()
            .prefix(labelValues(LABEL_1.value("v_1")))
            .labels(LABEL_1, LABEL_2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void prefixLabelValues_labelsNotUnique() {
        cachingVar()
            .labels(LABEL_1, LABEL_2)
            .prefix(labelValues(LABEL_2.value("v_1")));
    }
}