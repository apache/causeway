package org.apache.isis.viewer.bdd.common.util;

import java.util.StringTokenizer;

public final class Strings {

    private Strings() {}

    /**
     * Simply forces first char to be lower case.
     */
    public static String lowerLeading(final String str) {
        if (Strings.emptyString(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase()
                + (str.length() > 1 ? str.substring(1) : "");
    }

    public static boolean emptyString(final String str) {
        return str == null || str.length() == 0;
    }

    public static boolean nullSafeEquals(final String str1, final String str2) {
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    public static String memberIdFor(final String member) {
        return Strings.lowerLeading(camel(member));
    }
    
    public static String camel(String name) {
        StringBuffer b = new StringBuffer(name.length());
        StringTokenizer t = new StringTokenizer(name);
        b.append(t.nextToken());
        while (t.hasMoreTokens()) {
          String token = t.nextToken();
          b.append(token.substring(0, 1).toUpperCase()); // replace spaces with
          // camelCase
          b.append(token.substring(1));
        }
        return b.toString();
      }


    public static String simpleName(final String str) {
        final int lastDot = str.lastIndexOf('.');
        if (lastDot == -1) {
            return str;
        }
        if (lastDot == str.length() - 1) {
            throw new IllegalArgumentException("Name cannot end in '.'");
        }
        return str.substring(lastDot + 1);
    }

    public static String[] splitOnCommas(final String commaSeparatedList) {
        if (commaSeparatedList == null) {
            return null;
        }
        final String removeLeadingWhiteSpace = Strings
                .removeLeadingWhiteSpace(commaSeparatedList);
        // special handling
        if (removeLeadingWhiteSpace.length() == 0) {
            return new String[0];
        }
        return removeLeadingWhiteSpace.split("\\W*,\\W*");
    }

    public static String removeLeadingWhiteSpace(final String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("^\\W*", "");
    }

}
