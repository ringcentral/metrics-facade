package com.ringcentral.platform.metrics.producers;

import static java.lang.Double.*;

public class Ratio {

    private final double numerator;
    private final double denominator;

    public static Ratio of(double numerator, double denominator) {
        return new Ratio(numerator, denominator);
    }

    public Ratio(double numerator, double denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public double value() {
        double d = denominator;

        if (isNaN(d) || isInfinite(d) || d == 0) {
            return Double.NaN;
        }

        return numerator / d;
    }

    @Override
    public String toString() {
        return numerator + ":" + denominator;
    }
}
