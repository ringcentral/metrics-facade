package com.ringcentral.platform.metrics.utils;

public class StringUtils {

    public static final String EMPTY_STRING = "";

    /**
     * Checks if a CharSequence is null, empty, or contains only whitespace characters (Character.isWhitespace(char) == true).
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("abc")     = false
     * StringUtils.isBlank("  abc  ") = false
     * </pre>
     */
    public static boolean isBlank(CharSequence cs) {
        if (cs == null) {
            return true;
        }

        int len = cs.length();

        if (len == 0) {
            return true;
        }

        for (int i = 0; i < len; ++i) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * Splits a given string into an array of substrings divided by the dot (.) character.
     */
    public static String[] splitByDot(String s) {
        return s.split("\\.");
    }
}
