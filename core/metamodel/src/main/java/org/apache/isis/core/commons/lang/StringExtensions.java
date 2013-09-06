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

import com.google.common.base.Strings;

import org.apache.isis.applib.util.Enums;

public final class StringExtensions {
    
    static final char SPACE = ' ';

    private StringExtensions() {
    }

    // ////////////////////////////////////////////////////////////
    // naturalName, naturalize, simpleName, camel, memberIdFor
    // ////////////////////////////////////////////////////////////

    /**
     * Returns a word spaced version of the specified name, so there are spaces
     * between the words, where each word starts with a capital letter. E.g.,
     * "NextAvailableDate" is returned as "Next Available Date".
     */
    public static String asNaturalName2(final String name) {
    
        final int length = name.length();
    
        if (length <= 1) {
            return name.toUpperCase();// ensure first character is upper case
        }
    
        final StringBuffer naturalName = new StringBuffer(length);
    
        char previousCharacter;
        char character = Character.toUpperCase(name.charAt(0));// ensure first
                                                               // character is
                                                               // upper case
        naturalName.append(character);
        char nextCharacter = name.charAt(1);
    
        for (int pos = 2; pos < length; pos++) {
            previousCharacter = character;
            character = nextCharacter;
            nextCharacter = name.charAt(pos);
    
            if (previousCharacter != StringExtensions.SPACE) {
                if (Character.isUpperCase(character) && !Character.isUpperCase(previousCharacter)) {
                    naturalName.append(StringExtensions.SPACE);
                }
                if (Character.isUpperCase(character) && Character.isLowerCase(nextCharacter) && Character.isUpperCase(previousCharacter)) {
                    naturalName.append(StringExtensions.SPACE);
                }
                if (Character.isDigit(character) && !Character.isDigit(previousCharacter)) {
                    naturalName.append(StringExtensions.SPACE);
                }
            }
            naturalName.append(character);
        }
        naturalName.append(nextCharacter);
        return naturalName.toString();
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

    public static String asSimpleName(final String extendee) {
        final int lastDot = extendee.lastIndexOf('.');
        if (lastDot == -1) {
            return extendee;
        }
        if (lastDot == extendee.length() - 1) {
            throw new IllegalArgumentException("Name cannot end in '.'");
        }
        return extendee.substring(lastDot + 1);
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
        return extendee.toLowerCase().replaceAll("\\s+", "-");
    }

    public static String asPascal(final String extendee) {
        return capitalize(asCamel(extendee));
    }

    public static String asMemberIdFor(final String extendee) {
        return asLowerFirst(asCamel(extendee));
    }

    // ////////////////////////////////////////////////////////////
    // capitalize, lowerFirst, firstWord
    // ////////////////////////////////////////////////////////////

    public static String capitalize(final String extendee) {
        if (extendee == null || extendee.length() == 0) {
            return extendee;
        }
        if (extendee.length() == 1) {
            return extendee.toUpperCase();
        }
        return Character.toUpperCase(extendee.charAt(0)) + extendee.substring(1);
    }

    /**
     * Simply forces first char to be lower case.
     */
    public static String asLowerFirst(final String extendee) {
        if (Strings.isNullOrEmpty(extendee)) {
            return extendee;
        }
        if (extendee.length() == 1) {
            return extendee.toLowerCase();
        }
        return extendee.substring(0, 1).toLowerCase() + extendee.substring(1);
    }

    public static String asFirstWord(final String extendee) {
        final String[] split = extendee.split(" ");
        return split[0];
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

    public static String combinePaths(final String extendee, final String... furtherPaths) {
        final StringBuilder pathBuf = new StringBuilder(extendee);
        for (final String furtherPath : furtherPaths) {
            if (pathBuf.charAt(pathBuf.length() - 1) != File.separatorChar) {
                pathBuf.append(File.separatorChar);
            }
            pathBuf.append(furtherPath);
        }
        return pathBuf.toString();
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

    public static String removeTabs(final String extendee) {
        // quick return - jvm java should always return here
        if (extendee.indexOf('\t') == -1) {
            return extendee;
        }
        final StringBuffer buf = new StringBuffer();
        for (int i = 0; i < extendee.length(); i++) {
            // a bit clunky to stay with j# api
            if (extendee.charAt(i) != '\t') {
                buf.append(extendee.charAt(i));
            }
        }
        return buf.toString();
    }

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
    public static String normalized(final String extendee) {
        if (extendee == null) {
            return null;
        }
        return extendee.replaceAll("\\s+", " ");
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

    
    public static final String EMPTY = "";
    /**
     * <p>The maximum size to which the padding constant(s) can expand.</p>
     */
    private static final int PAD_LIMIT = 8192;

    /**
     * <p>Repeat a String <code>repeat</code> times to form a
     * new String.</p>
     *
     * <pre>
     * StringUtils.repeat(null, 2) = null
     * StringUtils.repeat("", 0)   = ""
     * StringUtils.repeat("", 2)   = ""
     * StringUtils.repeat("a", 3)  = "aaa"
     * StringUtils.repeat("ab", 2) = "abab"
     * StringUtils.repeat("a", -2) = ""
     * </pre>
     *
     * @param extendee  the String to repeat, may be null
     * @param repeat  number of times to repeat str, negative treated as zero
     * @return a new String consisting of the original String repeated,
     *  <code>null</code> if null String input
     */
    public static String repeat(final String extendee, int repeat) {
        // Performance tuned for 2.0 (JDK1.4)

        if (extendee == null) {
            return null;
        }
        if (repeat <= 0) {
            return EMPTY;
        }
        int inputLength = extendee.length();
        if (repeat == 1 || inputLength == 0) {
            return extendee;
        }
        if (inputLength == 1 && repeat <= PAD_LIMIT) {
            return padding(repeat, extendee.charAt(0));
        }

        int outputLength = inputLength * repeat;
        switch (inputLength) {
            case 1 :
                char ch = extendee.charAt(0);
                char[] output1 = new char[outputLength];
                for (int i = repeat - 1; i >= 0; i--) {
                    output1[i] = ch;
                }
                return new String(output1);
            case 2 :
                char ch0 = extendee.charAt(0);
                char ch1 = extendee.charAt(1);
                char[] output2 = new char[outputLength];
                for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
                    output2[i] = ch0;
                    output2[i + 1] = ch1;
                }
                return new String(output2);
            default :
                StringBuilder buf = new StringBuilder(outputLength);
                for (int i = 0; i < repeat; i++) {
                    buf.append(extendee);
                }
                return buf.toString();
        }
    }

    /**
     * <p>Returns padding using the specified delimiter repeated
     * to a given length.</p>
     *
     * <pre>
     * StringUtils.padding(0, 'e')  = ""
     * StringUtils.padding(3, 'e')  = "eee"
     * StringUtils.padding(-2, 'e') = IndexOutOfBoundsException
     * </pre>
     *
     * <p>Note: this method doesn't not support padding with
     * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode Supplementary Characters</a>
     * as they require a pair of <code>char</code>s to be represented.
     * If you are needing to support full I18N of your applications
     * consider using {@link #repeat(String, int)} instead. 
     * </p>
     *
     * @param repeat  number of times to repeat delim
     * @param padChar  character to repeat
     * @return String with repeated character
     * @throws IndexOutOfBoundsException if <code>repeat &lt; 0</code>
     * @see #repeat(String, int)
     */
    private static String padding(int repeat, char padChar) throws IndexOutOfBoundsException {
        if (repeat < 0) {
            throw new IndexOutOfBoundsException("Cannot pad a negative amount: " + repeat);
        }
        final char[] buf = new char[repeat];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = padChar;
        }
        return new String(buf);
    }

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
        if (Strings.isNullOrEmpty(extendee) && Strings.isNullOrEmpty(suffix)) {
            return "";
        }
        if (Strings.isNullOrEmpty(extendee)) {
            return suffix;
        }
        if (Strings.isNullOrEmpty(suffix)) {
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
        return Character.toUpperCase(extendee.charAt(0)) + extendee.substring(1);
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
