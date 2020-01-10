package org.apache.isis.extensions.base.dom.utils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public final class StringUtils {

    private StringUtils() {
    }

    private static Function<String, String> LOWER_CASE_THEN_CAPITALIZE = new Function<String, String>() {
        @Override
        public String apply(final String input) {
            return input != null? StringUtils.capitalize(input.toLowerCase()): null;
        }
    };

    private static Function<String, String> UPPER_CASE = new Function<String, String>() {
        @Override
        public String apply(final String input) {
            return input != null? input.toUpperCase(): null;
        }
    };

    public static String enumTitle(final String string) {
        if(string == null) {
            return null;
        }
        return Joiner.on(" ").join(Iterables.transform(Splitter.on("_").split(string), LOWER_CASE_THEN_CAPITALIZE));
    }

    public static String enumDeTitle(final String string) {
        if(string == null) {
            return null;
        }
        return Joiner.on("_").join(Iterables.transform(Splitter.on(" ").split(string), UPPER_CASE));
    }

    public static String wildcardToCaseInsensitiveRegex(final String pattern) {
        if(pattern == null) {
            return null;
        }
        return "(?i)".concat(wildcardToRegex(pattern));
    }

    public static String wildcardToRegex(final String pattern) {
        if(pattern == null) {
            return null;
        }
        return pattern.replace("*", ".*").replace("?", ".");
    }

    public static String capitalize(final String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }


}
