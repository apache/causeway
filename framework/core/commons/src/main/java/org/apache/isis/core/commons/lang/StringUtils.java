/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.commons.lang;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import com.google.common.collect.Lists;

import org.apache.isis.applib.util.Enums;

public final class StringUtils {
    private StringUtils() {
    }

    // ////////////////////////////////////////////////////////////
    // naturalName, naturalize, simpleName, camel, memberIdFor
    // ////////////////////////////////////////////////////////////

    public static String naturalName(final String name) {

        int pos = 0;

        // find first upper case character
        while ((pos < name.length()) && Character.isLowerCase(name.charAt(pos))) {
            pos++;
        }

        if (pos == name.length()) {
            return "invalid name";
        }
        return naturalize(name, pos);
    }

    public static String naturalize(final String name) {
        return naturalize(name, 0);
    }

    private static String naturalize(final String name, final int startingPosition) {
        if (name.length() <= startingPosition) {
            throw new IllegalArgumentException("string shorter than starting position provided");
        }
        final StringBuffer s = new StringBuffer(name.length() - startingPosition);
        for (int j = startingPosition; j < name.length(); j++) { // process
                                                                 // english name
                                                                 // - add spaces
            if ((j > startingPosition) && isStartOfNewWord(name.charAt(j), name.charAt(j - 1))) {
                s.append(' ');
            }
            if (j == startingPosition) {
                s.append(Character.toUpperCase(name.charAt(j)));
            } else {
                s.append(name.charAt(j));
            }
        }
        final String str = s.toString();
        return str;
    }

