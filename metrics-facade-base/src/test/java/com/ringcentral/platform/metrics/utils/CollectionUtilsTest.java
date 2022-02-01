package com.ringcentral.platform.metrics.utils;


import org.junit.Test;

import java.util.*;

import static com.ringcentral.platform.metrics.utils.CollectionUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class CollectionUtilsTest {

    @Test
    public void linkedHashMapOf_1() {
        assertThat(linkedHashMapOf(List.of("k_1"), List.of("v_1")), is(Map.of("k_1", "v_1")));

        assertThat(linkedHashMapOf(
            List.of("k_1", "k_2"), List.of("v_1", "v_2")),
            is(Map.of("k_1", "v_1", "k_2", "v_2")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void linkedHashMapOf_keyCountIsNotEqualToValueCount() {
        linkedHashMapOf(List.of("k_1", "k_2"), List.of("v_1"));
    }

    @Test
    public void iterToList_1() {
        assertThat(iterToList(emptyIterator()), is(emptyList()));
        assertThat(iterToList(asList(1, 2, 3).iterator()), is(List.of(1, 2, 3)));
    }

    @Test
    public void iterToSet_1() {
        assertThat(iterToSet(emptyIterator()), is(emptySet()));
        assertThat(iterToSet(asList(1, 2, 3).iterator()), is(Set.of(1, 2, 3)));
    }

    @Test
    public void containsAllInOrder_1() {
        assertTrue(containsAllInOrder(List.of(), List.of()));
        assertTrue(containsAllInOrder(List.of(1, 2, 3), List.of(1, 2, 3)));
        assertTrue(containsAllInOrder(List.of(1, 2, 3), List.of(1, 2)));
        assertTrue(containsAllInOrder(List.of(1, 2, 3), List.of(2, 3)));
        assertTrue(containsAllInOrder(List.of(1, 2, 3), List.of(1, 3)));
        assertTrue(containsAllInOrder(List.of(1, 2, 3), List.of(1)));
        assertTrue(containsAllInOrder(List.of(1, 2, 3), List.of(2)));
        assertTrue(containsAllInOrder(List.of(1, 2, 3), List.of(3)));
        assertTrue(containsAllInOrder(List.of(1, 2, 3), emptyList()));
        assertFalse(containsAllInOrder(List.of(1, 2, 3), List.of(1, 2, 3, 4)));
        assertFalse(containsAllInOrder(List.of(1, 2, 3), List.of(1, 3, 2)));
        assertFalse(containsAllInOrder(List.of(1, 2, 3), List.of(2, 1)));
        assertFalse(containsAllInOrder(List.of(1, 2, 3), List.of(1, 5)));
    }

    @Test
    public void copyingLongArray() {
        long[] a = new long[] { 1L, 2L, 3L };
        long[] c = copyLongArray(a);
        assertThat(a, is(c));
        assertThat(a, is(not(sameInstance(c))));

        assertNull(copyLongArray(null));
    }
}