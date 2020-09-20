package org.apache.isis.extensions.commandlog.impl.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

    public static String trimmed(final String str, final int lengthOfField) {
        if (str == null) {
            return null;
        }
        if (str.length() > lengthOfField) {
            return str.substring(0, lengthOfField - 3) + "...";
        }
        return str;
    }
}