    private static boolean isStartOfNewWord(final char c, final char previousChar) {
        return Character.isUpperCase(c) || Character.isDigit(c) && !Character.isDigit(previousChar);
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

    public static String camel(final String name) {
        final StringBuffer b = new StringBuffer(name.length());
        final StringTokenizer t = new StringTokenizer(name);
        b.append(t.nextToken());
        while (t.hasMoreTokens()) {
            final String token = t.nextToken();
            b.append(token.substring(0, 1).toUpperCase()); // replace spaces
                                                           // with
            // camelCase
            b.append(token.substring(1));
        }
        return b.toString();
    }

    // TODO: combine with camel
    public static String camelLowerFirst(final String name) {
        final StringBuffer b = new StringBuffer(name.length());
        final StringTokenizer t = new StringTokenizer(name);
        b.append(lowerFirst(t.nextToken()));
        while (t.hasMoreTokens()) {
            final String token = t.nextToken();
            b.append(token.substring(0, 1).toUpperCase()); // replace spaces
                                                           // with camelCase
            b.append(token.substring(1).toLowerCase());
        }
        return b.toString();
    }

    public static String pascal(final String name) {
        return capitalize(camel(name));
    }

    public static String memberIdFor(final String member) {
        return lowerLeading(camel(member));
    }

    // ////////////////////////////////////////////////////////////
    // capitalize, lowerFirst, firstWord
    // ////////////////////////////////////////////////////////////

    public static String capitalize(final String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * Simply forces first char to be lower case.
     */
    public static String lowerFirst(final String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        if (str.length() == 1) {
            return str.toLowerCase();
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static String lowerLeading(final String str) {
        return lowerFirst(str);
    }

    public static String firstWord(final String line) {
        final String[] split = line.split(" ");
        return split[0];
    }

    // ////////////////////////////////////////////////////////////
    // isNullOrEmpty, nullSafeEquals
    // ////////////////////////////////////////////////////////////

    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty();
    }

    public static boolean nullSafeEquals(final String str1, final String str2) {
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    // ////////////////////////////////////////////////////////////
    // in, combine, combinePaths, splitOnCommas
    // ////////////////////////////////////////////////////////////

    public static boolean in(final String str, final String[] strings) {
        for (final String strCandidate : strings) {
            if (strCandidate.equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static String combine(final List<String> list) {
        final StringBuffer buf = new StringBuffer();
        for (final String message : list) {
            if (list.size() > 1) {
                buf.append("; ");
            }
            buf.append(message);
        }
        return buf.toString();
    }

    public static String combinePaths(final String path, final String... furtherPaths) {
        final StringBuilder buf = new StringBuilder(path);
        for (final String furtherPath : furtherPaths) {
            if (buf.charAt(buf.length() - 1) != File.separatorChar) {
                buf.append(File.separatorChar);
            }
            buf.append(furtherPath);
        }
        return buf.toString();
    }

    public static List<String> splitOnCommas(final String commaSeparatedList) {
        if (commaSeparatedList == null) {
            return null;
        }
        final String removeLeadingWhiteSpace = removeLeadingWhiteSpace(commaSeparatedList);
        // special handling
        if (removeLeadingWhiteSpace.length() == 0) {
            return Collections.emptyList();
        }
        final String[] splitAsArray = removeLeadingWhiteSpace.split("\\W*,\\W*");
        return Arrays.asList(splitAsArray);
    }

    // ////////////////////////////////////////////////////////////
    // commaSeparatedClassNames
    // ////////////////////////////////////////////////////////////

    public static String commaSeparatedClassNames(final List<Object> objects) {
        final StringBuilder buf = new StringBuilder();
        int i = 0;
        for (final Object object : objects) {
            if (i++ > 0) {
                buf.append(',');
            }
            buf.append(object.getClass().getName());
        }
        return buf.toString();
    }

    private static final char CARRIAGE_RETURN = '\n';
    private static final char LINE_FEED = '\r';

    /**
     * Converts any <tt>\n</tt> to <tt>line.separator</tt>
     * 
     * @param string
     * @return
     */
    public static String lineSeparated(final String string) {
        final StringBuilder buf = new StringBuilder();
        final String lineSeparator = System.getProperty("line.separator");
        boolean lastWasLineFeed = false;
        for (final char c : string.toCharArray()) {
            final boolean isLineFeed = c == LINE_FEED;
            final boolean isCarriageReturn = c == CARRIAGE_RETURN;
            if (isCarriageReturn) {
                buf.append(lineSeparator);
                lastWasLineFeed = false;
            } else {
                if (lastWasLineFeed) {
                    buf.append(LINE_FEED);
                }
                if (isLineFeed) {
                    lastWasLineFeed = true;
                } else {
                    buf.append(c);
                    lastWasLineFeed = false;
                }
            }
        }
        if (lastWasLineFeed) {
            buf.append(LINE_FEED);
        }
        return buf.toString();
    }

    // ////////////////////////////////////////////////////////////
    // removeTabs, removeLeadingWhiteSpace, stripLeadingSlash, stripNewLines,
    // normalize
    // ////////////////////////////////////////////////////////////

    public static String removeTabs(final String text) {
        // quick return - jvm java should always return here
        if (text.indexOf('\t') == -1) {
            return text;
        }
        final StringBuffer buf = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            // a bit clunky to stay with j# api
            if (text.charAt(i) != '\t') {
                buf.append(text.charAt(i));
            }
        }
        return buf.toString();
    }

    public static String removeLeadingWhiteSpace(final String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("^\\W*", "");
    }

    public static String stripNewLines(final String str) {
        return str.replaceAll("[\r\n]", "");
    }

    public static String stripLeadingSlash(final String path) {
        if (!path.startsWith("/")) {
            return path;
        }
        if (path.length() < 2) {
            return "";
        }
        return path.substring(1);
    }

    /**
     * Condenses any whitespace to a single character
     * 
     * @param str
     * @return
     */
    public static String normalized(final String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("\\s+", " ");
    }

    public static String[] normalized(final String... strings) {
        final List<String> stringList = Lists.newArrayList();
        for (final String string : strings) {
            stringList.add(normalized(string));
        }
        return stringList.toArray(new String[] {});
    }

    public static String removePrefix(final String name, final String prefix) {
        if (name.startsWith(prefix)) {
            return name.substring(prefix.length());
        } else {
            return name;
        }
    }

    public static <T> T coalesce(final T... strings) {
        for (final T str : strings) {
            if (str != null) {
                return str;
            }
        }
        return null;
    }

    public static String enumTitle(String enumName) {
        return Enums.getFriendlyNameOf(enumName);
    }

    public static String enumDeTitle(String enumFriendlyName) {
        return Enums.getEnumNameFromFriendly(enumFriendlyName);
    }

}
