package org.apache.isis.extensions.restful.viewer.util;



public final class StringUtil {

    private StringUtil() {}

    public static String concat(final String... lines) {
        final StringBuilder buf = new StringBuilder();
        for (final String line : lines) {
            buf.append(line);
        }
        return buf.toString();
    }

    public static String quote(final String val) {
        return "\"" + val + "\"";
    }

}
