package com.ringcentral.platform.metrics.utils;

import java.io.Writer;

public class StringBuilderWriter extends Writer {

    private final StringBuilder builder;

    public StringBuilderWriter() {
        this.builder = new StringBuilder();
    }

    @Override
    public Writer append(char ch) {
        builder.append(ch);
        return this;
    }

    @Override
    public Writer append(CharSequence chSeq) {
        builder.append(chSeq);
        return this;
    }

    @Override
    public Writer append(CharSequence chSeq, int start, int end) {
        builder.append(chSeq, start, end);
        return this;
    }

    @Override
    public void write(String s) {
        if (s != null) {
            builder.append(s);
        }
    }

    @Override
    public void write(char[] chArr, int offset, int length) {
        if (chArr != null) {
            builder.append(chArr, offset, length);
        }
    }

    public String result() {
        return builder.toString();
    }

    @Override public void flush() {}
    @Override public void close() {}
}