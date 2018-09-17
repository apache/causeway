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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.isis.applib.util.Enums;
import org.apache.isis.commons.internal.base._Strings;

public final class StringExtensions {

    private StringExtensions() {}

    // ////////////////////////////////////////////////////////////
    // naturalName, naturalize, simpleName, camel, memberIdFor
    // ////////////////////////////////////////////////////////////

    /**
     * Returns a word spaced version of the specified name, so there are spaces
     * between the words, where each word starts with a capital letter. E.g.,
     * "NextAvailableDate" is returned as "Next Available Date".
     */
    public static String asNaturalName2(String name) {
        return _Strings.asNaturalName2.apply(name);
    }

    public static String asNaturalName(final String extendee) {

        int pos = 0;

        // find first upper case character
        while ((pos < extendee.length()) && Character.isLowerCase(extendee.charAt(pos))) {
            pos++;
        }

        if (pos == extendee.length()) {
            return "invalid name";
        }
        return naturalized(extendee, pos);
    }

    public static String asNaturalized(final String extendee) {
        return naturalized(extendee, 0);
    }

    private static String naturalized(final String name, final int startingPosition) {
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

    public static String asCamel(final String extendee) {
        final StringBuffer b = new StringBuffer(extendee.length());
        final StringTokenizer t = new StringTokenizer(extendee);
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
    public static String asCamelLowerFirst(final String extendee) {
        final StringBuffer b = new StringBuffer(extendee.length());
        final StringTokenizer t = new StringTokenizer(extendee);
        b.append(asLowerFirst(t.nextToken()));
        while (t.hasMoreTokens()) {
            final String token = t.nextToken();
            b.append(token.substring(0, 1).toUpperCase()); // replace spaces
            // with camelCase
            b.append(token.substring(1).toLowerCase());
        }
        return b.toString();
    }

    public static String asLowerDashed(String extendee) {
        return _Strings.asLowerDashed.apply(extendee);
    }

    public static String asPascal(final String extendee) {
        return capitalize(asCamel(extendee));
    }

    // ////////////////////////////////////////////////////////////
    // capitalize, lowerFirst, firstWord
    // ////////////////////////////////////////////////////////////

    public static String capitalize(final String extendee) {
        return _Strings.capitalize(extendee);
    }

    /**
     * Simply forces first char to be lower case.
     */
    public static String asLowerFirst(final String extendee) {
        if (_Strings.isNullOrEmpty(extendee)) {
            return extendee;
        }
        if (extendee.length() == 1) {
            return extendee.toLowerCase();
        }
        return extendee.substring(0, 1).toLowerCase() + extendee.substring(1);
    }

    // ////////////////////////////////////////////////////////////
    // in, combinePaths, splitOnCommas
    // ////////////////////////////////////////////////////////////

    public static boolean in(final String extendee, final String[] strings) {
        for (final String strCandidate : strings) {
            if (strCandidate.equals(extendee)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> splitOnCommas(final String commaSeparatedExtendee) {
        if (commaSeparatedExtendee == null) {
            return null;
        }
        final String removeLeadingWhiteSpace = removeLeadingWhiteSpace(commaSeparatedExtendee);
        // special handling
        if (removeLeadingWhiteSpace.length() == 0) {
            return Collections.emptyList();
        }
        final String[] splitAsArray = removeLeadingWhiteSpace.split("\\W*,\\W*");
        return Arrays.asList(splitAsArray);
    }


    private static final char CARRIAGE_RETURN = '\n';
    private static final char LINE_FEED = '\r';

    /**
     * Converts any <tt>\n</tt> to <tt>line.separator</tt>
     *
     * @param extendee
     * @return
     */
    public static String lineSeparated(final String extendee) {
        final StringBuilder buf = new StringBuilder();
        final String lineSeparator = System.getProperty("line.separator");
        boolean lastWasLineFeed = false;
        for (final char c : extendee.toCharArray()) {
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

    public static String removeLeadingWhiteSpace(final String extendee) {
        if (extendee == null) {
            return null;
        }
        return extendee.replaceAll("^\\W*", "");
    }

    public static String stripNewLines(final String extendee) {
        return extendee.replaceAll("[\r\n]", "");
    }

    public static String stripLeadingSlash(final String extendee) {
        if (!extendee.startsWith("/")) {
            return extendee;
        }
        if (extendee.length() < 2) {
            return "";
        }
        return extendee.substring(1);
    }

    /**
     * Condenses any whitespace to a single character
     *
     * @param extendee
     * @return
     */
    public static String normalized(@javax.annotation.Nullable final String extendee) {
        return _Strings.asNormalized.apply(extendee);
    }

    public static String removePrefix(final String extendee, final String prefix) {
        return extendee.startsWith(prefix)
                ? extendee.substring(prefix.length())
                        : extendee;
    }

    public static String enumTitle(String enumName) {
        return Enums.getFriendlyNameOf(enumName);
    }

    public static String enumDeTitle(String enumFriendlyName) {
        return Enums.getEnumNameFromFriendly(enumFriendlyName);
    }

    /*
     * eg converts <tt>HiddenFacetForMemberAnnotation</tt> to <tt>HFFMA</tt>.
     */
    public static String toAbbreviation(final String extendee) {
        final StringBuilder buf = new StringBuilder();
        for(char c: extendee.toCharArray()) {
            if(Character.isUpperCase(c)) {
                buf.append(c);
            }
        }
        return buf.toString();
    }


    // //////////////////////////////////////
    // copied in from Apache Commons
    // //////////////////////////////////////



    public static boolean startsWith(final String extendee, final String prefix) {
        final int length = prefix.length();
        if (length >= extendee.length()) {
            return false;
        } else {
            final char startingCharacter = extendee.charAt(length);
            return extendee.startsWith(prefix) && Character.isUpperCase(startingCharacter);
        }
    }

    public static String combinePath(final String extendee, final String suffix) {
        if (_Strings.isNullOrEmpty(extendee) && _Strings.isNullOrEmpty(suffix)) {
            return "";
        }
        if (_Strings.isNullOrEmpty(extendee)) {
            return suffix;
        }
        if (_Strings.isNullOrEmpty(suffix)) {
            return extendee;
        }
        if (extendee.endsWith("/") || suffix.startsWith("/")) {
            return extendee + suffix;
        }
        return extendee + "/" + suffix;
    }

    /**
     * Returns the name of a Java entity without any prefix. A prefix is defined
     * as the first set of lowercase letters and the name is characters from,
     * and including, the first upper case letter. If no upper case letter is
     * found then an empty string is returned.
     *
     * <p>
     * Calling this method with the following Java names will produce these
     * results:
     *
     * <pre>
     *                     getCarRegistration        -&gt; CarRegistration
     *                     CityMayor -&gt; CityMayor
     *                     isReady -&gt; Ready
     * </pre>
     *
     */
    public static String asJavaBaseName(final String javaName) {
        int pos = 0;

        // find first upper case character
        final int len = javaName.length();

        while ((pos < len) && (javaName.charAt(pos) != '_') && Character.isLowerCase(javaName.charAt(pos))) {
            pos++;
        }

        if (pos >= len) {
            return "";
        }

        if (javaName.charAt(pos) == '_') {
            pos++;
        }

        if (pos >= len) {
            return "";
        }

        final String baseName = javaName.substring(pos);
        final char firstChar = baseName.charAt(0);

        if (Character.isLowerCase(firstChar)) {
            return Character.toUpperCase(firstChar) + baseName.substring(1);
        } else {
            return baseName;
        }
    }

    public static String asJavaBaseNameStripAccessorPrefixIfRequired(final String javaNameExtendee) {
        if (javaNameExtendee.startsWith("is") || javaNameExtendee.startsWith("get")) {
            return asJavaBaseName(javaNameExtendee);
        } else {
            return StringExtensions.asCapitalizedName(javaNameExtendee);
        }
    }

    public static String asCapitalizedName(final String extendee) {

        return Character.toUpperCase(extendee.charAt(0)) +
                extendee.substring(1);
    }


    public static String asPluralName(final String extendee) {
        String pluralName;
        if (extendee.endsWith("y")) {
            pluralName = extendee.substring(0, extendee.length() - 1) + "ies";
        } else if (extendee.endsWith("s") || extendee.endsWith("x")) {
            pluralName = extendee + "es";
        } else {
            pluralName = extendee + 's';
        }
        return pluralName;
    }

    public static String toCamelCase(final String extendee) {
        final String nameLower = extendee.toLowerCase();
        final StringBuilder buf = new StringBuilder();
        boolean capitalizeNext = false;
        for (int i = 0; i < nameLower.length(); i++) {
            final char ch = nameLower.charAt(i);
            if (ch == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    buf.append(Character.toUpperCase(ch));
                } else {
                    buf.append(ch);
                }
                capitalizeNext = false;
            }
        }
        return buf.toString();
    }

}
