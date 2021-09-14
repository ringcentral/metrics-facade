package com.ringcentral.platform.metrics.dimensions;

import org.junit.Test;

import static org.junit.Assert.*;

public class MetricDimensionValueMaskTest {

    static final MetricDimension DIMENSION = new MetricDimension("name");

    @Test
    public void multipleOptions() {
        MetricDimensionValueMask pattern = MetricDimensionValueMask.of(
            DIMENSION,
            "aaa|*bbb|ccc*|*ddd*|eee*fff|*ggg*hhh|iii*jjj*|*kkk*ooo*");

        assertTrue(pattern.matches("aaa"));
        assertFalse(pattern.matches("aa"));
        assertFalse(pattern.matches("aaaa"));

        assertTrue(pattern.matches("bbb"));
        assertTrue(pattern.matches("bbbb"));
        assertTrue(pattern.matches("abbb"));
        assertFalse(pattern.matches("bb"));
        assertFalse(pattern.matches("bbba"));

        assertTrue(pattern.matches("ccc"));
        assertTrue(pattern.matches("cccc"));
        assertTrue(pattern.matches("cccb"));
        assertFalse(pattern.matches("cc"));
        assertFalse(pattern.matches("bccc"));

        assertTrue(pattern.matches("ddd"));
        assertTrue(pattern.matches("dddd"));
        assertTrue(pattern.matches("cddd"));
        assertTrue(pattern.matches("dddc"));
        assertFalse(pattern.matches("dd"));

        assertTrue(pattern.matches("eeefff"));
        assertTrue(pattern.matches("eee fff"));
        assertTrue(pattern.matches("eeedfff"));
        assertTrue(pattern.matches("eeeeffff"));
        assertFalse(pattern.matches("eefff"));
        assertFalse(pattern.matches("eeeff"));
        assertFalse(pattern.matches("eeefffd"));
        assertFalse(pattern.matches("deeefff"));

        assertTrue(pattern.matches("ggghhh"));
        assertTrue(pattern.matches("ggg hhh"));
        assertTrue(pattern.matches("gggfhhh"));
        assertTrue(pattern.matches("gggghhhh"));
        assertTrue(pattern.matches("dggghhh"));
        assertFalse(pattern.matches("gghhh"));
        assertFalse(pattern.matches("ggghh"));
        assertFalse(pattern.matches("ggghhhf"));

        assertTrue(pattern.matches("iiijjj"));
        assertTrue(pattern.matches("iii jjj"));
        assertTrue(pattern.matches("iiihjjj"));
        assertTrue(pattern.matches("iiiijjjj"));
        assertTrue(pattern.matches("iiijjjh"));
        assertFalse(pattern.matches("hiiijjj"));
        assertFalse(pattern.matches("iijjj"));
        assertFalse(pattern.matches("iiijj"));

        assertTrue(pattern.matches("kkkooo"));
        assertTrue(pattern.matches("kkk ooo"));
        assertTrue(pattern.matches("kkkjooo"));
        assertTrue(pattern.matches("kkkkoooo"));
        assertTrue(pattern.matches("kkkoooj"));
        assertTrue(pattern.matches("jkkkooo"));
        assertFalse(pattern.matches("kkooo"));
        assertFalse(pattern.matches("kkkoo"));
    }

    @Test
    public void matchingAny() {
        MetricDimensionValueMask pattern = MetricDimensionValueMask.of(DIMENSION, "*");
        assertTrue(pattern.matches(""));
        assertTrue(pattern.matches("a"));
        assertTrue(pattern.matches("ab"));
    }
}