package com.ringcentral.platform.metrics.labels;

import org.junit.Test;

import static org.junit.Assert.*;

public class LabelValueMaskTest {

    static final Label LABEL = new Label("name");

    @Test
    public void multipleOptions() {
        LabelValueMask mask = LabelValueMask.of(
            LABEL,
            "aaa|*bbb|ccc*|*ddd*|eee*fff|*ggg*hhh|iii*jjj*|*kkk*ooo*");

        assertTrue(mask.matches("aaa"));
        assertFalse(mask.matches("aa"));
        assertFalse(mask.matches("aaaa"));

        assertTrue(mask.matches("bbb"));
        assertTrue(mask.matches("bbbb"));
        assertTrue(mask.matches("abbb"));
        assertFalse(mask.matches("bb"));
        assertFalse(mask.matches("bbba"));

        assertTrue(mask.matches("ccc"));
        assertTrue(mask.matches("cccc"));
        assertTrue(mask.matches("cccb"));
        assertFalse(mask.matches("cc"));
        assertFalse(mask.matches("bccc"));

        assertTrue(mask.matches("ddd"));
        assertTrue(mask.matches("dddd"));
        assertTrue(mask.matches("cddd"));
        assertTrue(mask.matches("dddc"));
        assertFalse(mask.matches("dd"));

        assertTrue(mask.matches("eeefff"));
        assertTrue(mask.matches("eee fff"));
        assertTrue(mask.matches("eeedfff"));
        assertTrue(mask.matches("eeeeffff"));
        assertFalse(mask.matches("eefff"));
        assertFalse(mask.matches("eeeff"));
        assertFalse(mask.matches("eeefffd"));
        assertFalse(mask.matches("deeefff"));

        assertTrue(mask.matches("ggghhh"));
        assertTrue(mask.matches("ggg hhh"));
        assertTrue(mask.matches("gggfhhh"));
        assertTrue(mask.matches("gggghhhh"));
        assertTrue(mask.matches("dggghhh"));
        assertFalse(mask.matches("gghhh"));
        assertFalse(mask.matches("ggghh"));
        assertFalse(mask.matches("ggghhhf"));

        assertTrue(mask.matches("iiijjj"));
        assertTrue(mask.matches("iii jjj"));
        assertTrue(mask.matches("iiihjjj"));
        assertTrue(mask.matches("iiiijjjj"));
        assertTrue(mask.matches("iiijjjh"));
        assertFalse(mask.matches("hiiijjj"));
        assertFalse(mask.matches("iijjj"));
        assertFalse(mask.matches("iiijj"));

        assertTrue(mask.matches("kkkooo"));
        assertTrue(mask.matches("kkk ooo"));
        assertTrue(mask.matches("kkkjooo"));
        assertTrue(mask.matches("kkkkoooo"));
        assertTrue(mask.matches("kkkoooj"));
        assertTrue(mask.matches("jkkkooo"));
        assertFalse(mask.matches("kkooo"));
        assertFalse(mask.matches("kkkoo"));
    }

    @Test
    public void matchingAny() {
        LabelValueMask mask = LabelValueMask.of(LABEL, "*");
        assertTrue(mask.matches(""));
        assertTrue(mask.matches("a"));
        assertTrue(mask.matches("ab"));
    }
}