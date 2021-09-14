package com.ringcentral.platform.metrics.utils;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;

import java.util.List;

import static com.ringcentral.platform.metrics.utils.ObjectUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ObjectUtilsTest {

    @Test
    public void hashCodeFor_Object_Object() {
        Object o1 = new Object();
        Object o2 = new Object();
        assertThat(hashCodeFor(o1, o2), is(hashCodeBuilder().append(o1).append(o2).toHashCode()));

        o1 = new int[] { 1, 2 };
        o2 = new Object();
        assertThat(hashCodeFor(o1, o2), is(hashCodeBuilder().append(o1).append(o2).toHashCode()));

        o1 = new int[] { 1, 2 };
        o2 = new int[] { 1, 2 };
        assertThat(hashCodeFor(o1, o2), is(hashCodeBuilder().append(o1).append(o2).toHashCode()));

        o1 = List.of(1, 2);
        o2 = List.of(1, 2);
        assertThat(hashCodeFor(o1, o2), is(hashCodeBuilder().append(o1).append(o2).toHashCode()));

        o1 = List.of(1, 2);
        o2 = null;
        assertThat(hashCodeFor(o1, o2), is(hashCodeBuilder().append(o1).append(o2).toHashCode()));
    }

    private HashCodeBuilder hashCodeBuilder() {
        return new HashCodeBuilder(17, 37);
    }
}