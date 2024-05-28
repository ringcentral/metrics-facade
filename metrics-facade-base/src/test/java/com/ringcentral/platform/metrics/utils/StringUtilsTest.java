package com.ringcentral.platform.metrics.utils;

import org.junit.Test;

import static com.ringcentral.platform.metrics.utils.StringUtils.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class StringUtilsTest {

    @Test
    public void isBlank_NullOrEmpty() {
        assertTrue(isBlank(null));
        assertTrue(isBlank(""));

        assertFalse(isNotBlank(null));
        assertFalse(isNotBlank(""));
    }

    @Test
    public void isBlank_Whitespaces() {
        assertTrue(isBlank(" "));
        assertTrue(isBlank("   "));
        assertTrue(isBlank("\t"));
        assertTrue(isBlank("\n"));
        assertTrue(isBlank(" \t\n"));

        assertFalse(isNotBlank(" "));
        assertFalse(isNotBlank("   "));
        assertFalse(isNotBlank("\t"));
        assertFalse(isNotBlank("\n"));
        assertFalse(isNotBlank(" \t\n"));
    }

    @Test
    public void isBlank_NonWhitespace() {
        assertFalse(isBlank("abc"));
        assertFalse(isBlank("  abc  "));

        assertTrue(isNotBlank("abc"));
        assertTrue(isNotBlank("  abc  "));
    }

    @Test
    public void splittingByDot() {
        assertThat(splitByDot("a.b.c"), is(new String[] { "a", "b", "c" }));
        assertThat(splitByDot("no_dots"), is(new String[] { "no_dots" }));
        assertThat(splitByDot("a..b"), is(new String[] { "a", "", "b" }));
        assertThat(splitByDot(".start"), is(new String[] { "", "start" }));
        assertThat(splitByDot("end."), is(new String[] { "end" }));
        assertThat(splitByDot(""), is(new String[] { "" }));
    }
}