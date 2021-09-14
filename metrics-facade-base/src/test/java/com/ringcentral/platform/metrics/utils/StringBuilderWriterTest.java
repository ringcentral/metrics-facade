package com.ringcentral.platform.metrics.utils;

import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class StringBuilderWriterTest {

    @Test
    public void stringBuilderWriter() throws IOException {
        StringBuilderWriter writer = new StringBuilderWriter();
        writer.append('A').append('B');

        CharSequence chSeq = new StringBuilder("_CD_");
        writer.append(chSeq);
        chSeq = new StringBuilder("_EF_");
        writer.append(chSeq);

        chSeq = new StringBuilder("___GH___");
        writer.append(chSeq, 2, 5 + 1 /* exclusive */);
        chSeq = new StringBuilder("___IJ___");
        writer.append(chSeq, 2, 5 + 1 /* exclusive */);

        writer.write("_KL_");
        writer.write("_MN_");

        writer.write("___OP___".toCharArray(), 2, 4);
        writer.write("___QR___", 2, 4);

        assertThat(writer.result(), is("AB_CD__EF__GH__IJ__KL__MN__OP__QR_"));

        writer.flush();
        writer.close();
    }
}