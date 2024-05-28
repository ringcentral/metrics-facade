package com.ringcentral.platform.metrics.utils;

import org.junit.Test;

import java.util.List;
import java.util.Objects;

import static com.ringcentral.platform.metrics.utils.ObjectUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ObjectUtilsTest {

    @Test
    public void hashCodeFor_Object_Object() {
        Object o1 = new Object();
        Object o2 = new Object();
        assertThat(hashCodeFor(o1, o2), is(Objects.hash(o1, o2)));

        o1 = new int[] { 1, 2 };
        o2 = new Object();
        assertThat(hashCodeFor(o1, o2), is(Objects.hash(o1, o2)));

        o1 = new int[] { 1, 2 };
        o2 = new int[] { 1, 2 };
        assertThat(hashCodeFor(o1, o2), is(Objects.hash(o1, o2)));

        o1 = List.of(1, 2);
        o2 = List.of(1, 2);
        assertThat(hashCodeFor(o1, o2), is(Objects.hash(o1, o2)));

        o1 = List.of(1, 2);
        o2 = null;
        assertThat(hashCodeFor(o1, o2), is(Objects.hash(o1, o2)));
    }

    @Test
    public void hashCodeFor_Object_Object_Object() {
        Object o1 = new Object();
        Object o2 = new Object();
        Object o3 = new Object();
        assertThat(hashCodeFor(o1, o2, o3), is(Objects.hash(o1, o2, o3)));

        o1 = new int[] { 1, 2, 3 };
        o2 = new Object();
        o3 = List.of(1, 2, 3);
        assertThat(hashCodeFor(o1, o2, o3), is(Objects.hash(o1, o2, o3)));
    }
}